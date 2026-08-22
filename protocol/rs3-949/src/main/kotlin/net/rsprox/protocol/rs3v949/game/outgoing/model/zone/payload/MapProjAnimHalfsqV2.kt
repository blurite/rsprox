package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MapProjAnimHalfsqV2(
    public val srcCoordHalf: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val destXdeltaHalf: Int,
    public val destYdeltaHalf: Int,
    public val srcModel: Int,
    public val idB: Int,
    public val id: Int,
    public val heightByte: Int,
    public val trailingBytes: ByteArray,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "MapProjAnimHalfsqV2(srcCoordHalf=$srcCoordHalf, xInZone=$xInZone, zInZone=$zInZone, " +
            "destXdeltaHalf=$destXdeltaHalf, destYdeltaHalf=$destYdeltaHalf, srcModel=$srcModel, idB=$idB, " +
            "id=$id, heightByte=$heightByte, " +
            "trailingBytes=[${trailingBytes.joinToString(" ") { "%02x".format(it) }}])"
    }
}
