package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

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
        val fps = buffer.g1Alt1()
        val challengeA = buffer.g4()
        val challengeB = buffer.g4Alt3()
        return SendPingReply(
            fps,
            challengeA,
            challengeB,
        )
    }
}
