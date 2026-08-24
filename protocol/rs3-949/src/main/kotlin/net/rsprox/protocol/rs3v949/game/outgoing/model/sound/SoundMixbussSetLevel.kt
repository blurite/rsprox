package net.rsprox.protocol.rs3v949.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class SoundMixbussSetLevel(
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "SoundMixbussSetLevel(rawBytes=${rawBytes.joinToString(",") { "%02x".format(it) }})"
    }
}
