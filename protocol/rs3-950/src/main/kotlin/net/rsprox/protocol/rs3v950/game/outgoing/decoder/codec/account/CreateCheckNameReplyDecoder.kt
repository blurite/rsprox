package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateCheckNameReply
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class CreateCheckNameReplyDecoder : ProxyMessageDecoder<CreateCheckNameReply> {
    override val prot: ClientProt = GameServerProt.CREATE_CHECK_NAME_REPLY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CreateCheckNameReply {
        val response = buffer.g1()
        return CreateCheckNameReply(
            response,
        )
    }
}
