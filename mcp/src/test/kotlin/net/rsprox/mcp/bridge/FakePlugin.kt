package net.rsprox.mcp.bridge

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

private val mapper = jacksonObjectMapper()

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
internal class TestHub : AutoCloseable {
    private val rendezvous = Files.createTempDirectory("mcp-bridge-test").resolve("mcp").resolve("bridge.json")
    private val plugins = ArrayList<FakePlugin>()
    val hub = BridgeHub(rendezvous, Rendering.GPU).also { it.start() }

    /** Connects a plugin that says hello for [httpPort]. The hub's answer is its next [FakePlugin.read]. */
    fun dial(
        httpPort: Int,
        token: String? = null,
        protocol: Int = 1,
    ): FakePlugin = FakePlugin.dial(rendezvous, httpPort, token, protocol).also { plugins += it }

    override fun close() {
        plugins.forEach { it.close() }
        hub.close()
        rendezvous.parent.parent
            .toFile()
            .deleteRecursively()
    }
}

internal class RecordingListener(
    override val session: String = "s1",
) : BridgeListener {
    val hello = CompletableFuture<Pair<BridgeLink, Long>>()
    val closed = CompletableFuture<BridgeLink>()
    val rejected = CopyOnWriteArrayList<String>()

    override fun onHello(
        link: BridgeLink,
        pid: Long,
    ): Boolean {
        hello.complete(link to pid)

        return true
    }

    override fun onClosed(link: BridgeLink) {
        closed.complete(link)
    }

    override fun onRejected(reason: String) {
        rejected += reason
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
        /** Connects and says hello the way the plugin does. */
        fun dial(
            rendezvous: Path,
            httpPort: Int,
            token: String?,
            protocol: Int,
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
                    .put("pid", 4242)

            plugin.send(mapper.writeValueAsString(hello))

            return plugin
        }
    }
}
