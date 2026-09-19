package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.account.FriendlistLoaded
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class FriendlistLoadedDecoder : ProxyMessageDecoder<FriendlistLoaded> {
    override val prot: ClientProt = GameServerProt.FRIENDLIST_LOADED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendlistLoaded = FriendlistLoaded
}
