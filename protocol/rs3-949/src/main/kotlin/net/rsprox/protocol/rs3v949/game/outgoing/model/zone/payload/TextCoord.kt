package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class TextCoord(
    public val param1: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val rgb: Int,
    public val text: String,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "TextCoord(param1=$param1, xInZone=$xInZone, zInZone=$zInZone, rgb=${rgb.toString(16)}, text=\"$text\")"
    }
}
