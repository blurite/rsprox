package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.account.CreateSuggestNames
import net.rsprox.protocol.session.Session

internal class CreateSuggestNamesDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<CreateSuggestNames> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CreateSuggestNames {
        return CreateSuggestNames
    }
}
