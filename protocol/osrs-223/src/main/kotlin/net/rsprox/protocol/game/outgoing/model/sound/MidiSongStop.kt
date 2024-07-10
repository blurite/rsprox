package net.rsprox.protocol.game.outgoing.model.sound

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Midi song stop is used to stop playing an existing midi song.
 * @property fadeOutDelay the delay in client cycles (20ms/cc) until the song begins fading out.
 * @property fadeOutSpeed the speed at which the song fades out in client cycles (20ms/cc).
 */
public class MidiSongStop private constructor(
    private val _fadeOutDelay: UShort,
    private val _fadeOutSpeed: UShort,
) : OutgoingGameMessage {
    public constructor(
        fadeOutDelay: Int,
        fadeOutSpeed: Int,
    ) : this(
        fadeOutDelay.toUShort(),
        fadeOutSpeed.toUShort(),
    )

    public val fadeOutDelay: Int
        get() = _fadeOutDelay.toInt()
    public val fadeOutSpeed: Int
        get() = _fadeOutSpeed.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiSongStop

        if (_fadeOutDelay != other._fadeOutDelay) return false
        if (_fadeOutSpeed != other._fadeOutSpeed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _fadeOutDelay.hashCode()
        result = 31 * result + _fadeOutSpeed.hashCode()
        return result
    }

    override fun toString(): String {
        return "MidiSongStop(" +
            "fadeOutDelay=$fadeOutDelay, " +
            "fadeOutSpeed=$fadeOutSpeed" +
            ")"
    }
}
