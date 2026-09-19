package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateAccountReply
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class CreateAccountReplyDecoder : ProxyMessageDecoder<CreateAccountReply> {
    override val prot: ClientProt = GameServerProt.CREATE_ACCOUNT_REPLY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CreateAccountReply {
        val response = buffer.g1()
        return CreateAccountReply(
            response,
        )
    }
}
