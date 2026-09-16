package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightColour
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PointLightColourDecoder : ProxyMessageDecoder<PointLightColour> {
    override val prot: ClientProt = GameServerProt.POINTLIGHT_COLOUR

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PointLightColour {
        val colour = buffer.g4()
        val duration = buffer.g2Alt3()
        val id = buffer.g2Alt1().toShort().toInt()
        return PointLightColour(
            colour,
            duration,
            id,
        )
    }
}
