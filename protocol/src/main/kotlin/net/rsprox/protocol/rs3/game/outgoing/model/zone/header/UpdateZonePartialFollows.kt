package net.rsprox.protocol.rs3.game.outgoing.model.zone.header

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UpdateZonePartialFollows(
    public val level: Int,
    public val zoneX: Int,
    public val zoneZ: Int,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "UpdateZonePartialFollows(level=$level, zoneX=$zoneX, zoneZ=$zoneZ)"
    }
}
