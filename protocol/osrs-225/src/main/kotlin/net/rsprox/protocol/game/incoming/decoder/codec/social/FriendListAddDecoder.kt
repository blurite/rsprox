package net.rsprox.protocol.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.social.FriendListAdd
import net.rsprox.protocol.session.Session

@Consistent
public class FriendListAddDecoder : ProxyMessageDecoder<FriendListAdd> {
    override val prot: ClientProt = GameClientProt.FRIENDLIST_ADD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendListAdd {
        val name = buffer.gjstr()
        return FriendListAdd(name)
    }
}
