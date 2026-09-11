package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.TextCoord
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TextCoordDecoder : ProxyMessageDecoder<TextCoord> {
    override val prot: ClientProt = GameServerProt.TEXT_COORD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TextCoord {
        buffer.skipRead(1)
        val packedCoord = buffer.g1()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7

        val duration = buffer.g2()
        val height = buffer.g1()
        val rgb = buffer.g3()
        val text = buffer.gjstr()

        return TextCoord(
            duration = duration,
            xInZone = xInZone,
            zInZone = zInZone,
            height = height,
            rgb = rgb,
            text = text,
        )
    }
}
