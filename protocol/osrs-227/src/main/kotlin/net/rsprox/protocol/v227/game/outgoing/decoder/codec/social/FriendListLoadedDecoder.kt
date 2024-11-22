package net.rsprox.protocol.v227.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.social.FriendListLoaded
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class FriendListLoadedDecoder : ProxyMessageDecoder<FriendListLoaded> {
    override val prot: ClientProt = GameServerProt.FRIENDLIST_LOADED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendListLoaded {
        return FriendListLoaded
    }
}
