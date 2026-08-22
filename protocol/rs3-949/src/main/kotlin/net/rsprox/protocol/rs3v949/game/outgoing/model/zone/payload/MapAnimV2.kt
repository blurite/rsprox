package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MapAnimV2(
    public val xInZone: Int,
    public val zInZone: Int,
    public val id: Int,
    public val val2: Int,
    public val val3: Int,
    public val heightByte: Int,
    public val fineOffsetPacked: Int,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "MapAnimV2(xInZone=$xInZone, zInZone=$zInZone, id=$id, val2=$val2, val3=$val3, heightByte=$heightByte, fineOffsetPacked=$fineOffsetPacked)"
    }
}
