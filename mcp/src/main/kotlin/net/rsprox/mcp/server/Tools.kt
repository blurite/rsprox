package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import net.rsprox.mcp.bridge.BridgeError
import net.rsprox.mcp.packets.Cursor
import net.rsprox.mcp.packets.Origin
import net.rsprox.mcp.packets.PacketPage
import net.rsprox.mcp.packets.PacketQuery
import net.rsprox.mcp.session.SessionManager

/**
 * The tool table. [sessions] is resolved on every call and may throw a [ToolError] while the proxy
 * is still starting.
 */
public fun tools(sessions: () -> SessionManager): List<Tool> =
    listOf(
        Tool(
            name = "session_start",
            description =
                "Launch a RuneLite client through rsprox, or relaunch a stopped session. " +
                    "Blocks until the in-client bridge connects and then reports `state` as `connected`, " +
                    "after which the client_* tools work. If `wait_ms` elapses first, `state` is `launching`; " +
                    "call it again with the same `session` to keep waiting. With no arguments it starts a new " +
                    "session on the first custom target. Pass `session` to relaunch a stopped session on fresh " +
                    "ports; its packet log and cursor continue. Calling it for a session that is already " +
                    "running launches nothing.",
            inputSchema =
                schema(
                    "target" to string("Proxy target name, as listed by session_list. Example: \"My Server\"."),
                    "session" to string("Existing session id, such as \"s1\"."),
                    "wait_ms" to integer("How long to wait for the session to connect. Default 180000.", 0, 600_000),
                ),
        ) { args ->
            ToolResult.Json(
                sessions().start(
                    target = args.text("target"),
                    session = args.text("session"),
                    waitMs = args.long("wait_ms") ?: SessionManager.DEFAULT_WAIT_MS,
                ),
            )
        },
        Tool(
            name = "session_stop",
            description = "Kill the client of a session. The session and its packets stay readable.",
            inputSchema = schema("session" to SESSION),
        ) { args ->
            ToolResult.Json(sessions().stop(args.text("session")))
        },
        Tool(
            name = "session_list",
            description = "List every session with its state, ports, login and packet cursor, plus the target names.",
            inputSchema = schema(),
        ) {
            val manager = sessions()
            ToolResult.Json(mapOf("sessions" to manager.list(), "targets" to manager.targets()))
        },
        Tool(
            name = "packets_read",
            description =
                "Read decoded packets of a session after a cursor, unfiltered, in both directions. " +
                    "With `wait_ms` it blocks until the first match, which makes it the way to wait for a packet. " +
                    "Line 1 of the result is JSON: pass `next` as `after` on the following call; `dropped` counts " +
                    "records that were evicted before they could be read; `timedOut` is true when a wait " +
                    "elapsed with no match. Each further line is one packet: " +
                    "`<seq> L<login> T<tick> <C|S|P> <PROT> <text>`, where C is client to server, S is server " +
                    "to client and P is an rsprox marker (CLIENT_LAUNCHED, CLIENT_CONNECTED, CLIENT_EXITED, " +
                    "LOGIN, LOGOUT). Continuation lines of one packet are indented.",
            inputSchema =
                schema(
                    "session" to SESSION,
                    "after" to integer("Cursor to read after. Default 0, the start of the log.", 0),
                    "prots" to stringArray("Prot names to keep, such as [\"IF_SETTEXT\"]. Default: all."),
                    "origin" to string("Keep only one origin.", "client", "server", "proxy"),
                    "contains" to string("Case-insensitive substring the packet text must contain."),
                    "wait_ms" to integer("How long to block for a first match. Default 0.", 0, 120_000),
                    "limit" to integer("Maximum number of packets to return. Default 200.", 1, 1000),
                ),
        ) { args ->
            val query =
                PacketQuery(
                    after = Cursor(args.long("after") ?: 0),
                    prots = args.get("prots")?.map { it.asText().uppercase() }?.toSet().orEmpty(),
                    origin = args.text("origin")?.let { Origin.valueOf(it.uppercase()) },
                    contains = args.text("contains"),
                    limit = args.long("limit")?.toInt() ?: 200,
                )
            val page = sessions().resolve(args.text("session")).packets.read(query, args.long("wait_ms") ?: 0)
            ToolResult.Text(render(page))
        },
        clientTool(
            name = "client_state",
            description =
                "Read what the client is doing: `gameState` (such as LOGIN_SCREEN or LOGGED_IN), `tick`, " +
                    "`canvas` as [width, height], `world`, `player` with its name and tile (null unless logged " +
                    "in), and `menu` with the entries a right-click would show at the current mouse position.",
            schema = schema("session" to SESSION),
            sessions = sessions,
        ),
        clientTool(
            name = "client_screenshot",
            description =
                "Capture the game canvas as a PNG. The image has the size of the canvas, so a pixel " +
                    "position in it is the `x`,`y` to pass to client_click. The text block holds `width` " +
                    "and `height`.",
            schema = schema("session" to SESSION),
            sessions = sessions,
            result = { ok -> ToolResult.Image(ok.remove("png")?.asText().orEmpty(), ok) },
        ),
        clientTool(
            name = "client_widgets",
            description =
                "List the interface widgets that have text, a name or actions. Each has `id` " +
                    "(\"<group>:<child>\", or \"<group>:<child>[<index>]\" for a dynamic child), `text`, `name`, " +
                    "`actions`, `bounds` as [x, y, width, height], `click` as the [x, y] at its centre, `type` " +
                    "and `hidden`. `roots` lists the groups of the top-level interfaces. `truncated` is true " +
                    "when `limit` cut the list short; narrow it with `group` or `text`.",
            schema =
                schema(
                    "session" to SESSION,
                    "group" to integer("Keep only widgets of this interface group, such as 558.", 0),
                    "text" to string("Case-insensitive substring to find in the text, name or actions."),
                    "hidden" to boolean("Also list hidden widgets. Default false."),
                    "limit" to integer("Maximum number of widgets to return. Default 200.", 1, 2000),
                ),
            sessions = sessions,
        ),
        clientTool(
            name = "client_vars",
            description =
                "Read client variables by id. The result maps each requested id to its value under " +
                    "`varps`, `varbits`, `varcInts` and `varcStrs`.",
            schema =
                schema(
                    "session" to SESSION,
                    "varps" to integerArray("Player variable ids, such as [1055]."),
                    "varbits" to integerArray("Varbit ids, such as [8119]."),
                    "varcInts" to integerArray("Client integer variable ids."),
                    "varcStrs" to integerArray("Client string variable ids."),
                ),
            sessions = sessions,
        ),
    )

/**
 * The shape every client_* tool shares. The plugin's answer is the tool result, plus the packet cursor
 * taken before the call, so everything the action caused has a sequence number above it.
 */
private fun clientTool(
    name: String,
    description: String,
    schema: ObjectNode,
    sessions: () -> SessionManager,
    timeoutMs: (ObjectNode) -> Long = { CLIENT_CALL_TIMEOUT_MS },
    result: (ObjectNode) -> ToolResult = ToolResult::Json,
): Tool =
    Tool(name, "$description $CURSOR_NOTE", schema) { args ->
        val session = sessions().resolve(args.text("session"))
        val cursor = session.packets.head().seq
        val forwarded = args.deepCopy().without<ObjectNode>("session")
        val ok = session.requireLink().call(name.removePrefix("client_"), forwarded, timeoutMs(args))
        if (ok !is ObjectNode) throw BridgeError("internal", "the client answered $name with $ok")
        result(ok.put("cursor", cursor))
    }

private const val CLIENT_CALL_TIMEOUT_MS = 10_000L

private const val CURSOR_NOTE =
    "The result carries `cursor`, the packet cursor taken just before the call: pass it as `after` to " +
        "packets_read to see only the packets from this call onwards."

private val SESSION: ObjectNode = string("Session id, such as \"s1\". May be omitted while only one session exists.")

private fun render(page: PacketPage): String {
    val meta =
        linkedMapOf(
            "next" to page.next.seq,
            "head" to page.head.seq,
            "dropped" to page.dropped,
            "timedOut" to page.timedOut,
            "count" to page.packets.size,
        )
    val out = StringBuilder(McpDispatcher.MAPPER.writeValueAsString(meta))
    for (packet in page.packets) {
        out
            .append('\n')
            .append(packet.seq)
            .append(" L")
            .append(packet.login)
            .append(" T")
            .append(packet.cycle)
            .append(' ')
            .append(packet.origin.letter)
            .append(' ')
            .append(packet.prot)
            .append(' ')
            .append(packet.text.replace("\n", "\n    "))
    }
    return out.toString()
}

private fun schema(
    vararg properties: Pair<String, ObjectNode>,
    required: List<String> = emptyList(),
): ObjectNode {
    val schema = McpDispatcher.MAPPER.createObjectNode()
    schema.put("type", "object")
    val node = schema.putObject("properties")
    for ((name, property) in properties) {
        node.set<JsonNode>(name, property)
    }
    if (required.isNotEmpty()) {
        val names = schema.putArray("required")
        required.forEach(names::add)
    }
    return schema
}

private fun boolean(description: String): ObjectNode = property("boolean", description)

private fun integerArray(description: String): ObjectNode {
    val node = property("array", description)
    node.putObject("items").put("type", "integer")
    return node
}

private fun string(
    description: String,
    vararg allowed: String,
): ObjectNode {
    val node = property("string", description)
    if (allowed.isNotEmpty()) {
        val values = node.putArray("enum")
        allowed.forEach(values::add)
    }
    return node
}

private fun integer(
    description: String,
    minimum: Long,
    maximum: Long? = null,
): ObjectNode {
    val node = property("integer", description)
    node.put("minimum", minimum)
    if (maximum != null) node.put("maximum", maximum)
    return node
}

private fun stringArray(description: String): ObjectNode {
    val node = property("array", description)
    node.putObject("items").put("type", "string")
    return node
}

private fun property(
    type: String,
    description: String,
): ObjectNode =
    McpDispatcher.MAPPER
        .createObjectNode()
        .put("type", type)
        .put("description", description)

private fun ObjectNode.text(name: String): String? = get(name)?.asText()

private fun ObjectNode.long(name: String): Long? = get(name)?.asLong()
