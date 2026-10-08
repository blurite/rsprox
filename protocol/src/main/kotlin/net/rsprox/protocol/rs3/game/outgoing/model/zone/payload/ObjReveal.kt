package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class ObjReveal(
    public val big: Boolean,
    public val objId: Int,
    public val count: Int,
    public val excludedPlayerIndex: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val version: Int = if (big) 2 else 1,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "ObjReveal(big=$big, objId=$objId, count=$count, excludedPlayerIndex=$excludedPlayerIndex, " +
            "xInZone=$xInZone, zInZone=$zInZone, version=$version)"
    }
}
