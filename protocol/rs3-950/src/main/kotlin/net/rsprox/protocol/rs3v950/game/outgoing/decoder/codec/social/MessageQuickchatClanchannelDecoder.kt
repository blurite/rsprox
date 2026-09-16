package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatClanchannel
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.cache.readQuickChat
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessageQuickchatClanchannelDecoder : ProxyMessageDecoder<MessageQuickchatClanchannel> {
    override val prot: ClientProt = GameServerProt.MESSAGE_QUICKCHAT_CLANCHANNEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageQuickchatClanchannel {
        val channel = buffer.g1s()
        val sender = buffer.readNativeString()
        val messageWorld = buffer.g2()
        val messageCounter = buffer.g3()
        val playerType = buffer.g1()
        val phraseId = buffer.g2()
        return MessageQuickchatClanchannel(
            channel,
            sender,
            messageWorld,
            messageCounter,
            playerType,
            phraseId,
            buffer.readQuickChat(session, phraseId),
        )
    }
}
