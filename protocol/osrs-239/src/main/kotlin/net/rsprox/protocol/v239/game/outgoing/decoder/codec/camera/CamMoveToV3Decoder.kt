package net.rsprox.protocol.v239.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToV3
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

internal class CamMoveToV3Decoder : ProxyMessageDecoder<CamMoveToV3> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveToV3 {
        val z = buffer.g2()
        val heightRelative = buffer.g1() == 1
        val rate = buffer.g1()
        val height = buffer.g2sAlt2()
        val x = buffer.g2Alt1()
        val rate2 = buffer.g1Alt1()
        return CamMoveToV3(
            x,
            z,
            height,
            rate,
            rate2,
            heightRelative,
        )
    }
}