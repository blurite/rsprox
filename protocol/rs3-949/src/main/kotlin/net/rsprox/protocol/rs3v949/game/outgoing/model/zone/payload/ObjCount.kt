package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class ObjCount(
    public val big: Boolean,
    public val objId: Int,
    public val oldCount: Int,
    public val newCount: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "ObjCount(big=$big, objId=$objId, oldCount=$oldCount, newCount=$newCount, xInZone=$xInZone, zInZone=$zInZone)"
    }
}
