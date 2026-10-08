package net.rsprox.mcp

import net.rsprox.mcp.server.McpHttpServer
import net.rsprox.mcp.server.ToolServer
import net.rsprox.proxy.ProxyExtension
import java.net.ServerSocket
import java.util.ServiceLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class McpExtensionTest {
    private fun freePort(): Int = ServerSocket(0).use { it.localPort }

    @Test
    fun `the GUI finds the extension among the proxy extensions on its classpath`() {
        val found = ServiceLoader.load(ProxyExtension::class.java).map { it.javaClass }

        assertEquals(listOf<Class<*>>(McpExtension::class.java), found)
    }

    @Test
    fun `a port that is taken leaves no endpoint, builds no sessions and throws nothing`() {
        val holder = McpHttpServer(0, emptyList(), "1.2.3").also { it.start() }

        try {
            assertNull(serveOrNull(holder.localPort) { error("no session is built behind a port that is taken") })
        } finally {
            holder.close()
        }
    }

    @Test
    fun `sessions that cannot be built leave no endpoint, free the port and throw nothing`() {
        val port = freePort()

        assertNull(serveOrNull(port) { throw IllegalStateException("the bridge hub could not start") })

        ServerSocket(port).close()
    }

    @Test
    fun `a free port serves the tools over the sessions that were built`() {
        var port = 0

        // The port is picked once the test server holds its other ports, so that none of them takes it.
        val served =
            ToolServer { sessions ->
                port = freePort()
                checkNotNull(serveOrNull(port) { sessions })
            }

        served.use { server ->
            assertEquals(port, server.port)
            assertEquals(
                """{"sessions":[],"targets":["Old School RuneScape","My Server"]}""",
                server.call("session_list"),
            )
        }
    }
}
