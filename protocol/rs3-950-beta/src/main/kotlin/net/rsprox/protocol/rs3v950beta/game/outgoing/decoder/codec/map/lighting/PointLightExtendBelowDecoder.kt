package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.lighting

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightExtendBelow
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PointLightExtendBelowDecoder : ProxyMessageDecoder<PointLightExtendBelow> {
    override val prot: ClientProt = GameServerProt.POINTLIGHT_EXTEND_BELOW

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PointLightExtendBelow {
        val mode = buffer.g1Alt1()
        val id = buffer.g2().toShort().toInt()
        return PointLightExtendBelow(id, mode)
    }
}
