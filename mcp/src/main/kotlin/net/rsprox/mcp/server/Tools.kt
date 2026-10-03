package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
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
                    "Returns once the client's launcher has started. With no arguments it starts a new session " +
                    "on the first custom target. Pass `session` to relaunch a stopped session on fresh ports; " +
                    "its packet log and cursor continue. Calling it for a session that is already running " +
                    "launches nothing.",
            inputSchema =
                schema(
                    "target" to string("Proxy target name, as listed by session_list. Example: \"My Server\"."),
                    "session" to string("Existing session id, such as \"s1\"."),
                    "wait_ms" to integer("How long to wait for the session to connect. Default 0.", 0, 600_000),
                ),
        ) { args ->
            ToolResult.Json(
                sessions().start(
                    target = args.text("target"),
                    session = args.text("session"),
                    waitMs = args.long("wait_ms") ?: 0,
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
    )

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

private fun schema(vararg properties: Pair<String, ObjectNode>): ObjectNode {
    val schema = McpDispatcher.MAPPER.createObjectNode()
    schema.put("type", "object")
    val node = schema.putObject("properties")
    for ((name, property) in properties) {
        node.set<JsonNode>(name, property)
    }
    return schema
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
