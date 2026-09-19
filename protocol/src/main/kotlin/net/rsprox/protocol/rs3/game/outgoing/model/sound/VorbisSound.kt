package net.rsprox.protocol.rs3.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class VorbisSound(
    public val soundId: Int,
    public val loops: Int,
    public val delay: Int,
    public val volume: Int,
    public val pitch: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VorbisSound

        if (soundId != other.soundId) return false
        if (loops != other.loops) return false
        if (delay != other.delay) return false
        if (volume != other.volume) return false
        if (pitch != other.pitch) return false

        return true
    }

    override fun hashCode(): Int {
        var result = soundId
        result = 31 * result + loops
        result = 31 * result + delay
        result = 31 * result + volume
        result = 31 * result + pitch
        return result
    }

    override fun toString(): String {
        return "VorbisSound(soundId=$soundId, loops=$loops, delay=$delay, volume=$volume, pitch=$pitch)"
    }
}
