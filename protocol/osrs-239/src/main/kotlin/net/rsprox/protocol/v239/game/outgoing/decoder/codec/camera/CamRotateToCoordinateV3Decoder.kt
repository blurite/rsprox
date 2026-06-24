package net.rsprox.protocol.v239.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamRotateToCoordinateV3
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

internal class CamRotateToCoordinateV3Decoder : ProxyMessageDecoder<CamRotateToCoordinateV3> {
    override val prot: ClientProt = GameServerProt.CAM_ROTATETO_COORDINATE_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamRotateToCoordinateV3 {
        val trackTarget = buffer.g1Alt1() == 1
        val z = buffer.g2Alt1()
        val heightRelative = buffer.g1() == 1
        val x = buffer.g2Alt3()
        val cycles = buffer.g2Alt1()
        val height = buffer.g2sAlt3()
        val easing = buffer.g1()
        return CamRotateToCoordinateV3(
            x,
            z,
            height,
            cycles,
            easing,
            heightRelative,
            trackTarget,
        )
    }
}