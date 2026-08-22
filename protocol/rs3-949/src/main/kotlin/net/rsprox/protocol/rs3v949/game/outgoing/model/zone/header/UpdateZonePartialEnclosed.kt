package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.header

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UpdateZonePartialEnclosed(
    public val level: Int,
    public val zoneX: Int,
    public val zoneZ: Int,
    public val packets: List<IncomingServerGameMessage>,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "UpdateZonePartialEnclosed(level=$level, zoneX=$zoneX, zoneZ=$zoneZ, packets=$packets)"
    }
}
