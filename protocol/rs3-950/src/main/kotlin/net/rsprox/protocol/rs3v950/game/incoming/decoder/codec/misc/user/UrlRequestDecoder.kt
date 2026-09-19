package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.UrlRequest
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class UrlRequestDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<UrlRequest> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UrlRequest {
        val url = buffer.readNativeString()
        val textA = buffer.readNativeString()
        val textB = buffer.readNativeString()
        val flags = buffer.g1()
        return UrlRequest(
            url,
            textA,
            textB,
            flags,
        )
    }
}
