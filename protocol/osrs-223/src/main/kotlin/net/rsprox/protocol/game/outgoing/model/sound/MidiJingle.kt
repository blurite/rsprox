package net.rsprox.protocol.game.outgoing.model.sound

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Midi jingle packet is used to play a short midi song, typically when
 * the player accomplishes something. The normal song that was playing
 * will be resumed after the jingle finishes playing.
 * In the old days, the [lengthInMillis] property was used to tell the client
 * how long the jingle lasts, so it knows when to resume the normal midi song.
 * It has long since been removed, however - while the client expects a 24-bit
 * integer for the length, it does not use this value in any way.
 * @property id the id of the jingle to play
 * @property lengthInMillis the length in milliseconds of the jingle, now unused.
 */
public class MidiJingle private constructor(
    private val _id: UShort,
    public val lengthInMillis: Int,
) : OutgoingGameMessage {
    public constructor(
        id: Int,
    ) : this(
        id.toUShort(),
        0,
    )

    public constructor(
        id: Int,
        lengthInMillis: Int,
    ) : this(
        id.toUShort(),
        lengthInMillis,
    )

    public val id: Int
        get() = _id.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiJingle

        if (_id != other._id) return false
        if (lengthInMillis != other.lengthInMillis) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + lengthInMillis
        return result
    }

    override fun toString(): String {
        return "MidiJingle(" +
            "id=$id, " +
            "lengthInMillis=$lengthInMillis" +
            ")"
    }
}
