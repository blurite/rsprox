package net.rsprox.protocol.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.social.IgnoreListDel
import net.rsprox.protocol.session.Session

@Consistent
public class IgnoreListDelDecoder : ProxyMessageDecoder<IgnoreListDel> {
    override val prot: ClientProt = GameClientProt.IGNORELIST_DEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IgnoreListDel {
        val name = buffer.gjstr()
        return IgnoreListDel(name)
    }
}
