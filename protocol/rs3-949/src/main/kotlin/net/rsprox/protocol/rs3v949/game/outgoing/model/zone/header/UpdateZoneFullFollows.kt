package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.header

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UpdateZoneFullFollows(
    public val level: Int,
    public val zoneX: Int,
    public val zoneZ: Int,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "UpdateZoneFullFollows(level=$level, zoneX=$zoneX, zoneZ=$zoneZ)"
    }
}
