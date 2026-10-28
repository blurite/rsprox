package net.rsprox.mcp.server

/** The likely cause when the client performs an action in the game world and sends no packet for it. */
private const val LOST_CONNECTION = "the client logged out or lost its connection"

/**
 * The kinds of thing that client_interact acts on. The tool schemas and the proof of an interaction
 * are derived from this one list. The plugin finds a target by the same name, in `Targets.java`.
 */
internal enum class TargetKind(
    /** The name of the kind in the arguments and the result of a tool. */
    val wire: String,
    /** The likely cause when the client performs an action on a target of the kind and sends no packet for it. */
    val dropCause: String = LOST_CONNECTION,
) {
    /** An NPC, which the client drops an action on once the NPC is out of its view. */
    NPC("npc", "the NPC left the client's view"),

    /** An object on a tile. */
    OBJECT("object"),

    /** An item that lies on a tile. */
    GROUND_ITEM("ground_item"),

    /** Another player, which the client drops an action on once the player is out of its view. */
    PLAYER("player", "the player left the client's view"),

    /** A tile to walk to. */
    TILE("tile"),

    /** An interface component, some of whose options the client handles without a packet. */
    WIDGET("widget"),

    /** The open dialog, which the plugin resolves to the widget of the option. */
    DIALOG("dialog"),
    ;

    /** Whether a target of the kind is in the game world, where the camera can look at it. */
    val inWorld: Boolean
        get() = this != WIDGET && this != DIALOG

    /** Whether client_entities lists the targets of the kind. */
    val listed: Boolean
        get() = inWorld && this != TILE

    companion object {
        /** Get the names of the kinds that pass the test, in the order of the list. */
        fun names(wanted: (TargetKind) -> Boolean = { true }): Array<String> =
            entries.filter(wanted).map { it.wire }.toTypedArray()

        /** Get the kind with the name that the plugin answered with. */
        fun of(wire: String): TargetKind = entries.first { it.wire == wire }
    }
}
