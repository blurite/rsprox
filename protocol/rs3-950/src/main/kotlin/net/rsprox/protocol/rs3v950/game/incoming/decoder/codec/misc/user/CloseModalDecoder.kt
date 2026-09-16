package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.CloseModal
import net.rsprox.protocol.session.Session

internal class CloseModalDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<CloseModal> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CloseModal {
        return CloseModal
    }
}
