package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class SoundArea(
    public val soundId: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val rotation: Int,
    public val loopCount: Int,
    public val heightOffset: Int,
    public val range: Int,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "SoundArea(soundId=$soundId, xInZone=$xInZone, zInZone=$zInZone, rotation=$rotation, loopCount=$loopCount, heightOffset=$heightOffset, range=$range)"
    }
}
