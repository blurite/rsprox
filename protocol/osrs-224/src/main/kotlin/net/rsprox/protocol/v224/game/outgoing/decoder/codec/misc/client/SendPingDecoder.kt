package net.rsprox.protocol.v224.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.SendPing
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class SendPingDecoder : ProxyMessageDecoder<SendPing> {
    override val prot: ClientProt = GameServerProt.SEND_PING

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SendPing {
        val value1 = buffer.g4()
        val value2 = buffer.g4()
        return SendPing(
            value1,
            value2,
        )
    }
}
