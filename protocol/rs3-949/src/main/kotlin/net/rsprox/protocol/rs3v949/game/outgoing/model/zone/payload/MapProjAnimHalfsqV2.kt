package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MapProjAnimHalfsqV2(
    public val srcCoordHalf: Int,
    public val xInZoneHalf: Int,
    public val zInZoneHalf: Int,
    public val destXdeltaHalf: Int,
    public val destYdeltaHalf: Int,
    public val flags: Int,
    public val sourceType: Int,
    public val sourceIndex: Int,
    public val targetType: Int,
    public val targetIndex: Int,
    public val spotAnimId: Int,
    public val startHeight: Int,
    public val endHeight: Int,
    public val startTime: Int,
    public val endTime: Int,
    public val alpha: Int,
    public val angle: Int,
    public val startOffset: Int,
    public val endOffset: Int,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "MapProjAnimHalfsqV2(" +
            "xInZoneHalf=$xInZoneHalf, zInZoneHalf=$zInZoneHalf, " +
            "destXdeltaHalf=$destXdeltaHalf, destYdeltaHalf=$destYdeltaHalf, " +
            "flags=$flags, sourceType=$sourceType, sourceIndex=$sourceIndex, " +
            "targetType=$targetType, targetIndex=$targetIndex, " +
            "spotAnimId=$spotAnimId, startHeight=$startHeight, endHeight=$endHeight, " +
            "startTime=$startTime, endTime=$endTime, alpha=$alpha, angle=$angle, " +
            "startOffset=0x${startOffset.toString(16)}, endOffset=0x${endOffset.toString(16)})"
    }
}
