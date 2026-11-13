package net.rsprox.mcp.bridge

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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

class BridgeHubTest {
    private val mapper = jacksonObjectMapper()
    private val fixture = TestHub()
    private val hub = fixture.hub

    @AfterTest
    fun cleanUp() {
        fixture.close()
    }

    private fun args(json: String = "{}"): ObjectNode = mapper.readTree(json) as ObjectNode

    /** A welcomed plugin and the link the hub made for it. */
    private fun connect(listener: RecordingListener = RecordingListener()): Pair<FakePlugin, BridgeLink> {
        hub.expect(43650, listener)
        val plugin = fixture.dial(43650)
        assertEquals(2, plugin.read()?.get("welcome")?.asInt())

        return plugin to listener.hello.get(10, TimeUnit.SECONDS).first
    }

    private fun <T> async(body: () -> T): CompletableFuture<T> = CompletableFuture.supplyAsync(body)

    private fun bridgeError(call: CompletableFuture<*>): BridgeError {
        val failure = assertFailsWith<ExecutionException> { call.get(10, TimeUnit.SECONDS) }

        return failure.cause as BridgeError
    }

    @Test
    fun `a hello for an expected port is welcomed and handed to its listener`() {
        val listener = RecordingListener(session = "s3")
        hub.expect(43650, listener)
        val plugin = fixture.dial(43650)

        val welcome = plugin.read()!!
        assertEquals(2, welcome.get("welcome").asInt())
        assertEquals("s3", welcome.get("session").asText())
        assertEquals("drive", welcome.get("access").asText())
        assertFalse(welcome.get("softwareRendering").asBoolean())
        assertEquals(4242, listener.hello.get(10, TimeUnit.SECONDS).second)
    }

    @Test
    fun `a wrong token is rejected and leaves the expectation in place`() {
        val listener = RecordingListener()
        hub.expect(43650, listener)

        val intruder = fixture.dial(43650, token = "not-the-token")
        assertEquals("bad token", intruder.read()?.get("reject")?.asText())
        assertNull(intruder.read())
        assertFalse(listener.hello.isDone)

        assertEquals(2, fixture.dial(43650).read()?.get("welcome")?.asInt())
        assertEquals(emptyList(), listener.rejected)
    }

    @Test
    fun `a port that no session expects is rejected`() {
        hub.expect(43650, RecordingListener())
        val stranger = fixture.dial(43600)
        assertEquals("no session expects httpPort 43600", stranger.read()?.get("reject")?.asText())
        assertNull(stranger.read())
    }

    @Test
    fun `a call sends the op and its args and returns the ok value`() {
        val (plugin, link) = connect()
        val call = async { link.call("click", args("""{"x":380,"y":215}"""), Access.DRIVE, TIMEOUT_MS) }

        val request = plugin.read()!!
        assertEquals("click", request.get("op").asText())
        assertEquals(args("""{"x":380,"y":215}"""), request.get("args"))
        plugin.send("""{"id":${request.get("id")},"ok":{"x":380,"y":215}}""")

        assertEquals(args("""{"x":380,"y":215}"""), call.get(10, TimeUnit.SECONDS))
    }

    @Test
    fun `a listener that reads is welcomed with read access, and its link refuses an op that sends input`() {
        val listener = RecordingListener(access = Access.READ)
        hub.expect(43650, listener)
        val plugin = fixture.dial(43650)
        assertEquals("read", plugin.read()?.get("access")?.asText())
        val link = listener.hello.get(10, TimeUnit.SECONDS).first

        val refused =
            assertFailsWith<BridgeError> { link.call("click", args("""{"x":1,"y":2}"""), Access.DRIVE, TIMEOUT_MS) }
        assertEquals("read_only", refused.code)

        val call = async { link.call("state", args(), Access.READ, TIMEOUT_MS) }
        val request = plugin.read()!!
        assertEquals("state", request.get("op").asText())
        plugin.send("""{"id":${request.get("id")},"ok":{}}""")
        call.get(10, TimeUnit.SECONDS)
    }

    @Test
    fun `an err reply becomes a bridge error with its code and message`() {
        val (plugin, link) = connect()
        plugin.serve { _, _ -> """"err":{"code":"not_found","message":"widget 558:7 is not visible"}""" }

        val error =
            assertFailsWith<BridgeError> {
                link.call("click", args("""{"widget":"558:7"}"""), Access.DRIVE, TIMEOUT_MS)
            }
        assertEquals("not_found", error.code)
        assertEquals("widget 558:7 is not visible", error.message)
    }

    @Test
    fun `a closed connection fails the pending calls, reports the close and refuses new calls`() {
        val listener = RecordingListener()
        val (plugin, link) = connect(listener)
        val pending = async { link.call("screenshot", args(), Access.READ, TIMEOUT_MS) }
        plugin.read()

        plugin.close()

        assertEquals("closed", bridgeError(pending).code)
        assertSame(link, listener.closed.get(10, TimeUnit.SECONDS))
        val refused = assertFailsWith<BridgeError> { link.call("state", args(), Access.READ, TIMEOUT_MS) }
        assertEquals("closed", refused.code)
    }

    private companion object {
        private const val TIMEOUT_MS = 10_000L
    }
}
