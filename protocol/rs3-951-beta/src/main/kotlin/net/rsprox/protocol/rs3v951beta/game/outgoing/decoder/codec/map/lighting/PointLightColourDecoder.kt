package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.map.lighting

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightColour
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PointLightColourDecoder : ProxyMessageDecoder<PointLightColour> {
    override val prot: ClientProt = GameServerProt.POINTLIGHT_COLOUR

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PointLightColour {
        val colour = buffer.g4Alt2()
        val duration = buffer.g2Alt2()
        val id = buffer.g2().toShort().toInt()
        return PointLightColour(colour, duration, id)
    }
}
