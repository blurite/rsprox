package net.rsprox.protocol.v229.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.game.incoming.model.misc.user.CloseModal
import net.rsprox.protocol.v229.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CloseModalDecoder : ProxyMessageDecoder<CloseModal> {
    override val prot: ClientProt = GameClientProt.CLOSE_MODAL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CloseModal = CloseModal
}
