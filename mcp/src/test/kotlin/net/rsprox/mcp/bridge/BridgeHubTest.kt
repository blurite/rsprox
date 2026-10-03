package net.rsprox.mcp.bridge

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BridgeHubTest {
    private val mapper = jacksonObjectMapper()
    private val fixture = TestHub()
    private val hub = fixture.hub
    private val plugins = ArrayList<FakePlugin>()

    @AfterTest
    fun cleanUp() {
        plugins.forEach { it.close() }
        fixture.close()
    }

    private fun dial(
        httpPort: Int,
        token: String? = null,
        protocol: Int = 1,
    ): FakePlugin = FakePlugin.dial(fixture.rendezvous, httpPort, token, protocol).also { plugins += it }

    private fun args(json: String = "{}"): ObjectNode = mapper.readTree(json) as ObjectNode

    /** A welcomed plugin and the link the hub made for it. */
    private fun connect(listener: RecordingListener = RecordingListener()): Pair<FakePlugin, BridgeLink> {
        hub.expect(43650, listener)
        val plugin = dial(43650)
        assertEquals(1, plugin.read()?.get("welcome")?.asInt())
        return plugin to listener.hello.get(10, TimeUnit.SECONDS).first
    }

    private fun <T> async(body: () -> T): CompletableFuture<T> = CompletableFuture.supplyAsync(body)

    private fun bridgeError(call: CompletableFuture<*>): BridgeError {
        val failure = assertFailsWith<ExecutionException> { call.get(10, TimeUnit.SECONDS) }
        return failure.cause as BridgeError
    }

    @Test
    fun `the rendezvous file names the port and token, is private, and is deleted on close`() {
        val file = mapper.readTree(Files.readAllBytes(fixture.rendezvous))
        assertEquals(1, file.get("protocol").asInt())
        assertTrue(file.get("port").asInt() > 0)
        assertEquals(22, file.get("token").asText().length)
        assertEquals(ProcessHandle.current().pid(), file.get("pid").asLong())
        if (Files.getFileStore(fixture.rendezvous).supportsFileAttributeView("posix")) {
            assertEquals("rw-------", PosixFilePermissions.toString(Files.getPosixFilePermissions(fixture.rendezvous)))
        }

        hub.close()
        assertFalse(Files.exists(fixture.rendezvous))
    }

    @Test
    fun `each hub has its own token`() {
        TestHub().use { other ->
            val mine = mapper.readTree(Files.readAllBytes(fixture.rendezvous)).get("token")
            val theirs = mapper.readTree(Files.readAllBytes(other.rendezvous)).get("token")
            assertFalse(mine == theirs)
        }
    }

    @Test
    fun `a hello for an expected port is welcomed and handed to its listener`() {
        val listener = RecordingListener(session = "s3")
        hub.expect(43650, listener)
        val plugin = dial(43650)

        val welcome = plugin.read()!!
        assertEquals(1, welcome.get("welcome").asInt())
        assertEquals("s3", welcome.get("session").asText())
        assertFalse(welcome.get("softwareRendering").asBoolean())
        assertEquals(4242, listener.hello.get(10, TimeUnit.SECONDS).second)
    }

    @Test
    fun `a hub set to software rendering tells each client it welcomes`() {
        TestHub(softwareRendering = true).use { other ->
            other.hub.expect(43650, RecordingListener())
            val plugin = FakePlugin.dial(other.rendezvous, 43650, null, 1).also { plugins += it }

            assertTrue(plugin.read()!!.get("softwareRendering").asBoolean())
        }
    }

    @Test
    fun `a wrong token is rejected and leaves the expectation in place`() {
        val listener = RecordingListener()
        hub.expect(43650, listener)

        val intruder = dial(43650, token = "not-the-token")
        assertEquals("bad token", intruder.read()?.get("reject")?.asText())
        assertNull(intruder.read())
        assertFalse(listener.hello.isDone)

        assertEquals(1, dial(43650).read()?.get("welcome")?.asInt())
    }

    @Test
    fun `a port that no session expects is rejected`() {
        hub.expect(43650, RecordingListener())
        val stranger = dial(43600)
        assertEquals("no session expects httpPort 43600", stranger.read()?.get("reject")?.asText())
        assertNull(stranger.read())
    }

    @Test
    fun `a port is expected only once`() {
        connect()
        assertEquals("no session expects httpPort 43650", dial(43650).read()?.get("reject")?.asText())
    }

    @Test
    fun `another protocol version is rejected as a stale jar`() {
        val listener = RecordingListener()
        hub.expect(43650, listener)
        val reject = dial(43650, protocol = 2).read()?.get("reject")?.asText()
        assertEquals("bridge protocol 2 is not supported; the installed plugin jar is stale", reject)
        assertFalse(listener.hello.isDone)
    }

    @Test
    fun `a listener that no longer wants the client has its connection dropped`() {
        val listener = RecordingListener(accept = false)
        hub.expect(43650, listener)
        val plugin = dial(43650)
        assertEquals(1, plugin.read()?.get("welcome")?.asInt())
        assertNull(plugin.read())
        assertFalse(listener.closed.isDone)
    }

    @Test
    fun `a call sends the op and its args and returns the ok value`() {
        val (plugin, link) = connect()
        val call = async { link.call("click", args("""{"x":380,"y":215}""")) }

        val request = plugin.read()!!
        assertEquals("click", request.get("op").asText())
        assertEquals(args("""{"x":380,"y":215}"""), request.get("args"))
        plugin.send("""{"id":${request.get("id")},"ok":{"x":380,"y":215}}""")

        assertEquals(args("""{"x":380,"y":215}"""), call.get(10, TimeUnit.SECONDS))
    }

    @Test
    fun `overlapping calls each receive their own reply, whatever the order of the answers`() {
        val (plugin, link) = connect()
        val slow = async { link.call("screenshot", args()) }
        val first = plugin.read()!!
        val fast = async { link.call("state", args()) }
        val second = plugin.read()!!
        assertEquals(listOf("screenshot", "state"), listOf(first.get("op").asText(), second.get("op").asText()))

        plugin.send("""{"id":${second.get("id")},"ok":{"gameState":"LOGIN_SCREEN"}}""")
        assertEquals("LOGIN_SCREEN", fast.get(10, TimeUnit.SECONDS).get("gameState").asText())
        assertFalse(slow.isDone)

        plugin.send("""{"id":${first.get("id")},"ok":{"width":976}}""")
        assertEquals(976, slow.get(10, TimeUnit.SECONDS).get("width").asInt())
    }

    @Test
    fun `an err reply becomes a bridge error with its code and message`() {
        val (plugin, link) = connect()
        plugin.serve { _, _ -> """"err":{"code":"not_found","message":"widget 558:7 is not visible"}""" }

        val error = assertFailsWith<BridgeError> { link.call("click", args("""{"widget":"558:7"}""")) }
        assertEquals("not_found", error.code)
        assertEquals("widget 558:7 is not visible", error.message)
    }

    @Test
    fun `a call that gets no reply times out and the link stays usable`() {
        val (plugin, link) = connect()
        val error = assertFailsWith<BridgeError> { link.call("state", args(), timeoutMs = 50) }
        assertEquals("timeout", error.code)
        assertEquals("the client did not answer 'state' within 50 ms", error.message)

        val late = plugin.read()!!
        plugin.send("""{"id":${late.get("id")},"ok":{}}""")
        plugin.serve { _, _ -> """"ok":{"tick":7}""" }
        assertEquals(7, link.call("state", args()).get("tick").asInt())
    }

    @Test
    fun `a closed connection fails the pending calls, reports the close and refuses new calls`() {
        val listener = RecordingListener()
        val (plugin, link) = connect(listener)
        val pending = async { link.call("screenshot", args()) }
        plugin.read()

        plugin.close()

        assertEquals("closed", bridgeError(pending).code)
        assertSame(link, listener.closed.get(10, TimeUnit.SECONDS))
        assertEquals("closed", assertFailsWith<BridgeError> { link.call("state", args()) }.code)
    }

    @Test
    fun `closing the hub drops its links and stops accepting`() {
        val listener = RecordingListener()
        val (plugin, link) = connect(listener)

        hub.close()

        assertNull(plugin.read())
        assertSame(link, listener.closed.get(10, TimeUnit.SECONDS))
    }
}
