package net.rsprox.mcp.bridge

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.Writer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * A failed bridge call. [code] is one the plugin reports (`bad_args`, `not_found`, `wrong_state`,
 * `timeout`, `internal`) or `closed` when the client went away.
 */
public class BridgeError(
    /** The code that says what kind of failure this is. */
    public val code: String,
    message: String,
) : Exception(message)

/**
 * One connected in-client bridge. Compared by identity, so a late event from an old link cannot match a new one.
 *
 * The wire carries one JSON object per line in each direction. Requests may overlap; each reply is
 * matched to its caller by id.
 */
public class BridgeLink internal constructor(
    /** The connection to the plugin. */
    private val socket: Closeable,
    /** The source of the plugin's replies. */
    private val reader: BufferedReader,
    /** The sink of the requests. */
    private val writer: Writer,
    /** The callback that hears, once, that the link has closed. */
    private val onClosed: (BridgeLink) -> Unit,
) {
    /** The calls that wait for a reply, by request id. */
    private val pending = ConcurrentHashMap<Long, CompletableFuture<JsonNode>>()

    /** The source of request ids. */
    private val ids = AtomicLong()

    /** Whether the reader has stopped, after which no reply can arrive. */
    private val closed = AtomicBoolean()

    /** Start the thread that reads the plugin's replies. */
    internal fun start() {
        val thread = Thread(::readLoop, "mcp-bridge-reader")
        thread.isDaemon = true
        thread.start()
    }

    /**
     * Send one request and block the calling thread for its reply.
     *
     * @return the `ok` value of the reply
     * @throws BridgeError when the plugin answers `err`, when no reply arrives within [timeoutMs]
     * (code `timeout`), or when the link is or becomes closed (code `closed`)
     */
    public fun call(
        op: String,
        args: ObjectNode,
        timeoutMs: Long,
    ): JsonNode {
        val id = ids.incrementAndGet()
        val reply = CompletableFuture<JsonNode>()
        pending[id] = reply

        try {
            // Checked after registering: a close that happens from here on fails this call through `pending`.
            if (closed.get()) throw closedError()

            val request = MAPPER.createObjectNode().put("id", id).put("op", op)
            request.set<JsonNode>("args", args)

            try {
                send(MAPPER.writeValueAsString(request))
            } catch (e: IOException) {
                close()
                throw closedError()
            }

            return reply.get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            throw BridgeError("timeout", "the client did not answer '$op' within $timeoutMs ms")
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        } finally {
            pending.remove(id)
        }
    }

    /** Drop the connection. The reader then fails the pending calls and reports the close. */
    internal fun close() {
        try {
            socket.close()
        } catch (e: IOException) {
            // The link is being dropped either way.
        }
    }

    /** Write one line to the plugin. */
    @Synchronized
    private fun send(line: String) {
        writer.write(line)
        writer.write("\n")
        writer.flush()
    }

    /** Complete each pending call with its reply until the connection ends, then fail the rest and report the close. */
    private fun readLoop() {
        try {
            while (true) {
                val line = reader.readLine() ?: break
                val message = MAPPER.readTree(line)
                val id = message?.get("id")?.asLong() ?: continue
                val reply = pending[id] ?: continue
                val err = message.get("err")

                if (err != null) {
                    val code = err.get("code")?.asText() ?: "internal"
                    reply.completeExceptionally(BridgeError(code, err.get("message")?.asText().orEmpty()))
                } else {
                    reply.complete(message.get("ok") ?: MAPPER.nullNode())
                }
            }
        } catch (e: IOException) {
            // Falls through to the close below, like an orderly end of stream.
        } catch (e: JsonProcessingException) {
            // A peer that stops speaking the protocol cannot be resynchronised.
        } finally {
            closed.set(true)
            close()

            for (reply in pending.values) {
                reply.completeExceptionally(closedError())
            }

            onClosed(this)
        }
    }

    /** Build the error for a call on a link whose client has gone away. */
    private fun closedError(): BridgeError = BridgeError("closed", "the client is no longer connected")

    private companion object {
        /** The JSON mapper of the wire. */
        private val MAPPER: ObjectMapper = jacksonObjectMapper()
    }
}
