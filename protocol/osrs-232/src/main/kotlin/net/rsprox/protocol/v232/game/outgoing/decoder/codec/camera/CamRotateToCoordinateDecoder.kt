package net.rsprox.protocol.v232.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamRotateToCoordinateV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamRotateToCoordinateDecoder : ProxyMessageDecoder<CamRotateToCoordinateV1> {
    override val prot: ClientProt = GameServerProt.CAM_ROTATETO_COORDINATE_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamRotateToCoordinateV1 {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val duration = buffer.g2()
        val function = buffer.g1()
        return CamRotateToCoordinateV1(
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            duration,
            function,
        )
    }
}
