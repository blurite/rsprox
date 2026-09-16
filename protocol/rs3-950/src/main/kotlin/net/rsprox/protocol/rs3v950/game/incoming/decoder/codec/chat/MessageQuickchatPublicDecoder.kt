package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessageQuickchatPublic
import net.rsprox.protocol.rs3v950.cache.readQuickChat
import net.rsprox.protocol.session.Session

internal class MessageQuickchatPublicDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<MessageQuickchatPublic> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageQuickchatPublic {
        val phraseId = buffer.g2()
        return MessageQuickchatPublic(phraseId, buffer.readQuickChat(session, phraseId, fromClient = true))
    }
}
