package net.rsprox.mcp.server

import net.rsprox.mcp.session.FakeLauncher
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClosedGateTest {
    private val server = ToolServer()

    // Every request the plugin received, as its op.
    private val received = CopyOnWriteArrayList<String>()

    @AfterTest
    fun cleanUp() {
        server.close()
    }

    private fun connect() {
        server.call("session_start", """{"target":"My Server","wait_ms":0}""")
        val plugin = server.hub.dial(FakeLauncher.FIRST_HTTP_PORT)
        plugin.read()
        plugin.serve { op, _ ->
            received += op
            """"ok":{}"""
        }

        assertEquals("connected", server.manager.start(null, "s1", 10_000).state)
    }

    private fun states(): List<Pair<String, String?>> = server.manager.list().map { it.state to it.reason }

    @Test
    fun `a session_start that arrives at a closed gate reserves no ports and lists no session`() {
        server.gate.close()

        assertEquals(
            "the MCP endpoint was turned off",
            server.error("session_start", """{"target":"My Server","wait_ms":0}"""),
        )
        assertEquals(emptyList(), server.launcher.reserved)
        assertEquals(emptyList(), states())
    }

    @Test
    fun `a session_start that waits its turn while the gate closes forks no client`() {
        val forking = CountDownLatch(1)
        val release = CountDownLatch(1)
        server.launcher.launch = {
            forking.countDown()
            release.await()
        }

        val first = CompletableFuture.supplyAsync { server.error("session_start", """{"wait_ms":0}""") }
        assertEquals(true, forking.await(10, TimeUnit.SECONDS))
        val second = CompletableFuture.supplyAsync { server.error("session_start", """{"wait_ms":0}""") }

        server.gate.close()
        release.countDown()

        assertEquals("session s1 failed to launch: the MCP endpoint was turned off", first.get(10, TimeUnit.SECONDS))
        assertEquals("the MCP endpoint was turned off", second.get(10, TimeUnit.SECONDS))
        assertEquals(1, server.launcher.reserved.size)
        assertEquals(listOf(FakeLauncher.FIRST_PROXY_PORT), server.launcher.killed)
        assertEquals(listOf("stopped" to "the MCP endpoint was turned off"), states())
    }

    @Test
    fun `a gate that closes once the ports of a launch are reserved forks no client`() {
        var forked = false
        server.launcher.onReserve = { server.gate.close() }
        server.launcher.launch = { forked = true }

        assertEquals(
            "session s1 failed to launch: the MCP endpoint was turned off",
            server.error("session_start", """{"wait_ms":0}"""),
        )
        assertEquals(false, forked)
        assertEquals(listOf(FakeLauncher.FIRST_PROXY_PORT), server.launcher.killed)
        assertEquals(listOf("stopped" to "the MCP endpoint was turned off"), states())
    }

    @Test
    fun `a client tool sends the plugin nothing through a closed gate, and works again once it opens`() {
        connect()
        server.gate.close()

        assertEquals(
            "closed: the MCP endpoint was turned off",
            server.error("client_click", """{"session":"s1","x":10,"y":20}"""),
        )
        assertEquals(emptyList(), received)

        server.gate.open()
        server.call("client_click", """{"session":"s1","x":10,"y":20}""")

        assertEquals(listOf("click"), received)
    }

    @Test
    fun `session_stop kills no client through a closed gate`() {
        connect()
        server.gate.close()

        assertEquals("the MCP endpoint was turned off", server.error("session_stop", """{"session":"s1"}"""))
        assertEquals(emptyList(), server.launcher.killed)
        assertEquals(listOf("connected" to null), states())
    }

    @Test
    fun `closing the gate waits for the act that is passing it and then refuses the next`() {
        val passing = CountDownLatch(1)
        val release = CountDownLatch(1)
        val act =
            CompletableFuture.supplyAsync {
                server.gate.ifOpen {
                    passing.countDown()
                    release.await()
                }
            }

        assertEquals(true, passing.await(10, TimeUnit.SECONDS))
        val closed = CompletableFuture.runAsync { server.gate.close() }

        assertFailsWith<TimeoutException> { closed.get(200, TimeUnit.MILLISECONDS) }
        release.countDown()
        closed.get(10, TimeUnit.SECONDS)

        assertEquals(true, act.get(10, TimeUnit.SECONDS))
        assertEquals(false, server.gate.ifOpen { error("no act passes a closed gate") })
    }
}
