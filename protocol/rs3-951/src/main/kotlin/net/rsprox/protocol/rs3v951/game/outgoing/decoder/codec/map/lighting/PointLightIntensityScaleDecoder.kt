package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.map.lighting

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightIntensityScale
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PointLightIntensityScaleDecoder : ProxyMessageDecoder<PointLightIntensityScale> {
    override val prot: ClientProt = GameServerProt.POINTLIGHT_INTENSITYSCALE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PointLightIntensityScale {
        val id = buffer.g2().toShort().toInt()
        val duration = buffer.g2Alt3()
        val intensity = buffer.g1Alt1()
        return PointLightIntensityScale(intensity, duration, id)
    }
}
