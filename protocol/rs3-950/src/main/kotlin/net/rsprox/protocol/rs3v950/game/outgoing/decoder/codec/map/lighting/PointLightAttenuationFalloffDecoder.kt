package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightAttenuationFalloff
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PointLightAttenuationFalloffDecoder : ProxyMessageDecoder<PointLightAttenuationFalloff> {
    override val prot: ClientProt = GameServerProt.POINTLIGHT_ATTENUATION_FALLOFF

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PointLightAttenuationFalloff {
        val falloff = buffer.g2()
        val id = buffer.g2().toShort().toInt()
        return PointLightAttenuationFalloff(
            falloff,
            id,
        )
    }
}
