package net.rsprox.protocol.rs3v949.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MidiSong(
    public val idByte0: Int,
    public val idByte1: Int,
    public val tailByte: Int,
    public val extraByte: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiSong

        if (idByte0 != other.idByte0) return false
        if (idByte1 != other.idByte1) return false
        if (tailByte != other.tailByte) return false
        if (extraByte != other.extraByte) return false

        return true
    }

    override fun hashCode(): Int {
        var result = idByte0
        result = 31 * result + idByte1
        result = 31 * result + tailByte
        result = 31 * result + extraByte
        return result
    }

    override fun toString(): String {
        return "MidiSong(idByte0=$idByte0, idByte1=$idByte1, tailByte=$tailByte, extraByte=$extraByte)"
    }
}
