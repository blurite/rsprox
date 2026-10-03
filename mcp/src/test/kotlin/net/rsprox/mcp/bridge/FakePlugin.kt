package net.rsprox.mcp.bridge

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.net.InetAddress
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

private val mapper = jacksonObjectMapper()

/** A link that is connected to nothing, for tests that only need its identity. */
internal fun idleLink(): BridgeLink = BridgeLink(Closeable {}, BufferedReader(StringReader("")), StringWriter()) {}

/** Polls until [condition] holds, so a test fails with a message instead of hanging. */
internal fun awaitTrue(
    what: String,
    condition: () -> Boolean,
) {
    val deadline = System.nanoTime() + 10_000_000_000
    while (!condition()) {
        check(System.nanoTime() < deadline) { "timed out waiting until $what" }
        Thread.sleep(5)
    }
}

/** A started hub whose rendezvous file lives in a temporary directory. */
internal class TestHub(
    softwareRendering: Boolean = false,
) : AutoCloseable {
    val rendezvous: Path = Files.createTempDirectory("mcp-bridge-test").resolve("mcp").resolve("bridge.json")
    val hub = BridgeHub(rendezvous, softwareRendering).also { it.start() }

    override fun close() {
        hub.close()
        rendezvous.parent.parent
            .toFile()
            .deleteRecursively()
    }
}

internal class RecordingListener(
    override val session: String = "s1",
    private val accept: Boolean = true,
) : BridgeListener {
    val hello = CompletableFuture<Pair<BridgeLink, Long>>()
    val closed = CompletableFuture<BridgeLink>()

    override fun onHello(
        link: BridgeLink,
        pid: Long,
    ): Boolean {
        hello.complete(link to pid)
        return accept
    }

    override fun onClosed(link: BridgeLink) {
        closed.complete(link)
    }
}

/** The plugin's side of the wire, driven by the test. */
internal class FakePlugin private constructor(
    private val socket: Socket,
) : AutoCloseable {
    private val reader = socket.getInputStream().bufferedReader()
    private val writer = socket.getOutputStream().bufferedWriter()

    /** Null when the hub closed the connection. */
    fun read(): JsonNode? = reader.readLine()?.let(mapper::readTree)

    @Synchronized
    fun send(json: String) {
        writer.write(json)
        writer.write("\n")
        writer.flush()
    }

    /** Answers every request with the `"ok":...` or `"err":...` member that [answer] returns. */
    fun serve(answer: (op: String, args: JsonNode) -> String) {
        thread(isDaemon = true) {
            try {
                while (true) {
                    val request = read() ?: break
                    send("""{"id":${request.get("id")},${answer(request.get("op").asText(), request.get("args"))}}""")
                }
            } catch (e: IOException) {
                // The test closed the connection.
            }
        }
    }

    override fun close() {
        socket.close()
    }

    companion object {
        /** Connects and says hello the way the plugin does. The hub's answer is the next [read]. */
        fun dial(
            rendezvous: Path,
            httpPort: Int,
            token: String? = null,
            protocol: Int = 1,
            pid: Long = 4242,
        ): FakePlugin {
            val file = mapper.readTree(Files.readAllBytes(rendezvous))
            val socket = Socket(InetAddress.getLoopbackAddress(), file.get("port").asInt())
            socket.soTimeout = 10_000
            val plugin = FakePlugin(socket)
            val hello =
                mapper
                    .createObjectNode()
                    .put("hello", protocol)
                    .put("httpPort", httpPort)
                    .put("token", token ?: file.get("token").asText())
                    .put("pid", pid)
            plugin.send(mapper.writeValueAsString(hello))
            return plugin
        }
    }
}
