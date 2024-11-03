package net.rsprox.protocol.v225.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.social.FriendListDel
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

@Consistent
public class FriendListDelDecoder : ProxyMessageDecoder<FriendListDel> {
    override val prot: ClientProt = GameClientProt.FRIENDLIST_DEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendListDel {
        val name = buffer.gjstr()
        return FriendListDel(name)
    }
}
