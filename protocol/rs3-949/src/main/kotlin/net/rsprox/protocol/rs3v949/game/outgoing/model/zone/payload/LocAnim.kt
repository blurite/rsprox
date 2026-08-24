package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class LocAnim(
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "LocAnim(rawBytes=${rawBytes.joinToString(",") { "%02x".format(it) }})"
    }
}
