package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class McpDispatcherTest {
    private val mapper = McpDispatcher.MAPPER

    private fun schema(json: String): ObjectNode = mapper.readTree(json) as ObjectNode

    private val echo =
        Tool(
            name = "echo",
            description = "Returns its text argument",
            inputSchema =
                schema(
                    """{"type":"object","properties":{"text":{"type":"string"},"times":{"type":"integer","minimum":1},
                    "mode":{"type":"string","enum":["a","b"]},"tags":{"type":"array","items":{"type":"string"}}},
                    "required":["text"]}""",
                ),
        ) { args -> ToolResult.Text(args.get("text").asText()) }

    private val json =
        Tool("json", "Returns a map", schema("""{"type":"object","properties":{}}""")) {
            ToolResult.Json(mapOf("answer" to 42))
        }

    private val refuse =
        Tool("refuse", "Always fails", schema("""{"type":"object","properties":{}}""")) {
            throw ToolError("no session s9")
        }

    private val crash =
        Tool("crash", "Always throws", schema("""{"type":"object","properties":{}}""")) {
            throw IllegalStateException("boom")
        }

    private val dispatcher = McpDispatcher(listOf(echo, json, refuse, crash), "1.2.3")

    private fun post(
        body: String,
        origin: String? = null,
    ): HttpReply = dispatcher.handle("POST", origin, body)

    private fun result(body: String): JsonNode {
        val reply = post(body)
        assertEquals(200, reply.status)
        val node = mapper.readTree(reply.body)
        assertEquals("2.0", node.get("jsonrpc").asText())
        assertNull(node.get("error"), "unexpected error: ${reply.body}")
        return node.get("result")
    }

    private fun error(body: String): JsonNode = mapper.readTree(post(body).body).get("error")

    private fun callTool(
        name: String,
        arguments: String = "{}",
    ): JsonNode =
        result("""{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"$name","arguments":$arguments}}""")

    @Test
    fun `initialize echoes a supported protocol version and names the server`() {
        val result =
            result("""{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18"}}""")
        assertEquals("2025-06-18", result.get("protocolVersion").asText())
        assertTrue(result.get("capabilities").has("tools"))
        assertEquals("rsprox", result.get("serverInfo").get("name").asText())
        assertEquals("1.2.3", result.get("serverInfo").get("version").asText())
    }

    @Test
    fun `initialize answers an unknown protocol version with the newest supported one`() {
        val result =
            result("""{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"1999-01-01"}}""")
        assertEquals("2025-11-25", result.get("protocolVersion").asText())
    }

    @Test
    fun `the response carries the request id unchanged`() {
        val reply = mapper.readTree(post("""{"jsonrpc":"2.0","id":"abc","method":"ping"}""").body)
        assertEquals("abc", reply.get("id").asText())
        assertEquals(0, reply.get("result").size())
    }

    @Test
    fun `tools list returns every tool with its schema`() {
        val tools = result("""{"jsonrpc":"2.0","id":1,"method":"tools/list"}""").get("tools")
        assertEquals(listOf("echo", "json", "refuse", "crash"), tools.map { it.get("name").asText() })
        assertEquals("Returns its text argument", tools[0].get("description").asText())
        assertEquals(echo.inputSchema, tools[0].get("inputSchema"))
    }

    @Test
    fun `tools call returns the tool text as one content block`() {
        val result = callTool("echo", """{"text":"hello"}""")
        assertFalse(result.get("isError").asBoolean())
        assertEquals(1, result.get("content").size())
        assertEquals("text", result.get("content")[0].get("type").asText())
        assertEquals("hello", result.get("content")[0].get("text").asText())
    }

    @Test
    fun `a json tool result is rendered as compact json text`() {
        assertEquals("""{"answer":42}""", callTool("json").get("content")[0].get("text").asText())
    }

    @Test
    fun `a ToolError becomes an error result carrying its message`() {
        val result = callTool("refuse")
        assertTrue(result.get("isError").asBoolean())
        assertEquals("no session s9", result.get("content")[0].get("text").asText())
    }

    @Test
    fun `an unexpected exception becomes an internal error result`() {
        val result = callTool("crash")
        assertTrue(result.get("isError").asBoolean())
        assertTrue(result.get("content")[0].get("text").asText().startsWith("internal: "))
    }

    @Test
    fun `arguments that break the schema are refused before the tool runs`() {
        val cases =
            mapOf(
                """{}""" to "missing required argument 'text'",
                """{"text":5}""" to "'text' must be of type string",
                """{"text":"a","txt":"b"}""" to "unknown argument 'txt'",
                """{"text":"a","times":0}""" to "'times' must be at least 1",
                """{"text":"a","times":1.5}""" to "'times' must be of type integer",
                """{"text":"a","mode":"c"}""" to "'mode' must be one of a, b",
                """{"text":"a","tags":["x",1]}""" to "every item of 'tags' must be of type string",
            )
        for ((arguments, expected) in cases) {
            val result = callTool("echo", arguments)
            assertTrue(result.get("isError").asBoolean(), arguments)
            val text = result.get("content")[0].get("text").asText()
            assertTrue(text.contains(expected), "$arguments gave: $text")
        }
    }

    @Test
    fun `tools call without arguments runs the tool with an empty object`() {
        val result = result("""{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"json"}}""")
        assertFalse(result.get("isError").asBoolean())
    }

    @Test
    fun `an unknown tool is an invalid params error`() {
        val error = error("""{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"nope"}}""")
        assertEquals(-32602, error.get("code").asInt())
    }

    @Test
    fun `an unknown method is a method not found error`() {
        val reply = post("""{"jsonrpc":"2.0","id":7,"method":"resources/list"}""")
        assertEquals(200, reply.status)
        val node = mapper.readTree(reply.body)
        assertEquals(7, node.get("id").asInt())
        assertEquals(-32601, node.get("error").get("code").asInt())
        assertNull(node.get("result"))
    }

    @Test
    fun `a notification is accepted with no body`() {
        val reply = post("""{"jsonrpc":"2.0","method":"notifications/initialized"}""")
        assertEquals(202, reply.status)
        assertNull(reply.body)
    }

    @Test
    fun `a non-local origin is forbidden and a local one is served`() {
        val ping = """{"jsonrpc":"2.0","id":1,"method":"ping"}"""
        assertEquals(403, post(ping, origin = "https://example.com").status)
        assertEquals(403, post(ping, origin = "null").status)
        assertEquals(200, post(ping, origin = "http://localhost:6274").status)
        assertEquals(200, post(ping, origin = "http://127.0.0.1:6274").status)
    }

    @Test
    fun `a batch or malformed body is rejected`() {
        val batch = post("""[{"jsonrpc":"2.0","id":1,"method":"ping"}]""")
        assertEquals(400, batch.status)
        assertEquals(-32600, mapper.readTree(batch.body).get("error").get("code").asInt())
        val malformed = post("{not json")
        assertEquals(400, malformed.status)
        assertEquals(-32700, mapper.readTree(malformed.body).get("error").get("code").asInt())
    }

    @Test
    fun `only POST is allowed`() {
        assertEquals(405, dispatcher.handle("GET", null, "").status)
        assertEquals(405, dispatcher.handle("DELETE", null, "").status)
    }
}
