package net.rsprox.protocol.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Ambience stop is used to stop any looping background ambience sound effect.
 * @property fade whether to fade the existing ambience out, if one is currently playing.
 */
public class AmbienceStop(
    public val fade: Boolean,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AmbienceStop

        if (fade != other.fade) return false

        return true
    }

    override fun hashCode(): Int {
        return fade.hashCode()
    }

    override fun toString(): String =
        "AmbienceStop(" +
            "fade=$fade" +
            ")"
}
