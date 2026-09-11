package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.TickEnd
import net.rsprox.protocol.session.Session

internal class TickEndDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<TickEnd> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TickEnd = TickEnd()
}
