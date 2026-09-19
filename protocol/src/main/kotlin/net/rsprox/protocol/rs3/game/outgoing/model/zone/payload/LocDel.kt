package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class LocDel(
    public val xInZone: Int,
    public val zInZone: Int,
    public val shape: Int,
    public val rotation: Int,
    public val hasExtendedTransform: Boolean = false,
    public val rotationX: Float = 0f,
    public val rotationY: Float = 0f,
    public val rotationZ: Float = 0f,
    public val rotationW: Float = 1f,
    public val translationX: Float = 0f,
    public val translationY: Float = 0f,
    public val translationZ: Float = 0f,
    public val scaleX: Float = 1f,
    public val scaleY: Float = 1f,
    public val scaleZ: Float = 1f,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "LocDel(xInZone=$xInZone, zInZone=$zInZone, shape=$shape, rotation=$rotation, " +
            "hasExtendedTransform=$hasExtendedTransform, " +
            "rotation=($rotationX, $rotationY, $rotationZ, $rotationW), " +
            "translation=($translationX, $translationY, $translationZ), scale=($scaleX, $scaleY, $scaleZ))"
    }
}
