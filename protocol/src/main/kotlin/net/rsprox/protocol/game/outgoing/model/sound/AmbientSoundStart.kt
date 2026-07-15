package net.rsprox.protocol.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Ambient sound start is used to set a looping background ambience sound effect.
 * This will continue looping until stopped or overwritten.
 * @property id the id of the sound effect to use for the ambience.
 * @property fade whether to fade the existing ambience out, if one is currently playing.
 */
public class AmbientSoundStart private constructor(
    private val _id: UShort,
    public val fade: Boolean,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        fade: Boolean,
    ) : this(
        id.toUShort(),
        fade,
    )

    public val id: Int
        get() = _id.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AmbientSoundStart

        if (_id != other._id) return false
        if (fade != other.fade) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + fade.hashCode()
        return result
    }

    override fun toString(): String =
        "AmbientSoundStart(" +
            "id=$id, " +
            "fade=$fade" +
            ")"
}
