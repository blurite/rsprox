package net.rsprox.protocol.v227.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.client.SendPingReply
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

internal class SendPingReplyDecoder : ProxyMessageDecoder<SendPingReply> {
    override val prot: ClientProt = GameClientProt.SEND_PING_REPLY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SendPingReply {
        val gcPercentTime = buffer.g1Alt3()
        val fps = buffer.g1()
        val value1 = buffer.g4Alt3()
        val value2 = buffer.g4Alt1()
        return SendPingReply(
            fps,
            gcPercentTime,
            value1,
            value2,
        )
    }
}
