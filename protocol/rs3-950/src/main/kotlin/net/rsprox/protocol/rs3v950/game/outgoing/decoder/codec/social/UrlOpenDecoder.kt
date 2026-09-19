package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.UrlOpen
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.readNativeIsaacString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UrlOpenDecoder(
    private val cipher: () -> StreamCipher?,
) : ProxyMessageDecoder<UrlOpen> {
    override val prot: ClientProt = GameServerProt.URL_OPEN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UrlOpen {
        val activeCipher = checkNotNull(cipher()) { "URL_OPEN requires the inbound opcode cipher" }
        val length = buffer.readableBytes()
        val mode = buffer.g1()
        val url = buffer.readNativeIsaacString(activeCipher, (length - 1).toLong())
        val preferredUrl =
            if (mode == 1) {
                // Native budget uses decoded UTF-8 length, not the first string's consumed wire length.
                val budget = (length - 1 - url.toByteArray(Charsets.UTF_8).size).toUInt().toLong()
                buffer.readNativeIsaacString(activeCipher, budget)
            } else {
                null
            }
        return UrlOpen(mode, url, preferredUrl)
    }
}
