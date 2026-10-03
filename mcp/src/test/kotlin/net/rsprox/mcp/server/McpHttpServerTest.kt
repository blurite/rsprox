package net.rsprox.mcp.server

import java.net.BindException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class McpHttpServerTest {
    private val echo =
        Tool(
            name = "echo",
            description = "Returns its text argument",
            inputSchema =
                McpDispatcher.MAPPER
                    .createObjectNode()
                    .put("type", "object")
                    .also { it.putObject("properties").putObject("text").put("type", "string") },
        ) { args -> ToolResult.Text(args.get("text").asText()) }

    private val server = McpHttpServer(0, listOf(echo), "1.2.3").also { it.start() }
    private val client = HttpClient.newHttpClient()

    @AfterTest
    fun cleanUp() {
        server.close()
    }

    private fun request(path: String = "/mcp"): HttpRequest.Builder =
        HttpRequest.newBuilder(URI("http://127.0.0.1:${server.localPort}$path"))

    private fun send(request: HttpRequest.Builder): HttpResponse<String> =
        client.send(request.build(), HttpResponse.BodyHandlers.ofString())

    private fun post(
        body: String,
        origin: String? = null,
    ): HttpResponse<String> {
        val request = request().POST(HttpRequest.BodyPublishers.ofString(body))

        if (origin != null) request.header("Origin", origin)

        return send(request)
    }

    @Test
    fun `a tool call is answered as json over http`() {
        val response =
            post(
                """{"jsonrpc":"2.0","id":7,"method":"tools/call",""" +
                    """"params":{"name":"echo","arguments":{"text":"héllo"}}}""",
            )

        assertEquals(200, response.statusCode())
        assertEquals("application/json", response.headers().firstValue("Content-Type").orElse(null))
        assertEquals(
            """{"jsonrpc":"2.0","id":7,"result":{"isError":false,"content":[{"type":"text","text":"héllo"}]}}""",
            response.body(),
        )
    }

    @Test
    fun `a request from a page on another site is forbidden and one from a local page is served`() {
        val ping = """{"jsonrpc":"2.0","id":1,"method":"ping"}"""

        val foreign = post(ping, origin = "https://example.com")
        assertEquals(403, foreign.statusCode())
        assertEquals("", foreign.body())

        val local = post(ping, origin = "http://localhost:6274")
        assertEquals(200, local.statusCode())
        assertEquals("""{"jsonrpc":"2.0","id":1,"result":{}}""", local.body())
    }

    @Test
    fun `a notification is accepted with no body`() {
        val response = post("""{"jsonrpc":"2.0","method":"notifications/initialized"}""")

        assertEquals(202, response.statusCode())
        assertEquals("", response.body())
    }

    @Test
    fun `a GET is refused and told that POST is allowed`() {
        val response = send(request().GET())

        assertEquals(405, response.statusCode())
        assertEquals("POST", response.headers().firstValue("Allow").orElse(null))
        assertEquals("", response.body())
    }

    @Test
    fun `a body that is not json is a parse error`() {
        val response = post("{not json")

        assertEquals(400, response.statusCode())
        assertEquals(
            """{"jsonrpc":"2.0","id":null,"error":{"code":-32700,"message":"Parse error"}}""",
            response.body(),
        )
    }

    @Test
    fun `a batch is an invalid request`() {
        val response = post("""[{"jsonrpc":"2.0","id":1,"method":"ping"}]""")

        assertEquals(400, response.statusCode())
        assertEquals(
            """{"jsonrpc":"2.0","id":null,"error":{"code":-32600,""" +
                """"message":"Expected one JSON-RPC object; batching is unsupported"}}""",
            response.body(),
        )
    }

    @Test
    fun `a path below the endpoint is not found`() {
        val response = send(request("/mcp/other").POST(HttpRequest.BodyPublishers.ofString("{}")))

        assertEquals(404, response.statusCode())
    }

    @Test
    fun `a second server on the same port is refused`() {
        val second = McpHttpServer(server.localPort, listOf(echo), "1.2.3")

        assertFailsWith<BindException> { second.start() }
    }
}
