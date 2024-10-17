package net.rsprox.protocol.game.incoming.decoder.codec.friendchat
import net.rsprox.protocol.session.Session

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatSetRank
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder

public class FriendChatSetRankDecoder : ProxyMessageDecoder<FriendChatSetRank> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_SETRANK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendChatSetRank {
        val rank = buffer.g1Alt3()
        val name = buffer.gjstr()
        return FriendChatSetRank(
            name,
            rank,
        )
    }
}
