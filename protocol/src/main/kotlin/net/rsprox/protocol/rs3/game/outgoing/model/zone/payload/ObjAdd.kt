package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class ObjAdd(
    public val big: Boolean,
    public val objId: Int,
    public val count: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val version: Int = if (big) 2 else 1,
    public val coordinateFlags: Int? = null,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "ObjAdd(big=$big, objId=$objId, count=$count, xInZone=$xInZone, zInZone=$zInZone, " +
            "version=$version, coordinateFlags=$coordinateFlags)"
    }
}
