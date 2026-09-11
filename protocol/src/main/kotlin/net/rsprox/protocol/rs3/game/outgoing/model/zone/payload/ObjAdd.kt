package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class ObjAdd(
    public val big: Boolean,
    public val objId: Int,
    public val count: Int,
    public val xInZone: Int,
    public val zInZone: Int,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "ObjAdd(big=$big, objId=$objId, count=$count, xInZone=$xInZone, zInZone=$zInZone)"
    }
}
