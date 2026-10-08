package net.rsprox.mcp

import net.rsprox.mcp.bridge.TestHub
import net.rsprox.mcp.packets.TapSettingSetStore
import net.rsprox.mcp.server.McpHttpServer
import net.rsprox.mcp.session.FakeLauncher
import net.rsprox.mcp.session.SessionManager
import net.rsprox.proxy.ProxyExtension
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.ServiceLoader
import kotlin.test.Test
import kotlin.test.assertContains
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
        val port = freePort()
        val fixture = TestHub()
        val endpoint = serveOrNull(port) { SessionManager(FakeLauncher(), TapSettingSetStore, fixture.hub) }

        try {
            val request =
                HttpRequest
                    .newBuilder(URI("http://127.0.0.1:$port/mcp"))
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            """{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"session_list"}}""",
                        ),
                    ).build()

            val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

            assertContains(response.body(), """{\"sessions\":[],\"targets\":[\"Old School RuneScape\",\"My Server\"]}""")
        } finally {
            endpoint?.close()
            fixture.close()
        }
    }
}
