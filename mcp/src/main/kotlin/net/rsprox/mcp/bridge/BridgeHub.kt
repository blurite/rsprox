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

    /** Returns false when the session no longer waits for this client; the hub then drops the link. */
    fun onHello(
        link: BridgeLink,
        pid: Long,
    ): Boolean

    fun onClosed(link: BridgeLink)
}

/**
 * Where in-client bridge plugins connect. The plugin dials in: it finds the port and a token in the
 * rendezvous file and names the HTTP port of its launch, which routes it to the session that expects it.
 * A client this process did not launch is rejected and stays dormant.
 */
public class BridgeHub(
    private val rendezvous: Path,
    private val softwareRendering: Boolean = false,
) : AutoCloseable {
    private val expected = ConcurrentHashMap<Int, BridgeListener>()
    private val links = ConcurrentHashMap.newKeySet<BridgeLink>()
    private val token = newToken()

    @Volatile
    private var server: ServerSocket? = null

    /** Binds an ephemeral loopback port and publishes it, with the token, in the rendezvous file. */
    public fun start() {
        val server = ServerSocket(0, BACKLOG, InetAddress.getLoopbackAddress())
        this.server = server

        writeRendezvous(server.localPort)

        val thread = Thread({ acceptLoop(server) }, "mcp-bridge-accept")
        thread.isDaemon = true
        thread.start()
    }

    /** Routes the hello that names [httpPort] to [listener]. Must be called before the client is forked. */
    internal fun expect(
        httpPort: Int,
        listener: BridgeListener,
    ) {
        expected[httpPort] = listener
    }

    override fun close() {
        Files.deleteIfExists(rendezvous)
        server?.close()
        links.forEach { it.close() }
    }

    private fun writeRendezvous(port: Int) {
        val content =
            MAPPER
                .createObjectNode()
                .put("protocol", PROTOCOL)
                .put("port", port)
                .put("token", token)
                .put("pid", ProcessHandle.current().pid())

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

    private fun acceptLoop(server: ServerSocket) {
        while (true) {
            val socket =
                try {
                    server.accept()
                } catch (e: IOException) {
                    return
                }

            try {
                greet(socket)
            } catch (e: Exception) {
                logger.debug(e) { "Dropped a bridge connection that did not complete its hello" }
                socket.close()
            }
        }
    }

    private fun greet(socket: Socket) {
        socket.soTimeout = HELLO_TIMEOUT_MS
        val reader = socket.getInputStream().bufferedReader(Charsets.UTF_8)
        val writer = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
        val hello = MAPPER.readTree(reader.readLine() ?: throw IOException("closed before hello"))
        val httpPort = hello.get("httpPort")?.asInt() ?: -1
        val refusal =
            when {
                !tokenMatches(hello.get("token")) -> "bad token"
                hello.get("hello")?.asInt() != PROTOCOL ->
                    "bridge protocol ${hello.get("hello")} is not supported; the installed plugin jar is stale"
                else -> null
            }

        val listener = if (refusal == null) expected.remove(httpPort) else null
        if (listener == null) {
            writer.line(MAPPER.createObjectNode().put("reject", refusal ?: "no session expects httpPort $httpPort"))
            socket.close()

            return
        }

        socket.soTimeout = 0
        val link =
            BridgeLink(socket, reader, writer) { closed ->
                links.remove(closed)
                listener.onClosed(closed)
            }

        // The welcome goes out before the session can see the link, so no request can overtake it.
        writer.line(
            MAPPER
                .createObjectNode()
                .put("welcome", PROTOCOL)
                .put("session", listener.session)
                .put("softwareRendering", softwareRendering),
        )

        if (!listener.onHello(link, hello.get("pid")?.asLong() ?: -1)) {
            socket.close()

            return
        }

        links += link
        link.start()
        logger.info { "Bridge connected for session ${listener.session} (httpPort $httpPort)" }
    }

    private fun tokenMatches(candidate: JsonNode?): Boolean {
        if (candidate == null || !candidate.isTextual) {
            return false
        }

        return MessageDigest.isEqual(candidate.asText().toByteArray(), token.toByteArray())
    }

    private fun Writer.line(message: JsonNode) {
        write(MAPPER.writeValueAsString(message))
        write("\n")
        flush()
    }

    private companion object {
        private val logger = InlineLogger()
        private val MAPPER: ObjectMapper = jacksonObjectMapper()
        private const val PROTOCOL = 1
        private const val BACKLOG = 16
        private const val HELLO_TIMEOUT_MS = 5_000

        private fun newToken(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)

            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }
    }
}
