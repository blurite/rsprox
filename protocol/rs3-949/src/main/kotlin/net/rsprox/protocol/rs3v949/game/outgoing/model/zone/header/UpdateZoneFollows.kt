package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.header

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UpdateZoneFollows(
    public val full: Boolean,
    public val level: Int,
    public val zoneX: Int,
    public val zoneZ: Int,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "UpdateZoneFollows(full=$full, level=$level, zoneX=$zoneX, zoneZ=$zoneZ)"
    }
}
