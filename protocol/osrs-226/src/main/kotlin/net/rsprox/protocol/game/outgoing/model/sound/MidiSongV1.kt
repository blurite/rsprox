package net.rsprox.protocol.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Midi song old packet is used to play a midi song, in the old format.
 * This is equal to playing [MidiSongV2] with the arguments of `id, 0, 60, 60, 0`.
 * @property id the id of the song to play
 */
public class MidiSongV1(
    public val id: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiSongV1

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "MidiSongV1(id=$id)"
    }
}
