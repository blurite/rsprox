package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class ObjDel(
    public val big: Boolean,
    public val objId: Int,
    public val xInZone: Int,
    public val zInZone: Int,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "ObjDel(big=$big, objId=$objId, xInZone=$xInZone, zInZone=$zInZone)"
    }
}
