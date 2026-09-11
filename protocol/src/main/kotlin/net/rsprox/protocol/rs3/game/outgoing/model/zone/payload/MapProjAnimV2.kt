package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MapProjAnimV2(
    public val xInZone: Int,
    public val zInZone: Int,
    public val targetDeltaX: Int,
    public val targetDeltaY: Int,
    public val idMedium: Int,
    public val id: Int,
    public val trailingBytes: ByteArray,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "MapProjAnimV2(xInZone=$xInZone, zInZone=$zInZone, targetDeltaX=$targetDeltaX, " +
            "targetDeltaY=$targetDeltaY, idMedium=$idMedium, id=$id, " +
            "trailingBytes=[${trailingBytes.joinToString(" ") { "%02x".format(it) }}])"
    }
}
