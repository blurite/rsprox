package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Decoded wire fields; client scaling is not applied to the stored values. */
public data class SoundAreaV2(
    public val coordinate: Int,
    public val id: Int,
    public val loopsAndRange: Int,
    public val delay: Int,
    public val volume: Int,
    public val rate: Int,
    public val extendedAudioMode: Int,
) : IncomingServerGameMessage {
    public val xInZone: Int get() = (coordinate ushr 4) and 7
    public val zInZone: Int get() = coordinate and 7
    public val loops: Int get() = loopsAndRange and 7
    public val range: Int get() = loopsAndRange ushr 4
    public val consumerMode: Int get() = if (extendedAudioMode == 1) 8 else 6
}
