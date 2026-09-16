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
    public val translateA: Float,
    public val translateB: Float,
    public val translateC: Float,
    public val scaleX: Float,
    public val scaleY: Float,
    public val scaleZ: Float,
    public val uintArray: IntArray?,
    public val opcodeArrayA: IntArray?,
    public val opcodeArrayB: IntArray?,
    public val customisationFlags: Int? = null,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "LocCustomise(locId=$locId, xInZone=$xInZone, zInZone=$zInZone, shape=$shape, " +
            "rotation=$rotation, hasExtendedTransform=$hasExtendedTransform, " +
            "rotation=($rotationX, $rotationY, $rotationZ, $rotationW), " +
            "translate=($translateA, $translateB, $translateC), " +
            "scale=($scaleX, $scaleY, $scaleZ), " +
            "uintArraySize=${uintArray?.size}, opcodeArrayASize=${opcodeArrayA?.size}, " +
            "opcodeArrayBSize=${opcodeArrayB?.size})"
    }
}
