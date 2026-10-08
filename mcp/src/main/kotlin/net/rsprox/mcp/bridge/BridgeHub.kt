package net.rsprox.mcp.bridge

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.michaelbull.logging.InlineLogger
import java.io.IOException
import java.io.Writer
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/** What a session wants to hear about the client it is waiting for. */
internal interface BridgeListener {
    /** The session id, told to the plugin for its log. */
    val session: String

    /**
     * Take the link of the client that said hello for this session.
     * Returns false when the session no longer waits for this client; the hub then drops the link.
     */
    fun onHello(
        link: BridgeLink,
        pid: Long,
    ): Boolean

    /** Hear that the link, which this listener accepted earlier, has closed. */
    fun onClosed(link: BridgeLink)

    /** Hear that a hello for this session was rejected for [reason]. The hub keeps expecting the client. */
    fun onRejected(reason: String)
}

/** How a launched client draws its frames. [SOFTWARE] has the plugin stop the client's GPU plugin. */
public enum class Rendering {
    /** Frames drawn by the GPU plugin, when the client's profile enables it. */
    GPU,

    /** Frames drawn without the GPU plugin, which the bridge plugin stops. */
    SOFTWARE,
}

/**
 * Where in-client bridge plugins connect. The plugin dials in: it finds the port and a token in the
 * rendezvous file and names the HTTP port of its launch, which routes it to the session that expects it.
 * A client this process did not launch is rejected and stays dormant.
 */
public class BridgeHub(
    /** The file through which a plugin finds this hub. */
    private val rendezvous: Path,
    /** The rendering that every welcomed client is told to use. */
    private val rendering: Rendering,
) : AutoCloseable {
    /** The listeners that wait for a client, by the HTTP port of its launch. */
    private val expected = ConcurrentHashMap<Int, BridgeListener>()

    /** The links to the connected clients. */
    private val links = ConcurrentHashMap.newKeySet<BridgeLink>()

    /** The secret that a plugin must repeat from the rendezvous file. */
    private val token = newToken()

    /** The socket that plugins dial, or null before [start]. */
    @Volatile
    private var server: ServerSocket? = null

    /** Bind an ephemeral loopback port and publish it, with the token, in the rendezvous file. */
    public fun start() {
        val server = ServerSocket(0, BACKLOG, InetAddress.getLoopbackAddress())
        this.server = server

        writeRendezvous(server.localPort)

        val thread = Thread({ acceptLoop(server) }, "mcp-bridge-accept")
        thread.isDaemon = true
        thread.start()
    }

    /** Route the hello that names [httpPort] to [listener]. Must be called before the client is forked. */
    internal fun expect(
        httpPort: Int,
        listener: BridgeListener,
    ) {
        expected[httpPort] = listener
    }

    /** Stop expecting the hello that names [httpPort], so a client that says it later is rejected. Idempotent. */
    internal fun forget(httpPort: Int) {
        expected.remove(httpPort)
    }

    /** Delete the rendezvous file, stop accepting plugins and drop every link. */
    override fun close() {
        Files.deleteIfExists(rendezvous)
        server?.close()
        links.forEach { it.close() }
    }

    /** Publish the port and the token in the rendezvous file, readable only by its owner. */
    private fun writeRendezvous(port: Int) {
        val content =
            MAPPER
                .createObjectNode()
                .put("port", port)
                .put("token", token)

        Files.createDirectories(rendezvous.parent)

        // Written beside the target and moved into place, so a plugin never reads a partial file
        // and the token is never in a file that others may read.
        val temp = Files.createTempFile(rendezvous.parent, "bridge", ".tmp")
        if (Files.getFileStore(temp).supportsFileAttributeView("posix")) {
            Files.setPosixFilePermissions(temp, PosixFilePermissions.fromString("rw-------"))
        }

        Files.write(temp, MAPPER.writeValueAsBytes(content))
        Files.move(temp, rendezvous, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }

    /** Hand each plugin that dials in to a thread of its own, until the server socket closes. */
    private fun acceptLoop(server: ServerSocket) {
        while (true) {
            val socket =
                try {
                    server.accept()
                } catch (e: IOException) {
                    return
                }

            // A plugin dials once and gives up after a few seconds, so one silent peer must not hold up the next.
            val thread = Thread({ greetOrDrop(socket) }, "mcp-bridge-hello")
            thread.isDaemon = true
            thread.start()
        }
    }

    /** Greet the plugin, and drop its connection when it does not complete its hello. */
    private fun greetOrDrop(socket: Socket) {
        try {
            greet(socket)
        } catch (e: Exception) {
            logger.debug(e) { "Dropped a bridge connection that did not complete its hello" }
            socket.close()
        }
    }

    /**
     * Read the hello of a plugin, then welcome it and hand its link to the session that expects it, or reject it.
     *
     * @throws IOException when the plugin closes the connection or stays silent before its hello
     */
    private fun greet(socket: Socket) {
        socket.soTimeout = HELLO_TIMEOUT_MS
        val reader = socket.getInputStream().bufferedReader(Charsets.UTF_8)
        val writer = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
        val hello = MAPPER.readTree(reader.readLine() ?: throw IOException("closed before hello"))
        val httpPort = hello.get("httpPort")?.asInt() ?: -1

        // Only a caller that holds the token may end a launch, so a stray local connection cannot stop a session.
        if (!tokenMatches(hello.get("token"))) return reject(socket, writer, "bad token")

        if (hello.get("hello")?.asInt() != PROTOCOL) return rejectStale(socket, writer, hello.get("hello"), httpPort)

        val listener = expected.remove(httpPort)
        if (listener == null) return reject(socket, writer, "no session expects httpPort $httpPort")

        socket.soTimeout = 0
        val link =
            BridgeLink(socket, reader, writer) { closed ->
                links.remove(closed)
                listener.onClosed(closed)
            }

        // The welcome goes out before the session can see the link, so no request can overtake it.
        writer.line(welcome(listener))

        if (!listener.onHello(link, hello.get("pid")?.asLong() ?: -1)) return socket.close()

        links += link
        link.start()
        logger.info { "Bridge connected for session ${listener.session} (httpPort $httpPort)" }
    }

    /** Build the welcome that tells the plugin its session and the rendering to use. */
    private fun welcome(listener: BridgeListener): JsonNode =
        MAPPER
            .createObjectNode()
            .put("welcome", PROTOCOL)
            .put("session", listener.session)
            .put("softwareRendering", rendering == Rendering.SOFTWARE)

    /**
     * Reject a plugin that speaks another protocol than this hub, and tell the session that waits for
     * its client, which will not dial again.
     */
    private fun rejectStale(
        socket: Socket,
        writer: Writer,
        protocol: JsonNode?,
        httpPort: Int,
    ) {
        val refusal = "bridge protocol $protocol is not supported; the installed plugin jar is stale"
        reject(socket, writer, refusal)
        expected[httpPort]?.onRejected(refusal)
    }

    /** Tell the plugin why it is not welcome and close its connection. */
    private fun reject(
        socket: Socket,
        writer: Writer,
        reason: String,
    ) {
        writer.line(MAPPER.createObjectNode().put("reject", reason))
        socket.close()
    }

    /** Determine if the candidate carries this hub's token, in constant time. */
    private fun tokenMatches(candidate: JsonNode?): Boolean {
        if (candidate == null || !candidate.isTextual) return false

        return MessageDigest.isEqual(candidate.asText().toByteArray(), token.toByteArray())
    }

    /** Write the message as one line. */
    private fun Writer.line(message: JsonNode) {
        write(MAPPER.writeValueAsString(message))
        write("\n")
        flush()
    }

    private companion object {
        /** The logger of the hub. */
        private val logger = InlineLogger()

        /** The JSON mapper of the wire and the rendezvous file. */
        private val MAPPER: ObjectMapper = jacksonObjectMapper()

        /** The version of the wire protocol that this hub speaks. */
        private const val PROTOCOL = 1

        /** The number of plugins that may wait to be accepted. */
        private const val BACKLOG = 16

        /** The longest wait for a plugin to send its hello. */
        private const val HELLO_TIMEOUT_MS = 5_000

        /** Generate a random token of 22 URL-safe characters. */
        private fun newToken(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)

            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }
    }
}
