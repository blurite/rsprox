package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPrivateEcho
import net.rsprox.protocol.rs3v950beta.buffer.readNativeString
import net.rsprox.protocol.rs3v950beta.cache.readQuickChat
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessageQuickchatPrivateEchoDecoder : ProxyMessageDecoder<MessageQuickchatPrivateEcho> {
    override val prot: ClientProt = GameServerProt.MESSAGE_QUICKCHAT_PRIVATE_ECHO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageQuickchatPrivateEcho {
        val recipient = buffer.readNativeString()
        val phraseId = buffer.g2()
        return MessageQuickchatPrivateEcho(
            recipient,
            phraseId,
            buffer.readQuickChat(session, phraseId),
        )
    }
}
