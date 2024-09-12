package net.rsprox.protocol.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.SendPingReply
import net.rsprox.protocol.session.Session

public class SendPingReplyDecoder : ProxyMessageDecoder<SendPingReply> {
    override val prot: ClientProt = GameClientProt.SEND_PING_REPLY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SendPingReply {
        val value1 = buffer.g4Alt2()
        val value2 = buffer.g4Alt3()
        val fps = buffer.g1Alt1()
        val gcPercentTime = buffer.g1Alt3()
        return SendPingReply(
            fps,
            gcPercentTime,
            value1,
            value2,
        )
    }
}
