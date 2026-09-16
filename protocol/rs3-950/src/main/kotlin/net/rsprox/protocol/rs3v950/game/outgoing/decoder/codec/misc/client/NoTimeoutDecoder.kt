package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.NoTimeout
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

@Consistent
internal class NoTimeoutDecoder : ProxyMessageDecoder<NoTimeout> {
    override val prot: ClientProt = GameServerProt.NO_TIMEOUT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NoTimeout = NoTimeout
}
