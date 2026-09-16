package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.NoTimeout
import net.rsprox.protocol.session.Session

internal class NoTimeoutDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<NoTimeout> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NoTimeout {
        return NoTimeout
    }
}
