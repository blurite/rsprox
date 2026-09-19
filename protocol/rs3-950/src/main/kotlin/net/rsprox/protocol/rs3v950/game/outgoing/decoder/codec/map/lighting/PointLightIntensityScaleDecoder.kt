package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightIntensityScale
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PointLightIntensityScaleDecoder : ProxyMessageDecoder<PointLightIntensityScale> {
    override val prot: ClientProt = GameServerProt.POINTLIGHT_INTENSITYSCALE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PointLightIntensityScale {
        val intensity = buffer.g1Alt2()
        val duration = buffer.g2()
        val id = buffer.g2Alt3().toShort().toInt()
        return PointLightIntensityScale(
            intensity,
            duration,
            id,
        )
    }
}
