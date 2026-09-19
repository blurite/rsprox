package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class MapAnimV2(
    public val xInZone: Int,
    public val zInZone: Int,
    public val id: Int,
    public val height: Int,
    public val packedDelay: Int,
    public val rotation: Int,
    public val packedOffsets: Int,
    public val unused0: Int? = null,
    public val unused1: Int? = null,
    public val unused2: Int? = null,
) : IncomingServerGameMessage {
    public val delay: Int get() = packedDelay and 0x7FFF
    public val independentRotation: Boolean get() = packedDelay and 0x8000 != 0
    // Fine-coordinate units (512 per tile), biased by 1023 on the wire.
    public val offsetX: Int get() = (packedOffsets and 0x7FF) - 1023
    public val offsetZ: Int get() = (packedOffsets ushr 11 and 0x7FF) - 1023
    // The native client tests the entire two-bit selector, not just bit 22.
    public val relativeOffset: Boolean get() = packedOffsets ushr 22 == 1
}
