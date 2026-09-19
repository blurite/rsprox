package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPrivate
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.cache.readQuickChat
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessageQuickchatPrivateDecoder : ProxyMessageDecoder<MessageQuickchatPrivate> {
    override val prot: ClientProt = GameServerProt.MESSAGE_QUICKCHAT_PRIVATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageQuickchatPrivate {
        val alternateSenderFlag = buffer.g1()
        val sender = buffer.readNativeString()
        val alternateSender = if (alternateSenderFlag == 1) buffer.readNativeString() else null
        val messageWorld = buffer.g2()
        val messageCounter = buffer.g3()
        val playerType = buffer.g1()
        val phraseId = buffer.g2()
        return MessageQuickchatPrivate(
            alternateSenderFlag,
            sender,
            alternateSender,
            messageWorld,
            messageCounter,
            playerType,
            phraseId,
            buffer.readQuickChat(session, phraseId),
        )
    }
}
