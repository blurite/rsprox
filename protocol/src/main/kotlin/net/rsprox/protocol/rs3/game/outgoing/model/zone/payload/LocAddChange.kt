package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class LocAddChange(
    public val locId: Int,
    public val shape: Int,
    public val rotation: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val hasExtendedTransform: Boolean,
    public val rotationX: Float,
    public val rotationY: Float,
    public val rotationZ: Float,
    public val rotationW: Float,
    public val translationX: Float,
    public val translationY: Float,
    public val translationZ: Float,
    public val scaleX: Float,
    public val scaleY: Float,
    public val scaleZ: Float,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "LocAddChange(locId=$locId, shape=$shape, rotation=$rotation, xInZone=$xInZone, " +
            "zInZone=$zInZone, hasExtendedTransform=$hasExtendedTransform, " +
            "rotation=($rotationX, $rotationY, $rotationZ, $rotationW), " +
            "translation=($translationX, $translationY, $translationZ), " +
            "scale=($scaleX, $scaleY, $scaleZ))"
    }
}
