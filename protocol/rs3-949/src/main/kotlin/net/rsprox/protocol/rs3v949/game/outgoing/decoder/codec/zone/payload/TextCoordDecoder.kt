package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.TextCoord
import net.rsprox.protocol.session.Session

internal class TextCoordDecoder : ProxyMessageDecoder<TextCoord> {
    override val prot: ClientProt = GameServerProt.TEXT_COORD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TextCoord {
        val startIndex = buffer.buffer.readerIndex()
        val param1 = buffer.g2()
        val packedCoord = buffer.g1()
        val rgb = buffer.g3()
        val text = buffer.gjstr()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)
        return TextCoord(param1, xInZone, zInZone, rgb, text, rawBytes)
    }
}
