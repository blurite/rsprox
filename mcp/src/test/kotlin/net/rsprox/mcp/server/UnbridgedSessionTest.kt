package net.rsprox.mcp.server

import net.rsprox.mcp.session.FakeLauncher
import net.rsprox.mcp.session.loginHeader
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnbridgedSessionTest {
    private val server = ToolServer()

    @AfterTest
    fun cleanUp() {
        server.close()
    }

    @Test
    fun `a client launched without the plugin is unbridged at once, unexpected by the hub, and keeps its packets`() {
        server.launcher.bridged = false

        val started = server.callJson("session_start", """{"target":"My Server","wait_ms":10000}""")

        assertEquals("unbridged", started.get("state").asText())
        assertEquals(
            "no session expects httpPort 43650",
            server.hub.dial(FakeLauncher.FIRST_HTTP_PORT).read()?.get("reject")?.asText(),
        )
        assertEquals(
            "client_state is not available for session s1: its client was launched without the bridge plugin, " +
                "which mcp.plugin in proxy.properties, or --no-plugin, turns off, so only its packets can be read",
            server.error("client_state", "{}"),
        )

        server.launcher.monitor.forSession(loginHeader).onLogin(loginHeader)

        assertEquals(
            listOf(
                "1 L0 T0 P CLIENT_LAUNCHED generation=1 proxyPort=43751 httpPort=43650 plugin=none",
                "2 L1 T0 P LOGIN revision=235 world=301",
            ),
            server.rows("{}"),
        )
    }

    @Test
    fun `an unbridged client that is closed stops its session, and session_stop kills one that still runs`() {
        server.launcher.bridged = false
        server.call("session_start", """{"target":"My Server","wait_ms":0}""")
        server.call("session_start", """{"target":"My Server","wait_ms":0}""")

        server.manager.detach(FakeLauncher.FIRST_PROXY_PORT)
        server.call("session_stop", """{"session":"s2"}""")

        assertEquals(
            listOf("stopped: the client was closed", "stopped: stopped by caller"),
            server.manager.list().map { "${it.state}: ${it.reason}" },
        )
        assertEquals(listOf(FakeLauncher.FIRST_PROXY_PORT, FakeLauncher.FIRST_PROXY_PORT + 1), server.launcher.killed)
    }
}
