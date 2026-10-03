package net.rsprox.mcp.server

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.michaelbull.logging.InlineLogger
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import net.rsprox.mcp.bridge.BridgeError
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.Executors

/** A failure the agent can act on. Becomes a tool result with `isError: true` and the message as its text. */
public class ToolError(
    message: String,
) : Exception(message)

public sealed interface ToolResult {
    /** One text block holding [value] as compact JSON. */
    public data class Json(
        val value: Any,
    ) : ToolResult

    /** One text block holding [text] as is. */
    public data class Text(
        val text: String,
    ) : ToolResult

    /** A PNG image block followed by one text block holding [meta] as compact JSON. */
    public data class Image(
        val pngBase64: String,
        val meta: Any,
    ) : ToolResult
}

public class Tool(
    public val name: String,
    public val description: String,
    public val inputSchema: ObjectNode,
    /** Receives arguments that already satisfy [inputSchema]. */
    public val run: (args: ObjectNode) -> ToolResult,
)

internal class HttpReply(
    val status: Int,
    val body: String?,
)

private class RpcError(
    val code: Int,
    message: String,
) : Exception(message)

/** The MCP protocol without the socket: one HTTP request in, one reply out. */
internal class McpDispatcher(
    tools: List<Tool>,
    private val version: String,
) {
    private val tools = tools.associateBy { it.name }

    fun handle(
        httpMethod: String,
        origin: String?,
        body: String,
    ): HttpReply {
        // A browser page on another site could otherwise reach this loopback server through DNS rebinding.
        if (origin != null && !isLocalOrigin(origin)) return HttpReply(403, null)
        if (httpMethod != "POST") return HttpReply(405, null)
        val root =
            try {
                MAPPER.readTree(body)
            } catch (e: JsonProcessingException) {
                return HttpReply(400, error(null, PARSE_ERROR, "Parse error"))
            }
        if (root == null || !root.isObject) {
            return HttpReply(400, error(null, INVALID_REQUEST, "Expected one JSON-RPC object; batching is unsupported"))
        }
        val id = root.get("id")?.takeUnless { it.isNull }
        val method = root.get("method")?.takeIf { it.isTextual }?.asText()
        if (method == null) {
            val isResponse = root.has("result") || root.has("error")
            if (isResponse) return HttpReply(202, null)
            return HttpReply(400, error(id, INVALID_REQUEST, "Missing method"))
        }
        if (id == null) return HttpReply(202, null)
        val result =
            try {
                call(method, root.get("params"))
            } catch (e: RpcError) {
                return HttpReply(200, error(id, e.code, e.message.orEmpty()))
            }
        val reply = MAPPER.createObjectNode()
        reply.put("jsonrpc", "2.0")
        reply.set<JsonNode>("id", id)
        reply.set<JsonNode>("result", result)
        return HttpReply(200, MAPPER.writeValueAsString(reply))
    }

    private fun call(
        method: String,
        params: JsonNode?,
    ): JsonNode =
        when (method) {
            "initialize" -> initialize(params)
            "ping" -> MAPPER.createObjectNode()
            "tools/list" -> listTools()
            "tools/call" -> callTool(params)
            else -> throw RpcError(METHOD_NOT_FOUND, "Method not found: $method")
        }

    private fun initialize(params: JsonNode?): JsonNode {
        val requested = params?.get("protocolVersion")?.asText()
        val result = MAPPER.createObjectNode()
        result.put("protocolVersion", if (requested in SUPPORTED) requested else SUPPORTED.last())
        result.putObject("capabilities").putObject("tools")
        result
            .putObject("serverInfo")
            .put("name", "rsprox")
            .put("version", version)
        return result
    }

    private fun listTools(): JsonNode {
        val result = MAPPER.createObjectNode()
        val list = result.putArray("tools")
        for (tool in tools.values) {
            list
                .addObject()
                .put("name", tool.name)
                .put("description", tool.description)
                .set<JsonNode>("inputSchema", tool.inputSchema)
        }
        return result
    }

    private fun callTool(params: JsonNode?): JsonNode {
        val name =
            params?.get("name")?.takeIf { it.isTextual }?.asText()
                ?: throw RpcError(INVALID_PARAMS, "tools/call requires params.name")
        val tool = tools[name] ?: throw RpcError(INVALID_PARAMS, "Unknown tool: $name")
        val arguments = params.get("arguments")?.takeUnless { it.isNull } ?: MAPPER.createObjectNode()
        if (arguments !is ObjectNode) throw RpcError(INVALID_PARAMS, "params.arguments must be an object")
        val result = MAPPER.createObjectNode()
        val content =
            try {
                checkArguments(tool, arguments)
                val content = render(tool.run(arguments))
                result.put("isError", false)
                content
            } catch (e: ToolError) {
                result.put("isError", true)
                listOf(textBlock(e.message.orEmpty()))
            } catch (e: BridgeError) {
                result.put("isError", true)
                listOf(textBlock("${e.code}: ${e.message}"))
            } catch (t: Throwable) {
                logger.error(t) { "Tool $name failed" }
                result.put("isError", true)
                listOf(textBlock("internal: $t"))
            }
        result.putArray("content").addAll(content)
        return result
    }

    private fun render(result: ToolResult): List<ObjectNode> =
        when (result) {
            is ToolResult.Json -> listOf(textBlock(MAPPER.writeValueAsString(result.value)))
            is ToolResult.Text -> listOf(textBlock(result.text))
            is ToolResult.Image ->
                listOf(
                    MAPPER
                        .createObjectNode()
                        .put("type", "image")
                        .put("data", result.pngBase64)
                        .put("mimeType", "image/png"),
                    textBlock(MAPPER.writeValueAsString(result.meta)),
                )
        }

    private fun textBlock(text: String): ObjectNode =
        MAPPER
            .createObjectNode()
            .put("type", "text")
            .put("text", text)

    /**
     * Checks the subset of JSON Schema the tool table uses: `required`, per-property `type`, `enum`,
     * `minimum`, `maximum`, and `items.type` for arrays. Unknown keys are rejected so a misspelt
     * argument fails loudly instead of being ignored.
     */
    private fun checkArguments(
        tool: Tool,
        arguments: ObjectNode,
    ) {
        val properties = tool.inputSchema.get("properties") ?: MAPPER.createObjectNode()
        for (key in arguments.fieldNames()) {
            if (!properties.has(key)) {
                val allowed = properties.fieldNames().asSequence().joinToString(", ")
                throw ToolError("${tool.name}: unknown argument '$key'. Allowed: $allowed")
            }
        }
        for (required in tool.inputSchema.get("required") ?: emptyList<JsonNode>()) {
            if (!arguments.hasNonNull(required.asText())) {
                throw ToolError("${tool.name}: missing required argument '${required.asText()}'")
            }
        }
        for ((key, value) in arguments.fields()) {
            val schema = properties.get(key)
            val type = schema.get("type").asText()
            if (!value.hasType(type)) throw ToolError("${tool.name}: argument '$key' must be of type $type")
            val itemType = schema.get("items")?.get("type")?.asText()
            if (itemType != null && value.any { !it.hasType(itemType) }) {
                throw ToolError("${tool.name}: every item of '$key' must be of type $itemType")
            }
            val allowed = schema.get("enum")
            if (allowed != null && allowed.none { it == value }) {
                val values = allowed.joinToString(", ") { it.asText() }
                throw ToolError("${tool.name}: argument '$key' must be one of $values")
            }
            val minimum = schema.get("minimum")?.asLong()
            if (minimum != null && value.asLong() < minimum) {
                throw ToolError("${tool.name}: argument '$key' must be at least $minimum")
            }
            val maximum = schema.get("maximum")?.asLong()
            if (maximum != null && value.asLong() > maximum) {
                throw ToolError("${tool.name}: argument '$key' must be at most $maximum")
            }
        }
    }

    private fun JsonNode.hasType(type: String): Boolean =
        when (type) {
            "string" -> isTextual
            "integer" -> isIntegralNumber
            "number" -> isNumber
            "boolean" -> isBoolean
            "array" -> isArray
            "object" -> isObject
            else -> error("Unsupported schema type: $type")
        }

    private fun isLocalOrigin(origin: String): Boolean {
        val host =
            try {
                URI(origin).host
            } catch (e: URISyntaxException) {
                null
            }
        return host in LOCAL_HOSTS
    }

    private fun error(
        id: JsonNode?,
        code: Int,
        message: String,
    ): String {
        val reply = MAPPER.createObjectNode()
        reply.put("jsonrpc", "2.0")
        reply.set<JsonNode>("id", id ?: MAPPER.nullNode())
        reply
            .putObject("error")
            .put("code", code)
            .put("message", message)
        return MAPPER.writeValueAsString(reply)
    }

    internal companion object {
        internal val MAPPER: ObjectMapper = jacksonObjectMapper()
        private val logger = InlineLogger()
        private val SUPPORTED = listOf("2025-03-26", "2025-06-18", "2025-11-25")
        private val LOCAL_HOSTS = setOf("localhost", "127.0.0.1", "[::1]")
        private const val PARSE_ERROR = -32700
        private const val INVALID_REQUEST = -32600
        private const val METHOD_NOT_FOUND = -32601
        private const val INVALID_PARAMS = -32602
    }
}

/**
 * Serves MCP over plain request/response HTTP on loopback. Every request runs on its own pool thread,
 * so a tool may block for as long as it needs.
 */
public class McpHttpServer(
    private val port: Int,
    tools: List<Tool>,
    version: String,
) : AutoCloseable {
    private val dispatcher = McpDispatcher(tools, version)
    private var server: HttpServer? = null

    /** Throws [java.net.BindException] when the port is taken, which is how a second instance is refused. */
    public fun start() {
        val server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0)
        server.executor =
            Executors.newCachedThreadPool { runnable ->
                Thread(runnable, "mcp-http").apply { isDaemon = true }
            }
        server.createContext(PATH, ::handle)
        server.start()
        this.server = server
    }

    private fun handle(exchange: HttpExchange) {
        try {
            if (exchange.requestURI.path != PATH) {
                exchange.sendResponseHeaders(404, -1)
                return
            }
            val body = exchange.requestBody.readAllBytes().toString(Charsets.UTF_8)
            val reply =
                dispatcher.handle(
                    exchange.requestMethod,
                    exchange.requestHeaders.getFirst("Origin"),
                    body,
                )
            if (reply.status == 405) exchange.responseHeaders.add("Allow", "POST")
            if (reply.body == null) {
                exchange.sendResponseHeaders(reply.status, -1)
                return
            }
            val bytes = reply.body.toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(reply.status, bytes.size.toLong())
            exchange.responseBody.write(bytes)
        } finally {
            exchange.close()
        }
    }

    override fun close() {
        server?.stop(0)
    }

    private companion object {
        private const val PATH = "/mcp"
    }
}
