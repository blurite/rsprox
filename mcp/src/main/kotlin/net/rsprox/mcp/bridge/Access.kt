package net.rsprox.mcp.bridge

/**
 * What may be done with a client: read it, or also drive it by sending it input. The order matters:
 * [DRIVE] includes [READ], so an access is enough for a call when it is at least what the call needs.
 */
public enum class Access(
    /** The name on the wire: in the welcome the hub sends and in the `access` of a session. */
    public val wire: String,
) {
    /** Read the client: its state, widgets, variables, entities, a screenshot and where the camera points. */
    READ("read"),

    /** Read the client and send it input: clicks, keys, a login and camera turns. */
    DRIVE("drive"),
}
