package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MapProjAnimHalfsq(
    public val srcCoordHalf: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val destXdeltaHalf: Int,
    public val destYdeltaHalf: Int,
    public val srcModel: Int,
    public val idB: Int,
    public val id: Int,
    public val heightByte: Int,
    public val heightByte2: Int,
    public val trailingBytes: ByteArray,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "MapProjAnimHalfsq(srcCoordHalf=$srcCoordHalf, xInZone=$xInZone, zInZone=$zInZone, " +
            "destXdeltaHalf=$destXdeltaHalf, destYdeltaHalf=$destYdeltaHalf, srcModel=$srcModel, idB=$idB, " +
            "id=$id, heightByte=$heightByte, heightByte2=$heightByte2, " +
            "trailingBytes=[${trailingBytes.joinToString(" ") { "%02x".format(it) }}])"
    }
}
