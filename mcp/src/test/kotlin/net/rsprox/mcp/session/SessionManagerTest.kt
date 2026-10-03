package net.rsprox.mcp.session

import net.rsprox.mcp.packets.PacketQuery
import net.rsprox.mcp.packets.TapSettingSetStore
import net.rsprox.mcp.server.ToolError
import net.rsprox.proxy.target.ProxyTargetConfig
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class FakeLauncher(
    private val targets: List<ProxyTargetConfig> = listOf(target(0, "Old School RuneScape"), target(1, "My Server")),
) : ClientLauncher {
    val reserved = ArrayList<ProxyTargetConfig>()
    val killed = ArrayList<Int>()
    var reserve: (ProxyTargetConfig) -> Unit = {}
    var launch: () -> Unit = {}

    override fun targets(): List<ProxyTargetConfig> = targets

    override fun reserve(target: ProxyTargetConfig): Reservation {
        reserve.invoke(target)
        reserved += target
        return Reservation(43750 + reserved.size, 43649 + reserved.size) { launch() }
    }

    override fun kill(proxyPort: Int) {
        killed += proxyPort
    }
}

class SessionManagerTest {
    private val launcher = FakeLauncher()
    private val manager = SessionManager(launcher, TapSettingSetStore, launchTimeoutMs = 200)

    @Test
    fun `a first start launches the first custom target and stays launching`() {
        val snapshot = manager.start(null, null, 0)
        assertEquals("s1", snapshot.session)
        assertEquals("My Server", snapshot.target)
        assertEquals("launching", snapshot.state)
        assertEquals(1, snapshot.generation)
        assertEquals(43751, snapshot.proxyPort)
        assertEquals(43650, snapshot.httpPort)
        assertEquals(listOf(snapshot), manager.list())
    }

    @Test
    fun `a target is chosen by name regardless of case`() {
        assertEquals("Old School RuneScape", manager.start("old school runescape", null, 0).target)
    }

    @Test
    fun `an unknown target names the known ones and creates no session`() {
        val error = assertFailsWith<ToolError> { manager.start("Nowhere", null, 0) }
        assertEquals("no target 'Nowhere'. Targets: Old School RuneScape, My Server", error.message)
        assertEquals(emptyList(), manager.list())
    }

    @Test
    fun `starting a running session again launches nothing`() {
        manager.start(null, null, 0)
        val again = manager.start(null, "s1", 0)
        assertEquals(1, again.generation)
        assertEquals(1, launcher.reserved.size)
    }

    @Test
    fun `stop kills the client and a later start relaunches the same session on new ports`() {
        manager.start(null, null, 0)
        val stopped = manager.stop("s1")
        assertEquals("stopped", stopped.state)
        assertEquals("stopped by caller", stopped.reason)
        assertEquals(listOf(43751), launcher.killed)

        val relaunched = manager.start(null, "s1", 0)
        assertEquals("s1", relaunched.session)
        assertEquals("launching", relaunched.state)
        assertEquals(2, relaunched.generation)
        assertEquals(43752, relaunched.proxyPort)
        assertEquals(
            listOf("CLIENT_LAUNCHED", "CLIENT_EXITED", "CLIENT_LAUNCHED"),
            manager.resolve("s1").packets.read(PacketQuery()).packets.map { it.prot },
        )
    }

    @Test
    fun `stopping a stopped session changes nothing`() {
        manager.start(null, null, 0)
        val first = manager.stop(null)
        val second = manager.stop(null)
        assertEquals(first, second)
        assertEquals(listOf(43751), launcher.killed)
    }

    @Test
    fun `a session cannot be restarted on another target`() {
        manager.start("My Server", null, 0)
        val error = assertFailsWith<ToolError> { manager.start("Old School RuneScape", "s1", 0) }
        assertEquals("session s1 belongs to target 'My Server'", error.message)
    }

    @Test
    fun `a target that cannot be prepared fails without creating a session`() {
        launcher.reserve = { throw RuntimeException("wrapper", IllegalStateException("connection refused")) }
        val error = assertFailsWith<ToolError> { manager.start(null, null, 0) }
        assertEquals("could not prepare target 'My Server': connection refused", error.message)
        assertEquals(emptyList(), manager.list())
    }

    @Test
    fun `a launch that fails stops the session with the cause and can be retried`() {
        launcher.launch = { throw IllegalStateException("proxy port 43751 could not be bound") }
        val error = assertFailsWith<ToolError> { manager.start(null, null, 0) }
        assertEquals("session s1 failed to launch: proxy port 43751 could not be bound", error.message)
        val stopped = manager.list().single()
        assertEquals("stopped", stopped.state)
        assertEquals("proxy port 43751 could not be bound", stopped.reason)
        assertEquals(listOf(43751), launcher.killed)

        launcher.launch = {}
        val retried = manager.start(null, "s1", 0)
        assertEquals("launching", retried.state)
        assertEquals(2, retried.generation)
    }

    @Test
    fun `a launch that never returns is abandoned and blocks further launches`() {
        val release = CountDownLatch(1)
        launcher.launch = { release.await() }
        try {
            val error = assertFailsWith<ToolError> { manager.start(null, null, 0) }
            assertEquals("session s1 failed to launch: launcher never completed its handshake", error.message)
            assertEquals("stopped", manager.list().single().state)
            assertEquals(listOf(43751), launcher.killed)

            launcher.launch = {}
            for (session in listOf("s1", null)) {
                val refused = assertFailsWith<ToolError> { manager.start(null, session, 0) }
                assertTrue(refused.message!!.contains("restart the rsprox MCP process"), refused.message)
            }
            assertEquals(1, launcher.reserved.size)
        } finally {
            release.countDown()
        }
    }

    @Test
    fun `an omitted session resolves only when exactly one exists`() {
        val none = assertFailsWith<ToolError> { manager.resolve(null) }
        assertEquals("no session exists yet; call session_start first", none.message)

        manager.start(null, null, 0)
        assertEquals("s1", manager.resolve(null).id.value)

        manager.start(null, null, 0)
        val several = assertFailsWith<ToolError> { manager.resolve(null) }
        assertEquals("several sessions exist; pass one of: s1, s2", several.message)
        val unknown = assertFailsWith<ToolError> { manager.resolve("s9") }
        assertEquals("no session 's9'. Sessions: s1, s2", unknown.message)
    }
}
