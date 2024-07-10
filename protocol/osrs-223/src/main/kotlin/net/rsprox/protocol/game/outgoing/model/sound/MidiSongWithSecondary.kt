package net.rsprox.protocol.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Midi song packets are used to play songs through the music player.
 * This packet pre-queues a secondary song which can be hot-swapped at any point.
 * The intended use case here is to swap the song out mid-playing between identical
 * songs that have different tones playing, e.g. a more up-beat vs a more somber song,
 * while letting the song play on from where it was, rather than re-starting the song.
 * @property primaryId the primary id of the song that will be playing
 * @property secondaryId the secondary id that will play if the `MIDI_SWAP` packet
 * is sent.
 * @property fadeOutDelay the delay in client cycles (20ms/cc) until the old song
 * begins fading out. The default value for this, based on the old midi song packet, is 0.
 * @property fadeOutSpeed the speed at which the old song fades out in client cycles (20ms/cc).
 * The default value for this, based on the old midi song packet, is 60.
 * @property fadeInDelay the delay until the new song begins playing, in client cycles (20ms/cc).
 * The default value for this, based on the old midi song packet is 60.
 * @property fadeInSpeed the speed at which the new song fades in, in client cycles (20ms/cc).
 * The default value for this, based on the old midi song packet is 0.
 */
@Suppress("DuplicatedCode")
public class MidiSongWithSecondary private constructor(
    private val _primaryId: UShort,
    private val _secondaryId: UShort,
    private val _fadeOutDelay: UShort,
    private val _fadeOutSpeed: UShort,
    private val _fadeInDelay: UShort,
    private val _fadeInSpeed: UShort,
) : IncomingServerGameMessage {
    public constructor(
        primaryId: Int,
        secondaryId: Int,
        fadeOutDelay: Int,
        fadeOutSpeed: Int,
        fadeInDelay: Int,
        fadeInSpeed: Int,
    ) : this(
        primaryId.toUShort(),
        secondaryId.toUShort(),
        fadeOutDelay.toUShort(),
        fadeOutSpeed.toUShort(),
        fadeInDelay.toUShort(),
        fadeInSpeed.toUShort(),
    )

    public val primaryId: Int
        get() = _primaryId.toInt()
    public val secondaryId: Int
        get() = _secondaryId.toInt()
    public val fadeOutDelay: Int
        get() = _fadeOutDelay.toInt()
    public val fadeOutSpeed: Int
        get() = _fadeOutSpeed.toInt()
    public val fadeInDelay: Int
        get() = _fadeInDelay.toInt()
    public val fadeInSpeed: Int
        get() = _fadeInSpeed.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiSongWithSecondary

        if (_primaryId != other._primaryId) return false
        if (_secondaryId != other._secondaryId) return false
        if (_fadeOutDelay != other._fadeOutDelay) return false
        if (_fadeOutSpeed != other._fadeOutSpeed) return false
        if (_fadeInDelay != other._fadeInDelay) return false
        if (_fadeInSpeed != other._fadeInSpeed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _primaryId.hashCode()
        result = 31 * result + _secondaryId.hashCode()
        result = 31 * result + _fadeOutDelay.hashCode()
        result = 31 * result + _fadeOutSpeed.hashCode()
        result = 31 * result + _fadeInDelay.hashCode()
        result = 31 * result + _fadeInSpeed.hashCode()
        return result
    }

    override fun toString(): String {
        return "MidiSongWithSecondary(" +
            "primaryId=$primaryId, " +
            "secondaryId=$secondaryId, " +
            "fadeOutSpeed=$fadeOutSpeed, " +
            "fadeOutDelay=$fadeOutDelay, " +
            "fadeInDelay=$fadeInDelay, " +
            "fadeInSpeed=$fadeInSpeed" +
            ")"
    }
}
