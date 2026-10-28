package net.rsprox.mcp

import net.rsprox.mcp.bridge.BridgeError
import net.rsprox.mcp.bridge.Gate
import net.rsprox.mcp.server.McpDispatcher
import net.rsprox.mcp.server.McpHttpServer
import net.rsprox.mcp.server.ToolServer
import net.rsprox.mcp.server.tools
import net.rsprox.mcp.session.FakeLauncher
import net.rsprox.mcp.session.loginHeader
import net.rsprox.mcp.session.target
import net.rsprox.proxy.ProxyExtension
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.RootProperty
import java.io.IOException
import java.net.ConnectException
import java.net.ServerSocket
import java.util.ServiceLoader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

class McpExtensionTest {
    private lateinit var switch: EndpointSwitch

    private fun freePort(): Int = ServerSocket(0).use { it.localPort }

    /** The tools over a switch that is off. Closing the server turns the switch off. */
    private fun switched(): ToolServer {
        val gate = Gate()

        // The port is picked once the test server holds its other ports, so that none of them takes it.
        return ToolServer(gate) { sessions ->
            val port = freePort()
            switch = EndpointSwitch(port, gate) { sessions }

            port to AutoCloseable { switch.stopServing() }
        }
    }

    private fun ToolServer.refuses() {
        assertFailsWith<ConnectException> { rpc("ping", "{}") }
    }

    private fun packet(prot: String): RootProperty =
        object : RootProperty {
            override val prot = prot
            override val children = mutableListOf<ChildProperty<*>>()
        }

    @Test
    fun `the GUI finds the extension among the proxy extensions on its classpath`() {
        val found = ServiceLoader.load(ProxyExtension::class.java).map { it.javaClass }

        assertEquals(listOf<Class<*>>(McpExtension::class.java), found)
    }

    @Test
    fun `an endpoint that is off at start-up binds nothing and builds no sessions`() {
        val port = freePort()
        var built = false
        val off =
            EndpointSwitch(port, Gate()) {
                built = true
                error("no session is built for an endpoint that is off")
            }

        off.atStartUp(enabled = false)
        off.stopServing()

        ServerSocket(port).close()
        assertFalse(built)
    }

    @Test
    fun `an endpoint that is on at start-up serves the tools on its port`() {
        switched().use { server ->
            switch.atStartUp(enabled = true)

            assertEquals(
                """{"sessions":[],"targets":["Old School RuneScape","My Server"]}""",
                server.call("session_list"),
            )
        }
    }

    @Test
    fun `turning the endpoint on serves the tools and lists a client that is launched after it`() {
        switched().use { server ->
            server.refuses()

            assertNull(switch.serve())
            server.manager.attach(43701, target(1, "Local"))

            assertEquals(
                """{"sessions":[{"session":"s1","kind":"attached","target":"Local","access":"read",""" +
                    """"state":"attached","proxyPort":43701,"cursor":1}],""" +
                    """"targets":["Old School RuneScape","My Server"]}""",
                server.call("session_list"),
            )
        }
    }

    @Test
    fun `turning the endpoint off refuses a connection and keeps a request that still runs from forking a client`() {
        switched().use { server ->
            val reserved = CountDownLatch(1)
            val off = CountDownLatch(1)
            var forked = false
            server.launcher.onReserve = {
                reserved.countDown()
                off.await()
            }
            server.launcher.launch = { forked = true }
            switch.serve()

            val running = CompletableFuture.runAsync { server.rpc("tools/call", """{"name":"session_start"}""") }
            assertEquals(true, reserved.await(10, TimeUnit.SECONDS))
            switch.stopServing()
            off.countDown()

            assertIs<IOException>(assertFailsWith<ExecutionException> { running.get(10, TimeUnit.SECONDS) }.cause)
            server.refuses()
            assertFalse(forked)
            assertEquals(
                listOf("stopped" to "the MCP endpoint was turned off"),
                server.manager.list().map { it.state to it.reason },
            )
        }
    }

    @Test
    fun `a tool that runs after the endpoint is turned off sends the client nothing`() {
        switched().use { server ->
            val received = CopyOnWriteArrayList<String>()
            switch.serve()
            server.call("session_start", """{"target":"My Server","wait_ms":0}""")
            val plugin = server.hub.dial(FakeLauncher.FIRST_HTTP_PORT)
            plugin.read()
            plugin.serve { op, _ ->
                received += op
                """"ok":{}"""
            }
            server.call("session_start", """{"session":"s1","wait_ms":10000}""")
            val click = tools { server.manager }.single { it.name == "client_click" }
            val arguments = McpDispatcher.MAPPER.createObjectNode().put("x", 10).put("y", 20)

            switch.stopServing()

            assertEquals("closed", assertFailsWith<BridgeError> { click.run(arguments) }.code)
            assertEquals(emptyList(), received)
            assertEquals(emptyList(), server.launcher.killed)
        }
    }

    @Test
    fun `turning the endpoint on again lists the earlier session with its log and what it recorded while off`() {
        switched().use { server ->
            switch.serve()
            val login = checkNotNull(server.manager.attach(43701, target(1, "Local"))).forSession(loginHeader)
            login.onLogin(loginHeader)
            login.onPacketDirection(StreamDirection.SERVER_TO_CLIENT)
            login.onTranscribe(4, packet("REBUILD_NORMAL"))
            val served = server.call("packets_read", """{"session":"s1"}""")

            switch.stopServing()
            login.onTranscribe(5, packet("UPDATE_STAT"))
            assertNull(switch.serve())

            assertEquals(
                listOf(
                    """{"next":3,"head":3,"dropped":0,"timedOut":false,"count":3}""",
                    "1 L0 T0 P CLIENT_ATTACHED proxyPort=43701",
                    "2 L1 T0 P LOGIN revision=235 world=301",
                    "3 L1 T4 S REBUILD_NORMAL [rebuild_normal] ",
                ),
                served.lines(),
            )
            assertEquals(listOf("4 L1 T5 S UPDATE_STAT [update_stat] "), server.rows("""{"session":"s1","after":3}"""))
            assertEquals(4, server.callJson("session_list").get("sessions").single().get("cursor").asInt())
        }
    }

    @Test
    fun `turning the endpoint on while its port is taken says why, builds no sessions and binds nothing`() {
        val holder = McpHttpServer(0, emptyList(), "1.2.3").also { it.start() }
        val port = holder.localPort
        var built = false
        val taken =
            EndpointSwitch(port, Gate()) {
                built = true
                error("no session is built behind a port that is taken")
            }

        try {
            val refusal = checkNotNull(taken.serve())

            // The reason of the operating system sits between the two halves.
            assertEquals(
                "The MCP endpoint is not served: 127.0.0.1:$port cannot be bound",
                refusal.substringBefore(" ("),
            )
            assertEquals(
                "Another rsprox or the standalone MCP server may hold it. Close that one, or set mcp.port " +
                    "in proxy.properties.",
                refusal.substringAfter("). "),
            )
            assertFalse(built)
        } finally {
            holder.close()
        }

        ServerSocket(port).close()
    }

    @Test
    fun `sessions that cannot be built leave no endpoint and free the port, and the next turn builds them`() {
        switched().use { server ->
            val port = server.port
            var fail = true
            val failing =
                EndpointSwitch(port, server.gate) {
                    if (fail) throw IllegalStateException("the bridge hub could not start")

                    server.manager
                }

            assertEquals(
                "The MCP endpoint is not served: it could not start (the bridge hub could not start). " +
                    "The log has the details.",
                failing.serve(),
            )
            ServerSocket(port).close()

            fail = false
            assertNull(failing.serve())
            server.call("session_list")
            failing.stopServing()
        }
    }

    @Test
    fun `turning the endpoint on and off again and again ends in the last state`() {
        switched().use { server ->
            switch.serve()
            switch.serve()
            switch.stopServing()
            switch.stopServing()
            switch.serve()
            server.call("session_list")

            val clicks =
                (1..8).map { thread ->
                    CompletableFuture.runAsync {
                        repeat(25) { click -> if ((thread + click) % 2 == 0) switch.serve() else switch.stopServing() }
                    }
                }
            clicks.forEach { it.get(30, TimeUnit.SECONDS) }

            switch.stopServing()
            server.refuses()

            assertNull(switch.serve())
            server.call("session_list")
        }
    }
}
