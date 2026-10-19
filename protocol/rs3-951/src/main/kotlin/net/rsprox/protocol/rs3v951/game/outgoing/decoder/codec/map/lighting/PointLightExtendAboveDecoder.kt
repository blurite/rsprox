package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.map.lighting

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightExtendAbove
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PointLightExtendAboveDecoder : ProxyMessageDecoder<PointLightExtendAbove> {
    override val prot: ClientProt = GameServerProt.POINTLIGHT_EXTEND_ABOVE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PointLightExtendAbove {
        val mode = buffer.g1Alt2()
        val id = buffer.g2Alt1().toShort().toInt()
        return PointLightExtendAbove(id, mode)
    }
}
