package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessageQuickchatPrivate
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.cache.readQuickChat
import net.rsprox.protocol.session.Session

internal class MessageQuickchatPrivateDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<MessageQuickchatPrivate> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageQuickchatPrivate {
        val recipient = buffer.readNativeString()
        val phraseId = buffer.g2()
        return MessageQuickchatPrivate(recipient, phraseId, buffer.readQuickChat(session, phraseId, fromClient = true))
    }
}
