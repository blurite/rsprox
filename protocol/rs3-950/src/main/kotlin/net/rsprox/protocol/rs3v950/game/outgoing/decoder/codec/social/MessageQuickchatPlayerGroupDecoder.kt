package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPlayerGroup
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.cache.readQuickChat
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessageQuickchatPlayerGroupDecoder : ProxyMessageDecoder<MessageQuickchatPlayerGroup> {
    override val prot: ClientProt = GameServerProt.MESSAGE_QUICKCHAT_PLAYER_GROUP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageQuickchatPlayerGroup {
        val sender = buffer.readNativeString()
        val messageWorld = buffer.g2()
        val messageCounter = buffer.g3()
        val playerType = buffer.g1()
        val broadcast = buffer.g1()
        val phraseId = buffer.g2()
        return MessageQuickchatPlayerGroup(
            sender,
            messageWorld,
            messageCounter,
            playerType,
            broadcast,
            phraseId,
            buffer.readQuickChat(session, phraseId),
        )
    }
}
