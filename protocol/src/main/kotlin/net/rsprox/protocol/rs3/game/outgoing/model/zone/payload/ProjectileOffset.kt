package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

/** Packed projectile position offsets, in fine-coordinate units (512 per tile). */
@JvmInline
public value class ProjectileOffset(
    public val packed: Int,
) {
    public val x: Int get() = (packed and 0x7FF) - 1023
    public val z: Int get() = (packed ushr 11 and 0x7FF) - 1023
    public val mode: Int get() = packed ushr 22

    // Mode 3 is not relative: native code tests the entire two-bit selector.
    public val relative: Boolean get() = mode == 1
    public val unknownMode: Int get() = if (mode >= 2) mode else 0
}
