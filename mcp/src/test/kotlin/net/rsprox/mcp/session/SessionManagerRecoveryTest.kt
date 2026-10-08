package net.rsprox.mcp.session

import net.rsprox.mcp.bridge.Access
import net.rsprox.mcp.bridge.TestHub
import net.rsprox.mcp.bridge.awaitTrue
import net.rsprox.mcp.packets.PacketQuery
import net.rsprox.mcp.packets.TapSettingSetStore
import net.rsprox.mcp.server.McpDispatcher
import net.rsprox.mcp.server.ToolError
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SessionManagerRecoveryTest {
    private val launcher = FakeLauncher()
    private val fixture = TestHub()

    @AfterTest
    fun cleanUp() {
        fixture.close()
    }

    private fun manager(
        launchTimeoutMs: Long = 60_000,
        helloTimeoutMs: Long = 60_000,
    ) = SessionManager(launcher, TapSettingSetStore, fixture.hub, launchTimeoutMs, helloTimeoutMs)

    private fun SessionManager.awaitStopped(session: String): SessionSnapshot {
        awaitTrue("session $session is stopped") { list().single { it.session == session }.state == "stopped" }

        return list().single { it.session == session }
    }

    private fun SessionManager.markers(session: String): List<String> =
        resolve(session).packets.read(PacketQuery()).packets.map { it.prot }

    private fun rejection(httpPort: Int): String? = fixture.dial(httpPort).read()?.get("reject")?.asText()

    @Test
    fun `a client whose plugin never says hello is killed and its session stops with the reason`() {
        val manager = manager(helloTimeoutMs = 100)

        val waited = manager.start(null, null, 10_000)

        assertEquals("stopped", waited.state)
        assertEquals("the client started, but its bridge plugin never connected", waited.reason)
        awaitTrue("the client is killed") { launcher.killed == listOf(43751) }
        assertEquals(listOf("CLIENT_LAUNCHED", "CLIENT_EXITED"), manager.markers("s1"))
        assertEquals("no session expects httpPort 43650", rejection(43650))
        assertEquals(2, manager.start(null, "s1", 0).generation)
    }

    @Test
    fun `a client whose plugin says hello in time stays connected after the deadline`() {
        val manager = manager(helloTimeoutMs = 1_000)
        manager.start(null, null, 0)
        fixture.dial(FakeLauncher.FIRST_HTTP_PORT)
        assertEquals("connected", manager.start(null, "s1", 10_000).state)

        // The deadlines pass in the order of the launches, so that of s1 has passed once s2 is stopped.
        manager.start(null, null, 0)
        manager.awaitStopped("s2")

        awaitTrue("only the client of s2 is killed") { launcher.killed == listOf(43752) }
        assertEquals("connected", manager.list().first().state)
    }

    @Test
    fun `a hello of a stale plugin jar stops the session and says the jar is stale`() {
        val manager = manager()
        manager.start(null, null, 0)

        fixture.dial(43650, protocol = 2).read()

        assertEquals(
            "the client started, but its bridge plugin was rejected: " +
                "bridge protocol 2 is not supported; the installed plugin jar is stale",
            manager.awaitStopped("s1").reason,
        )
    }

    @Test
    fun `a rejected hello that names the port of a connected session leaves that session alone`() {
        val manager = manager()
        manager.start(null, null, 0)
        fixture.dial(43650)
        assertEquals("connected", manager.start(null, "s1", 10_000).state)

        assertEquals("bad token", fixture.dial(43650, token = "not-the-token").read()?.get("reject")?.asText())
        assertEquals("no session expects httpPort 43650", rejection(43650))

        assertEquals("connected", manager.list().single().state)
        assertEquals(emptyList(), launcher.killed)
    }

    @Test
    fun `a hello that arrives after its session was stopped is rejected`() {
        val manager = manager()
        manager.start(null, null, 0)
        manager.stop(null)

        assertEquals("no session expects httpPort 43650", rejection(43650))
        assertEquals("stopped by caller", manager.list().single().reason)
    }

    @Test
    fun `the hub expects no hello from any of several launches that failed`() {
        val manager = manager()
        launcher.launch = { throw IllegalStateException("proxy port could not be bound") }

        assertFailsWith<ToolError> { manager.start(null, null, 0) }
        repeat(2) { assertFailsWith<ToolError> { manager.start(null, "s1", 0) } }

        assertEquals(3, launcher.reserved.size)

        for (httpPort in 43650..43652) {
            assertEquals("no session expects httpPort $httpPort", rejection(httpPort))
        }

        assertEquals("proxy port could not be bound", manager.list().single().reason)
    }

    @Test
    fun `a launch whose launcher exits fails at once and leaves the other sessions usable`() {
        val manager = manager()
        manager.start(null, null, 0)
        val plugin = fixture.dial(43650)
        plugin.read()
        assertEquals("connected", manager.start(null, "s1", 10_000).state)
        plugin.serve { _, _ -> """"ok":{"tick":7}""" }

        val release = CountDownLatch(1)
        launcher.launch = { release.await() }
        launcher.launcherExited = { true }

        try {
            val launch = CompletableFuture.supplyAsync { assertFailsWith<ToolError> { manager.start(null, null, 0) } }

            // The launch timeout is a minute; a launcher that is gone must not be waited for that long.
            val error = launch.get(20, TimeUnit.SECONDS)
            assertEquals(
                "session s2 failed to launch: the launcher exited before completing its handshake",
                error.message,
            )

            val stopped = manager.list().last()
            assertEquals("stopped", stopped.state)
            assertEquals("the launcher exited before completing its handshake", stopped.reason)
            assertEquals(listOf(43752), launcher.killed)
            assertEquals("no session expects httpPort 43651", rejection(43651))

            val refused = assertFailsWith<ToolError> { manager.start(null, "s2", 0) }
            assertTrue(refused.message!!.contains("restart the rsprox MCP process"), refused.message)

            assertEquals(listOf("connected", "stopped"), manager.list().map { it.state })
            assertEquals("connected", manager.start(null, "s1", 0).state)
            val link = manager.resolve("s1").link("client_state", Access.READ)
            val state = link.call("state", McpDispatcher.MAPPER.createObjectNode(), Access.READ, 10_000)
            assertEquals(7, state.get("tick").asInt())
            assertEquals(listOf("CLIENT_LAUNCHED", "CLIENT_EXITED"), manager.markers("s2"))
            assertEquals("stopped by caller", manager.stop("s1").reason)
        } finally {
            release.countDown()
        }
    }
}
