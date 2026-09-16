package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.IgnoreListAdd
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class IgnoreListAddDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<IgnoreListAdd> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IgnoreListAdd {
        val name = buffer.readNativeString()
        val temporary = buffer.g1()
        return IgnoreListAdd(
            name,
            temporary,
        )
    }
}
