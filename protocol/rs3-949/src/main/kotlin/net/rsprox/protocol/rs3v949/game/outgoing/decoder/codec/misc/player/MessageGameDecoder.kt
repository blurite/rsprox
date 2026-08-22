package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.misc.player.MessageGame
import net.rsprox.protocol.session.Session

internal class MessageGameDecoder : ProxyMessageDecoder<MessageGame> {
    override val prot: ClientProt = GameServerProt.MESSAGE_GAME

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageGame {
        val peek = buffer.g1()
        val type =
            if (peek < 128) {
                peek
            } else {
                ((peek shl 8) or buffer.g1()) - 32768
            }
        val effectFlags = run {
            val b0 = buffer.g1()
            val b1 = buffer.g1()
            val b2 = buffer.g1()
            val b3 = buffer.g1()
            (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
        }
        val hasSender = buffer.g1() == 1
        val name = if (hasSender) buffer.gjstr() else null
        val message = buffer.gjstr()
        return MessageGame(
            type,
            effectFlags,
            name,
            message,
        )
    }
}
