package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
import net.rsprox.mcp.bridge.Gate
import net.rsprox.mcp.bridge.TestHub
import net.rsprox.mcp.packets.TapSettingSetStore
import net.rsprox.mcp.session.FakeLauncher
import net.rsprox.mcp.session.SessionManager
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The tools over a fake launcher and a started bridge hub, called over HTTP the way an MCP client calls them.
 * [serve] starts the endpoint over the sessions and returns its port with what stops it. The port is a
 * free one unless a test says otherwise. The sessions act on clients through [gate].
 */
internal class ToolServer(
    val gate: Gate = Gate(),
    serve: (SessionManager) -> Pair<Int, AutoCloseable> = { sessions ->
        val server = McpHttpServer(0, tools { sessions }, "1.2.3").also { it.start() }

        server.localPort to server
    },
) : AutoCloseable {
    val launcher = FakeLauncher()
    val hub = TestHub()
    val manager = SessionManager(launcher, TapSettingSetStore, hub.hub, gate = gate)
    private val served = serve(manager)
    private val http = HttpClient.newHttpClient()

    val port: Int get() = served.first

    /** Sends one JSON-RPC request and returns its `result`. */
    fun rpc(
        method: String,
        params: String,
    ): JsonNode {
        val body = """{"jsonrpc":"2.0","id":1,"method":"$method","params":$params}"""
        val request =
            HttpRequest
                .newBuilder(URI("http://127.0.0.1:$port/mcp"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()

        val response = http.send(request, HttpResponse.BodyHandlers.ofString())

        return McpDispatcher.MAPPER.readTree(response.body()).get("result")
    }

    /** Calls a tool that must succeed and returns its text. */
    fun call(
        tool: String,
        arguments: String = "{}",
    ): String {
        val (text, isError) = toolResult(tool, arguments)
        assertFalse(isError, text)

        return text
    }

    /** Calls a tool that must succeed and returns its text as JSON. */
    fun callJson(
        tool: String,
        arguments: String = "{}",
    ): JsonNode = McpDispatcher.MAPPER.readTree(call(tool, arguments))

    /** Calls a tool that must fail and returns its message. */
    fun error(
        tool: String,
        arguments: String,
    ): String {
        val (text, isError) = toolResult(tool, arguments)
        assertTrue(isError, text)

        return text
    }

    /** Reads packets and returns their lines, without the line of meta. */
    fun rows(arguments: String): List<String> = call("packets_read", arguments).lines().drop(1)

    override fun close() {
        served.second.close()
        hub.close()
    }

    private fun toolResult(
        tool: String,
        arguments: String,
    ): Pair<String, Boolean> {
        val result = rpc("tools/call", """{"name":"$tool","arguments":$arguments}""")

        return result.get("content").single().get("text").asText() to result.get("isError").asBoolean()
    }
}
