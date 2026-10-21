package net.rsprox.protocol.rs3v951beta.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.SendPingReply
import net.rsprox.protocol.session.Session

internal class SendPingReplyDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<SendPingReply> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SendPingReply {
        val challengeB = buffer.g4()
        val fps = buffer.g1()
        val challengeA = buffer.g4()
        return SendPingReply(
            fps,
            challengeA,
            challengeB,
        )
    }
}
