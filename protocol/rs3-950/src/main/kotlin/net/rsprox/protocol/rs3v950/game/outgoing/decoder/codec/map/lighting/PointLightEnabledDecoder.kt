package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightEnabled
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PointLightEnabledDecoder : ProxyMessageDecoder<PointLightEnabled> {
    override val prot: ClientProt = GameServerProt.POINTLIGHT_ENABLED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PointLightEnabled {
        val encodedControl = buffer.g1()
        val id = buffer.g2Alt2().toShort().toInt()
        // Only these control bits have native semantic evidence; preserve the wire byte as well.
        val mode = -encodedControl and 0xF
        val preserveIntensity = -encodedControl and 0x10 != 0
        return PointLightEnabled(
            encodedControl,
            id,
            mode,
            preserveIntensity,
        )
    }
}
