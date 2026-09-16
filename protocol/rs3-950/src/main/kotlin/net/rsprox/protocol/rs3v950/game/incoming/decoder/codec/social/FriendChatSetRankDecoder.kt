package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendChatSetRank
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class FriendChatSetRankDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<FriendChatSetRank> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendChatSetRank {
        val name = buffer.readNativeString()
        val rank = buffer.g1()
        return FriendChatSetRank(
            name,
            rank,
        )
    }
}
