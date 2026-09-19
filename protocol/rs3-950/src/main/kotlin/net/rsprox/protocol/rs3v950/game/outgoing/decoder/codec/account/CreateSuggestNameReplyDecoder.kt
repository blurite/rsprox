package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateSuggestNameReply
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class CreateSuggestNameReplyDecoder : ProxyMessageDecoder<CreateSuggestNameReply> {
    override val prot: ClientProt = GameServerProt.CREATE_SUGGEST_NAME_REPLY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CreateSuggestNameReply {
        val name = buffer.readNativeString()
        return CreateSuggestNameReply(name)
    }
}
