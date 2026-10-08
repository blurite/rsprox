package net.rsprox.protocol.rs3v950beta.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendSetRank
import net.rsprox.protocol.rs3v950beta.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class FriendSetRankDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<FriendSetRank> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendSetRank {
        val rank = buffer.g1Alt3()
        val name = buffer.readNativeString()
        return FriendSetRank(
            name,
            rank,
        )
    }
}
