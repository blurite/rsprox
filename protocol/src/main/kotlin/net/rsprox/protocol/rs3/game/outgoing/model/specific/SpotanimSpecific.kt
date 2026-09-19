package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Packed wire fields are retained. Height is signed and already in native fine units;
 * id -1 denotes removal. Targets can be players, NPCs or map tiles.
 */
public data class SpotanimSpecific(
    public val height: Int,
    public val packedDelay: Int,
    public val rotationFlags: Int,
    public val target: Int,
    public val id: Int,
    public val slot: Int,
) : IncomingServerGameMessage {
    public val delay: Int get() = packedDelay and 0x7FFF
    public val independentRotation: Boolean get() = packedDelay and 0x8000 != 0
    public val rotation: Int get() = rotationFlags and 7

    /** Requests the loop override; false leaves the animation mode to the effect definition. */
    public val loop: Boolean get() = rotationFlags and 0x80 != 0
    public val unknownFlags: Int get() = rotationFlags and 0x78

    // Specific spotanims do not use the projectile/mask type-byte actor encoding.
    public val isMapTarget: Boolean get() = target ushr 30 != 0
    public val isNpcTarget: Boolean get() = !isMapTarget && target and 0x20000000 != 0
    public val targetIndex: Int get() = target and 0xFFFF
    public val unusedTargetBits: Int get() = if (isMapTarget) 0 else target and 0x1FFF0000

    /** Only meaningful for map targets; the native -1 sentinel remains invalid. */
    public val targetCoord: CoordGrid
        get() = if (target == -1) CoordGrid.INVALID else CoordGrid(target and 0x3FFFFFFF)
}
