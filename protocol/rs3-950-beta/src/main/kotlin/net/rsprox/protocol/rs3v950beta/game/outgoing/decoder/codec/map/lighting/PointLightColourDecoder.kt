package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.lighting

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightColour
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PointLightColourDecoder : ProxyMessageDecoder<PointLightColour> {
    override val prot: ClientProt = GameServerProt.POINTLIGHT_COLOUR

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PointLightColour {
        val id = buffer.g2().toShort().toInt()
        val duration = buffer.g2Alt1()
        val colour = buffer.g4Alt2()
        return PointLightColour(colour, duration, id)
    }
}
