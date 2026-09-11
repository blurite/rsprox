package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class TextCoord(
    public val duration: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val height: Int,
    public val rgb: Int,
    public val text: String,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextCoord

        if (duration != other.duration) return false
        if (xInZone != other.xInZone) return false
        if (zInZone != other.zInZone) return false
        if (height != other.height) return false
        if (rgb != other.rgb) return false
        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = duration
        result = 31 * result + xInZone
        result = 31 * result + zInZone
        result = 31 * result + height
        result = 31 * result + rgb
        result = 31 * result + text.hashCode()
        return result
    }

    override fun toString(): String {
        return "TextCoord(duration=$duration, xInZone=$xInZone, zInZone=$zInZone, " +
            "height=$height, rgb=$rgb, text='$text')"
    }
}
