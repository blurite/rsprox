package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateSuggestNameError
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class CreateSuggestNameErrorDecoder : ProxyMessageDecoder<CreateSuggestNameError> {
    override val prot: ClientProt = GameServerProt.CREATE_SUGGEST_NAME_ERROR

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CreateSuggestNameError {
        val response = buffer.g1()
        return CreateSuggestNameError(
            response,
        )
    }
}
