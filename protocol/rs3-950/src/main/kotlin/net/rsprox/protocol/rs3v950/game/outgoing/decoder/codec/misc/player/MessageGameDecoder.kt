package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.MessageGame
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessageGameDecoder : ProxyMessageDecoder<MessageGame> {
    override val prot: ClientProt = GameServerProt.MESSAGE_GAME

    override fun decode(buffer: JagByteBuf, session: Session): MessageGame {
        val type = buffer.gSmart1or2()
        val channel = buffer.g4()
        val flags = buffer.g1()
        val sender = if (flags and 1 != 0) buffer.gjstr() else null
        val alternateSender = if (flags and 3 == 3) buffer.gjstr() else null
        val message = buffer.gjstr()
        return MessageGame(type, channel, flags, sender, alternateSender, message)
    }
}
