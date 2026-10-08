package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import net.rsprox.mcp.bridge.BridgeError
import net.rsprox.mcp.packets.Cursor
import net.rsprox.mcp.packets.Origin
import net.rsprox.mcp.packets.PacketPage
import net.rsprox.mcp.packets.PacketQuery
import net.rsprox.mcp.packets.PacketRecord
import net.rsprox.mcp.session.Session
import net.rsprox.mcp.session.SessionManager

/**
 * Build the tool table. [sessions] is resolved on every call and may throw a [ToolError] while the proxy
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
                    "running launches nothing. An attached session cannot be started.",
            inputSchema =
                schema(
                    "target" to string("Proxy target name, as listed by session_list. Example: \"My Server\"."),
                    "session" to string("Existing session id, such as \"s1\"."),
                    "wait_ms" to
                        integer(
                            "How long to wait for the session to connect. Default ${SessionManager.DEFAULT_WAIT_MS}.",
                            0,
                            600_000,
                        ),
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
            description =
                "Kill the client of a session that session_start launched. The session and its packets stay " +
                    "readable. Not available for an attached session, whose client belongs to whoever runs " +
                    "the rsprox GUI.",
            inputSchema = schema("session" to SESSION),
        ) { args ->
            ToolResult.Json(sessions().stop(args.text("session")))
        },
        Tool(
            name = "session_list",
            description =
                "List every session, plus the target names. `kind` is `launched` for a client that " +
                    "session_start launched, and `attached` for a client that was launched by hand from the " +
                    "rsprox GUI, which only packets_read works on. Each session has `session`, `target`, " +
                    "`state`, `proxyPort` and `cursor`, the newest packet cursor. A launched session with a " +
                    "client also has `generation`, the number of its launch, and `httpPort`, and `pid` once " +
                    "connected. `state` is `stopped`, " +
                    "`launching` or `connected` when launched, and `attached` or `ended` when attached; " +
                    "`reason` says why a session has no client. Once the client has logged in, `login` holds " +
                    "`epoch` (the L number of its packets), `revision`, `world`, `host`, `localPlayerIndex`, " +
                    "`connectedAt`, `online` (whether it is still logged in) and `transcribing` (whether its " +
                    "packets are decoded). `captureFile`, the recording under the rsprox binary directory, " +
                    "`name`, the display name, and `tick`, the newest server tick seen, are absent until known.",
            inputSchema = schema(),
        ) {
            val manager = sessions()
            ToolResult.Json(mapOf("sessions" to manager.list(), "targets" to manager.targets()))
        },
        Tool(
            name = "packets_read",
            description =
                "Read decoded packets of a session after a cursor, in both directions. The standalone MCP " +
                    "server logs every packet, unfiltered. Inside the rsprox GUI the log holds the GUI's view: " +
                    "a packet that the GUI's filters or settings hide is missing from every session. " +
                    "To wait for a packet, pass `wait_ms`. The call then blocks until the first match. " +
                    "Line 1 of the result is JSON: pass `next` as `after` on the following call; `dropped` counts " +
                    "records that were evicted before they could be read; `timedOut` is true when a wait " +
                    "elapsed with no match; `head` is the newest cursor of the log and `count` the number of " +
                    "packets that follow. Each further line is one packet: " +
                    "`<seq> L<login> T<tick> <C|S|P> <PROT> <text>`, where C is client to server, S is server " +
                    "to client and P is an rsprox marker (CLIENT_LAUNCHED, CLIENT_CONNECTED, CLIENT_ATTACHED, " +
                    "CLIENT_EXITED, LOGIN, LOGOUT). Continuation lines of one packet are indented.",
            inputSchema =
                schema(
                    "session" to SESSION,
                    "after" to integer("Cursor to read after. Default 0, the start of the log.", 0),
                    "prots" to stringArray("Prot names to keep, such as [\"IF_SETTEXT\"]. Default: all."),
                    "origin" to string("Keep only one origin.", "client", "server", "proxy"),
                    "contains" to string("Case-insensitive substring the packet text must contain."),
                    "wait_ms" to integer("How long to block for a first match. Default 0.", 0, 120_000),
                    "limit" to
                        integer("Maximum number of packets to return. Default ${PacketQuery.DEFAULT_LIMIT}.", 1, 1000),
                ),
        ) { args ->
            val query =
                PacketQuery(
                    after = Cursor(args.long("after") ?: 0),
                    prots = args.get("prots")?.map { it.asText().uppercase() }?.toSet().orEmpty(),
                    origin = args.text("origin")?.let { Origin.valueOf(it.uppercase()) },
                    contains = args.text("contains"),
                    limit = args.long("limit")?.toInt() ?: PacketQuery.DEFAULT_LIMIT,
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
            result = { ok, _ -> ToolResult.Image(ok.remove("png")?.asText().orEmpty(), ok) },
        ),
        clientTool(
            name = "client_widgets",
            description =
                "List the interface widgets that have text, a name or options. Each has `id` " +
                    "(\"<group>:<child>\", or \"<group>:<child>[<index>]\" for a dynamic child), `text`, `name`, " +
                    "`actions` (its options), `bounds` as [x, y, width, height], `click` as the [x, y] at its " +
                    "centre, `type` " +
                    "and `hidden`. `roots` lists the groups of the top-level interfaces. `truncated` is true " +
                    "when `limit` cut the list short; narrow it with `group` or `text`.",
            schema =
                schema(
                    "session" to SESSION,
                    "group" to integer("Keep only widgets of this interface group, such as 558.", 0),
                    "text" to string("Case-insensitive substring to find in the text, name or options."),
                    "hidden" to boolean("Also list hidden widgets. Default false."),
                    "limit" to integer("Maximum number of widgets to return. Default $WIDGET_LIMIT.", 1, 2000),
                ),
            sessions = sessions,
            defaults = mapOf("limit" to WIDGET_LIMIT),
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
        clientTool(
            name = "client_login",
            description =
                "Log in with the given credentials and wait until the client is in the game. A client that is " +
                    "still starting is given until `wait_ms` to reach the login screen first. " +
                    "Returns `gameState` LOGGED_IN. Fails with `wrong_state` when the client is already past " +
                    "the login screen or the login is refused, and with `timeout` when `wait_ms` " +
                    "elapses first.",
            schema =
                schema(
                    "session" to SESSION,
                    "username" to string("Account name to log in with."),
                    "password" to
                        string("Password. It must not be empty. A server that ignores passwords accepts any value."),
                    "wait_ms" to
                        integer("How long to wait for the login to complete. Default $LOGIN_WAIT_MS.", 0, 120_000),
                    required = listOf("username", "password"),
                ),
            sessions = sessions,
            defaults = mapOf("wait_ms" to LOGIN_WAIT_MS),
            timeoutMs = { forwarded -> forwarded.get("wait_ms").asLong() + CLIENT_CALL_TIMEOUT_MS },
        ),
        clientTool(
            name = "client_click",
            description =
                "Click on the game canvas, either at `x`,`y` in canvas units (the pixels of a " +
                    "client_screenshot) or at the centre of `widget`. This is the low-level fallback: prefer " +
                    "client_interact, which aims by identity and cancels a click that would hit the wrong " +
                    "thing. A raw click is not guarded; the client performs whatever is under the mouse. " +
                    "Returns the `x`,`y` that was clicked. Fails with `not_found` when the widget does not " +
                    "exist or is not visible.",
            schema =
                schema(
                    "session" to SESSION,
                    "x" to integer("Horizontal canvas position. Requires `y`.", 0),
                    "y" to integer("Vertical canvas position. Requires `x`.", 0),
                    "widget" to WIDGET,
                    "button" to string("Mouse button. Default left.", "left", "right"),
                ),
            sessions = sessions,
        ),
        clientTool(
            name = "client_type",
            description =
                "Type text into the client as key presses, as if on the keyboard. Returns `typed`, the " +
                    "number of characters sent.",
            schema =
                schema(
                    "session" to SESSION,
                    "text" to string("The characters to type."),
                    "enter" to boolean("Press Enter after the text. Default false."),
                    required = listOf("text"),
                ),
            sessions = sessions,
        ),
        clientTool(
            name = "client_entities",
            description =
                "List what is near the local player on its plane, nearest first: `npcs`, `objects`, " +
                    "`ground_items` and `players`, each with `name`, its tile `x`,`y` in world coordinates and " +
                    "`options`, the names that client_interact accepts as `option`. An NPC and a player also " +
                    "have `index`; an NPC, an object and a ground item have `id`. An object is listed once, at " +
                    "its tile of origin, and only when it has options. `origin` is the tile of the local " +
                    "player. `truncated` is true when `limit` cut off the farthest entities; narrow the " +
                    "list with `kinds`, `radius` or `name`.",
            schema =
                schema(
                    "session" to SESSION,
                    "kinds" to
                        stringArray("Kinds to list. Default: all.", *TargetKind.names { it.listed }),
                    "radius" to
                        integer("Greatest distance from the local player in tiles. Default $ENTITY_RADIUS.", 0, 52),
                    "name" to string("Case-insensitive substring the name must contain."),
                    "limit" to integer("Maximum number of entities to return. Default $ENTITY_LIMIT.", 1, 2000),
                ),
            sessions = sessions,
            defaults = mapOf("radius" to ENTITY_RADIUS, "limit" to ENTITY_LIMIT),
        ),
        clientTool(
            name = "client_interact",
            description =
                "Perform one option on one thing in the game with a real mouse click, and confirm it in " +
                    "the packet log. It aims by identity, cancels a click that the client resolves to " +
                    "anything else and aims again, and turns the camera when the target is out of view. " +
                    "The server receives exactly what a player's click sends, mouse packets included. " +
                    "Pick the target from client_entities. Name an `npc` or a `player` by `index`. Name an " +
                    "`object` or a `ground_item` by `id` and its tile `x`,`y`. Name a `widget` by its " +
                    "client_widgets id. For `dialog`, `option` is \"continue\", or the text or the 1-based " +
                    "number of a choice. A `tile` is named by `x`,`y`; the call walks there and takes no " +
                    "`option`. Otherwise `option` is one of the target's own options, in any case. An NPC, " +
                    "an object and a ground item also accept \"Examine\", and a widget \"Continue\". " +
                    "Returns what was resolved, `attempts`, `tick` (the client's tick count when it " +
                    "performed the action), `tried` (the last attempts in words) and `camera` when it was " +
                    "turned. `packet` is the line of the packet the client sent, as packets_read prints " +
                    "it. `proof` is `click` when that packet follows the client's own record of the " +
                    "press, and `prefix` when no such record arrived and the packet was matched by its " +
                    "name alone. `packet` is null, with a `note`, for a widget option that the client " +
                    "handles without a packet, and while the packets of the login are not decoded. " +
                    "Fails with `not_found` when the client does not have the target or the target does " +
                    "not offer the option. Fails with `wrong_state` when the client never offered the " +
                    "option within the deadline, and says what it offered instead. The deadline is about " +
                    "three ticks, or six seconds after a camera turn. Fails with an error that names the " +
                    "likely cause when the client sent no packet for a target in the game world.",
            schema =
                schema(
                    "session" to SESSION,
                    "target" to string("Kind of thing to act on.", *TargetKind.names()),
                    "option" to string("Option to perform, such as \"Talk-to\". Required unless `target` is tile."),
                    *TARGET_FIELDS,
                    "widget" to WIDGET,
                    required = listOf("target"),
                ),
            sessions = sessions,
            timeoutMs = { INTERACTION_TIMEOUT_MS },
            result = { ok, session -> ToolResult.Json(confirmSent(session, ok, SENT_WAIT_MS)) },
        ),
        clientTool(
            name = "client_camera",
            description =
                "Read or turn the camera. With no arguments it returns the current `yaw` and `pitch`. " +
                    "Pass `yaw` and/or `pitch`, or `look_at` with the same target fields as client_interact, " +
                    "to turn it; the call then waits until the camera has settled and returns the resulting " +
                    "`yaw` and `pitch`, plus `onScreen` for a looked-at target. Angles are in the client's " +
                    "units: 16384 to a full turn of yaw, where 0 has north up; pitch from 1024 (lowest) to " +
                    "3064 (highest). client_interact turns the camera itself when it needs to.",
            schema =
                schema(
                    "session" to SESSION,
                    "yaw" to integer("Yaw to turn to.", 0, 16383),
                    "pitch" to integer("Pitch to turn to.", 1024, 3064),
                    "look_at" to
                        string(
                            "Kind of target to face, named by the fields of client_interact.",
                            *TargetKind.names { it.inWorld },
                        ),
                    *TARGET_FIELDS,
                ),
            sessions = sessions,
            timeoutMs = { INTERACTION_TIMEOUT_MS },
        ),
    )

/**
 * Build a client_* tool, which forwards its arguments to the op of the same name in the plugin.
 * The plugin's answer is the tool result, plus the packet cursor
 * taken before the call, so everything the action caused has a sequence number above it.
 * The session decides whether the tool may reach its client, and refuses it for an attached session.
 *
 * The plugin has no defaults of its own: each of [defaults] is forwarded when the caller left it out.
 */
private fun clientTool(
    name: String,
    description: String,
    schema: ObjectNode,
    sessions: () -> SessionManager,
    defaults: Map<String, Long> = emptyMap(),
    timeoutMs: (ObjectNode) -> Long = { CLIENT_CALL_TIMEOUT_MS },
    result: (ok: ObjectNode, session: Session) -> ToolResult = { ok, _ -> ToolResult.Json(ok) },
): Tool =
    Tool(name, "$description $CLIENT_TOOL_NOTE", schema) { args ->
        val session = sessions().resolve(args.text("session"))
        val link = session.link(name)
        val cursor = session.packets.head().seq
        val forwarded = args.deepCopy().without<ObjectNode>("session")

        for ((argument, value) in defaults) {
            if (!forwarded.hasNonNull(argument)) forwarded.put(argument, value)
        }

        val ok = link.call(name.removePrefix("client_"), forwarded, timeoutMs(forwarded))
        if (ok !is ObjectNode) throw BridgeError("internal", "the client answered $name with $ok")

        result(ok.put("cursor", cursor), session)
    }

/** The longest wait for the plugin to answer a call, on top of any wait the call itself asks for. */
private const val CLIENT_CALL_TIMEOUT_MS = 10_000L

/**
 * The longest wait for an interaction or a camera turn. It exceeds the plugin's longest deadline, six
 * seconds after a camera turn, plus the wait for a tick and for the camera to settle.
 */
private const val INTERACTION_TIMEOUT_MS = 15_000L

/** The wait for a login to complete, unless the caller says otherwise. */
private const val LOGIN_WAIT_MS = 15_000L

/** The most widgets that client_widgets returns, unless the caller says otherwise. */
private const val WIDGET_LIMIT = 200L

/** The greatest distance from the local player that client_entities lists, unless the caller says otherwise. */
private const val ENTITY_RADIUS = 15L

/** The most entities that client_entities returns, unless the caller says otherwise. */
private const val ENTITY_LIMIT = 100L

/**
 * The wait for the mouse click and the packet of an interaction. Packets reach the log in batches at
 * the end of each server tick, so the wait spans several ticks.
 */
private const val SENT_WAIT_MS = 3_000L

/** The sentences that end the description of every client_* tool. */
private const val CLIENT_TOOL_NOTE =
    "The result carries `cursor`, the packet cursor taken just before the call: pass it as `after` to " +
        "packets_read to see only the packets from this call onwards. Not available for an attached session."

/** The schema of the `session` argument that every tool but session_start and session_list takes. */
private val SESSION: ObjectNode = string("Session id, such as \"s1\". May be omitted while only one session exists.")

/** The schema of the `widget` argument of the tools that take a widget. */
private val WIDGET: ObjectNode = string("Widget id as listed by client_widgets, such as \"558:7\" or \"558:7[3]\".")

/** The arguments that name a target in the game world, for the tools that act on one. */
private val TARGET_FIELDS: Array<Pair<String, ObjectNode>> =
    arrayOf(
        "index" to integer("Index of the NPC or player, as listed by client_entities.", 0),
        "id" to integer("Id of the object or ground item, as listed by client_entities.", 0),
        "x" to integer("World x of the tile of the object, ground item or tile.", 0),
        "y" to integer("World y of the tile of the object, ground item or tile.", 0),
    )

/** Render a page as one line of JSON meta followed by one line per packet. */
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
        out.append('\n').append(line(packet))
    }

    return out.toString()
}

/** Render a packet as its line, with the continuation lines of its text indented. */
internal fun line(packet: PacketRecord): String {
    val text = packet.text.replace("\n", "\n    ")

    return "${packet.seq} L${packet.login} T${packet.cycle} ${packet.origin.letter} ${packet.prot} $text"
}

/** Build the schema of an object with the given properties. */
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

/** Build the schema of a boolean argument. */
private fun boolean(description: String): ObjectNode = property("boolean", description)

/** Build the schema of an argument that is an array of integers. */
private fun integerArray(description: String): ObjectNode {
    val node = property("array", description)
    node.putObject("items").put("type", "integer")

    return node
}

/** Build the schema of a string argument, limited to the allowed values when any are given. */
private fun string(
    description: String,
    vararg allowed: String,
): ObjectNode = property("string", description).limitedTo(allowed)

/** Build the schema of an integer argument within the given bounds. */
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

/** Build the schema of an argument that is an array of strings, limited to the allowed values when any are given. */
private fun stringArray(
    description: String,
    vararg allowed: String,
): ObjectNode {
    val node = property("array", description)
    node.putObject("items").put("type", "string").limitedTo(allowed)

    return node
}

/** Limit the schema to the allowed values when any are given. */
private fun ObjectNode.limitedTo(allowed: Array<out String>): ObjectNode {
    if (allowed.isNotEmpty()) {
        val values = putArray("enum")
        allowed.forEach(values::add)
    }

    return this
}

/** Build the schema of an argument with the given type and description. */
private fun property(
    type: String,
    description: String,
): ObjectNode =
    McpDispatcher.MAPPER
        .createObjectNode()
        .put("type", type)
        .put("description", description)

/** Get the named argument as text, or null when it is absent. */
private fun ObjectNode.text(name: String): String? = get(name)?.asText()

/** Get the named argument as a long, or null when it is absent. */
private fun ObjectNode.long(name: String): Long? = get(name)?.asLong()
