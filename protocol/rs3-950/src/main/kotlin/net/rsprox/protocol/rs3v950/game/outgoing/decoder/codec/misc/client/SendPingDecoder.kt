package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.SendPing
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

@Consistent
internal class SendPingDecoder : ProxyMessageDecoder<SendPing> {
    override val prot: ClientProt = GameServerProt.SEND_PING

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SendPing {
        val echo0 = buffer.g4()
        val echo1 = buffer.g4()
        return SendPing(
            echo0,
            echo1,
        )
    }
}
