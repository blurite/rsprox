package net.rsprox.protocol.game.incoming.decoder.codec.social
import net.rsprox.protocol.session.Session

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.social.FriendListDel
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprot.protocol.metadata.Consistent

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
