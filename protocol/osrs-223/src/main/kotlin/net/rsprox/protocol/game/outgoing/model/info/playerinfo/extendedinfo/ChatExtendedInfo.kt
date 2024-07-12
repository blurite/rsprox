package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class ChatExtendedInfo(
    public val colour: Int,
    public val effects: Int,
    public val modIcon: Int,
    public val autotyper: Boolean,
    public val text: String,
    public val pattern: ByteArray?,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatExtendedInfo

        if (colour != other.colour) return false
        if (effects != other.effects) return false
        if (modIcon != other.modIcon) return false
        if (autotyper != other.autotyper) return false
        if (text != other.text) return false
        if (pattern != null) {
            if (other.pattern == null) return false
            if (!pattern.contentEquals(other.pattern)) return false
        } else if (other.pattern != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = colour
        result = 31 * result + effects
        result = 31 * result + modIcon
        result = 31 * result + autotyper.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + (pattern?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ChatExtendedInfo(" +
            "colour=$colour, " +
            "effects=$effects, " +
            "modIcon=$modIcon, " +
            "autotyper=$autotyper, " +
            "text='$text', " +
            "pattern=${pattern?.contentToString()}" +
            ")"
    }
}
