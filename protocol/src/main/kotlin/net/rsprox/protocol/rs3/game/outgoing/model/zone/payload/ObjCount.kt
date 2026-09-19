package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class ObjCount(
    public val big: Boolean,
    public val objId: Int,
    public val oldQuantity: Int,
    public val newQuantity: Int,
    public val xInZone: Int,
    public val zInZone: Int,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "ObjCount(big=$big, objId=$objId, oldQuantity=$oldQuantity, newQuantity=$newQuantity, xInZone=$xInZone, zInZone=$zInZone)"
    }
}
