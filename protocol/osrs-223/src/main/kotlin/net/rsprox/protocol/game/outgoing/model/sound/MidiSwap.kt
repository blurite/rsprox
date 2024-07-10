package net.rsprox.protocol.game.outgoing.model.sound

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Midi swap packet allows one to hot-swap a song mid-playing with a different one
 * that was pre-queued with the [MidiSongWithSecondary] packet.
 * This hot-swapping only works if the secondary packet was used, as that defines
 * the id of the secondary song to swap to.
 * @property fadeOutDelay the delay in client cycles (20ms/cc) until the old song
 * begins fading out.
 * @property fadeOutSpeed the speed at which the old song fades out in client cycles (20ms/cc).
 * @property fadeInDelay the delay until the new song begins playing, in client cycles (20ms/cc).
 * @property fadeInSpeed the speed at which the new song fades in, in client cycles (20ms/cc).
 */
public class MidiSwap private constructor(
    private val _fadeOutDelay: UShort,
    private val _fadeOutSpeed: UShort,
    private val _fadeInDelay: UShort,
    private val _fadeInSpeed: UShort,
) : OutgoingGameMessage {
    public constructor(
        fadeOutDelay: Int,
        fadeOutSpeed: Int,
        fadeInDelay: Int,
        fadeInSpeed: Int,
    ) : this(
        fadeOutDelay.toUShort(),
        fadeOutSpeed.toUShort(),
        fadeInDelay.toUShort(),
        fadeInSpeed.toUShort(),
    )

    public val fadeOutDelay: Int
        get() = _fadeOutDelay.toInt()
    public val fadeOutSpeed: Int
        get() = _fadeOutSpeed.toInt()
    public val fadeInDelay: Int
        get() = _fadeInDelay.toInt()
    public val fadeInSpeed: Int
        get() = _fadeInSpeed.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiSwap

        if (_fadeOutDelay != other._fadeOutDelay) return false
        if (_fadeOutSpeed != other._fadeOutSpeed) return false
        if (_fadeInDelay != other._fadeInDelay) return false
        if (_fadeInSpeed != other._fadeInSpeed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _fadeOutDelay.hashCode()
        result = 31 * result + _fadeOutSpeed.hashCode()
        result = 31 * result + _fadeInDelay.hashCode()
        result = 31 * result + _fadeInSpeed.hashCode()
        return result
    }

    override fun toString(): String {
        return "MidiSwap(" +
            "fadeOutDelay=$fadeOutDelay, " +
            "fadeInDelay=$fadeInDelay, " +
            "fadeInSpeed=$fadeInSpeed, " +
            "fadeOutSpeed=$fadeOutSpeed" +
            ")"
    }
}
