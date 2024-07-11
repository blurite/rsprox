package net.rsprox.protocol.game.outgoing.model.playerinfo.extendedinfo

public class SayExtendedInfo(
    public val text: String,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SayExtendedInfo

        return text == other.text
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }

    override fun toString(): String {
        return "SayExtendedInfo(text='$text')"
    }
}
