package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class LocCustomise(
    public val locId: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val shape: Int,
    public val rotation: Int,
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
    public val models: IntArray?,
    public val recolours: IntArray?,
    public val retextures: IntArray?,
    public val customisationFlags: Int? = null,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "LocCustomise(locId=$locId, xInZone=$xInZone, zInZone=$zInZone, shape=$shape, " +
            "rotation=$rotation, hasExtendedTransform=$hasExtendedTransform, " +
            "rotation=($rotationX, $rotationY, $rotationZ, $rotationW), " +
            "translate=($translationX, $translationY, $translationZ), " +
            "scale=($scaleX, $scaleY, $scaleZ), " +
            "modelsSize=${models?.size}, recoloursSize=${recolours?.size}, " +
            "retexturesSize=${retextures?.size})"
    }
}
