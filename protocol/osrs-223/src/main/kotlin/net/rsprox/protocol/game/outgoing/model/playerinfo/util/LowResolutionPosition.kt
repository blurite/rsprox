package net.rsprox.protocol.game.outgoing.model.playerinfo.util

import net.rsprox.protocol.common.CoordGrid

@JvmInline
public value class LowResolutionPosition(
    public val packed: Int,
) {
    public val x: Int
        get() = packed ushr 8 and 0xFF
    public val z: Int
        get() = packed and 0xFF
    public val level: Int
        get() = packed ushr 16 and 0x3
}

/**
 * A fake constructor for the low resolution position value class, as the JVM signature
 * matches that of the primary constructor.
 * @param coordGrid the absolute coordinate to turn into a low resolution position.
 * @return the low resolution representation of the given [coordGrid]
 */
public fun LowResolutionPosition(coordGrid: CoordGrid): LowResolutionPosition {
    return LowResolutionPosition(
        (coordGrid.z ushr 13)
            .or((coordGrid.x ushr 13) shl 8)
            .or((coordGrid.level shl 16)),
    )
}
