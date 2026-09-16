package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateCheckEmailReply
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class CreateCheckEmailReplyDecoder : ProxyMessageDecoder<CreateCheckEmailReply> {
    override val prot: ClientProt = GameServerProt.CREATE_CHECK_EMAIL_REPLY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CreateCheckEmailReply {
        val response = buffer.g1()
        return CreateCheckEmailReply(
            response,
        )
    }
}
