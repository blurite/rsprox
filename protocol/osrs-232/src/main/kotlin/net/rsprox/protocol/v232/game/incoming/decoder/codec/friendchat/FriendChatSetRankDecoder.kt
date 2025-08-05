package net.rsprox.protocol.v232.game.incoming.decoder.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatSetRank
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.incoming.decoder.prot.GameClientProt

public class FriendChatSetRankDecoder : ProxyMessageDecoder<FriendChatSetRank> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_SETRANK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendChatSetRank {
        val name = buffer.gjstr()
        val rank = buffer.g1Alt1()
        return FriendChatSetRank(
            name,
            rank,
        )
    }
}
