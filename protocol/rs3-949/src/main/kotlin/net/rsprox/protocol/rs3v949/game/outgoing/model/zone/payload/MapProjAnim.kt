package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MapProjAnim(
    public val mediumId: Int,
    public val id: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val targetDeltaX: Int,
    public val targetDeltaY: Int,
    public val startHeight: Int,
    public val endHeight: Int,
    public val startTime: Int,
    public val endTime: Int,
    public val alpha: Int,
    public val lockonSlot: Int,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "MapProjAnim(mediumId=$mediumId, id=$id, xInZone=$xInZone, zInZone=$zInZone, targetDeltaX=$targetDeltaX, " +
            "targetDeltaY=$targetDeltaY, startHeight=$startHeight, endHeight=$endHeight, " +
            "startTime=$startTime, endTime=$endTime, alpha=$alpha, lockonSlot=$lockonSlot)"
    }
}
