package net.rsprox.mcp.session

import net.rsprox.mcp.bridge.BridgeLink
import net.rsprox.mcp.packets.PacketQuery
import net.rsprox.mcp.session.ClientState.Connected
import net.rsprox.mcp.session.ClientState.Launching
import net.rsprox.mcp.session.ClientState.Stopped
import net.rsprox.mcp.session.SessionEvent.Hello
import net.rsprox.mcp.session.SessionEvent.Launched
import net.rsprox.mcp.session.SessionEvent.LinkClosed
import net.rsprox.mcp.session.SessionEvent.Stop
import net.rsprox.proxy.target.ProxyTargetConfig
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal fun target(
    id: Int,
    name: String,
): ProxyTargetConfig =
    ProxyTargetConfig(
        id = id,
        name = name,
        javConfigUrl = "http://127.0.0.1/jav_config.ws",
        modulus = null,
        varpCount = 5000,
        revision = null,
        runeliteBootstrapUrl = null,
        runeliteBootstrapCommitHash = null,
        runeliteGamepackUrl = null,
        binaryFolder = null,
    )

class SessionTest {
    private val first = Launch(generation = 1, proxyPort = 43751, httpPort = 43650, startedAtMs = 0)
    private val second = Launch(generation = 2, proxyPort = 43752, httpPort = 43651, startedAtMs = 0)
    private val link = BridgeLink()

    @Test
    fun `a launch starts only from stopped`() {
        assertEquals(Launching(first), reduce(Stopped("not started"), Launched(first)))
        assertEquals(Launching(first), reduce(Launching(first), Launched(second)))
        val connected = Connected(first, link, pid = 7)
        assertEquals(connected, reduce(connected, Launched(second)))
    }

    @Test
    fun `a hello for the current launch connects it`() {
        assertEquals(
            Connected(first, link, pid = 7),
            reduce(Launching(first), Hello(first.httpPort, link, pid = 7)),
        )
    }

    @Test
    fun `a hello from an earlier launch is dropped`() {
        assertEquals(Launching(second), reduce(Launching(second), Hello(first.httpPort, link, pid = 7)))
        assertEquals(Stopped("stopped by caller"), reduce(Stopped("stopped by caller"), Hello(first.httpPort, link, 7)))
        val connected = Connected(first, link, pid = 7)
        assertEquals(connected, reduce(connected, Hello(first.httpPort, BridgeLink(), pid = 8)))
    }

    @Test
    fun `closing the current link stops the session`() {
        assertEquals(Stopped("client exited"), reduce(Connected(first, link, pid = 7), LinkClosed(link)))
    }

    @Test
    fun `a close from an earlier link is dropped`() {
        val relaunched = Connected(second, BridgeLink(), pid = 9)
        assertEquals(relaunched, reduce(relaunched, LinkClosed(link)))
        assertEquals(Launching(second), reduce(Launching(second), LinkClosed(link)))
        assertEquals(Stopped("stopped by caller"), reduce(Stopped("stopped by caller"), LinkClosed(link)))
    }

    @Test
    fun `stop applies from every running state and keeps the first reason when repeated`() {
        assertEquals(Stopped("stopped by caller"), reduce(Launching(first), Stop("stopped by caller")))
        assertEquals(Stopped("stopped by caller"), reduce(Connected(first, link, 7), Stop("stopped by caller")))
        val once = reduce(Launching(first), Stop("stopped by caller"))
        assertEquals(once, reduce(once, Stop("again")))
    }

    @Test
    fun `each transition leaves one marker in the packet log and dropped events leave none`() {
        val session = Session(SessionId("s1"), target(1, "My Server"))
        session.apply(Launched(first))
        session.apply(Launched(second))
        session.apply(Hello(first.httpPort, link, pid = 7))
        session.apply(Stop("stopped by caller"))
        session.apply(Stop("again"))

        val markers = session.packets.read(PacketQuery()).packets
        assertEquals(listOf("CLIENT_LAUNCHED", "CLIENT_CONNECTED", "CLIENT_EXITED"), markers.map { it.prot })
        assertEquals("generation=1 proxyPort=43751 httpPort=43650", markers[0].text)
        assertEquals("pid=7", markers[1].text)
        assertEquals("stopped by caller", markers[2].text)
    }

    @Test
    fun `the snapshot describes the current state`() {
        val session = Session(SessionId("s1"), target(1, "My Server"))
        val stopped = session.snapshot()
        assertEquals("stopped", stopped.state)
        assertEquals("not started", stopped.reason)
        assertNull(stopped.proxyPort)
        assertEquals(0, stopped.cursor)

        session.apply(Launched(first))
        val launching = session.snapshot()
        assertEquals("launching", launching.state)
        assertNull(launching.reason)
        assertEquals(1, launching.generation)
        assertEquals(43751, launching.proxyPort)
        assertEquals(43650, launching.httpPort)
        assertNull(launching.pid)
        assertEquals(1, launching.cursor)

        session.apply(Hello(first.httpPort, link, pid = 7))
        val connected = session.snapshot()
        assertEquals("connected", connected.state)
        assertEquals(7, connected.pid)
        assertEquals("My Server", connected.target)
    }

    @Test
    fun `awaiting a launching session returns at the deadline without throwing`() {
        val session = Session(SessionId("s1"), target(1, "My Server"))
        session.apply(Launched(first))
        assertEquals(Launching(first), session.awaitConnected(30))
        assertEquals(Launching(first), session.awaitConnected(0))
    }

    @Test
    fun `awaiting a launching session returns when it connects`() {
        val session = Session(SessionId("s1"), target(1, "My Server"))
        session.apply(Launched(first))
        val waiter = CompletableFuture.supplyAsync { session.awaitConnected(30_000) }
        session.apply(Hello(first.httpPort, link, pid = 7))
        assertEquals(Connected(first, link, pid = 7), waiter.get(10, TimeUnit.SECONDS))
    }

    @Test
    fun `an update from an earlier login does not change the current login`() {
        val logins = LoginRegistry()
        val old = logins.nextEpoch()
        logins.login(old, revision = 241, world = 1)
        logins.update(old) { it.copy(name = "first") }
        val new = logins.nextEpoch()
        logins.login(new, revision = 241, world = 2)

        logins.update(old) { it.copy(online = false, name = "stale") }
        logins.login(old, revision = 241, world = 1)

        assertEquals(LoginInfo(new, 241, 2, name = null, online = true, transcribing = false), logins.current())
    }

    @Test
    fun `a reconnect within one login keeps what is known about it`() {
        val logins = LoginRegistry()
        val epoch = logins.nextEpoch()
        logins.login(epoch, revision = 241, world = 1)
        logins.update(epoch) { it.copy(name = "tester", transcribing = true, online = false) }
        logins.login(epoch, revision = 241, world = 1)
        assertEquals(LoginInfo(epoch, 241, 1, name = "tester", online = true, transcribing = true), logins.current())
    }
}
