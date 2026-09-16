package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightShadow
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PointLightShadowDecoder : ProxyMessageDecoder<PointLightShadow> {
    override val prot: ClientProt = GameServerProt.POINTLIGHT_SHADOW

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PointLightShadow {
        val id = buffer.g2Alt3().toShort().toInt()
        val encodedControl = buffer.g1()
        // Only these control bits have native semantic evidence; preserve the wire byte as well.
        val mode = -encodedControl and 0xF
        return PointLightShadow(
            id,
            encodedControl,
            mode,
        )
    }
}
