package net.rsprox.protocol.v233.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToArcV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamMoveToArcDecoder : ProxyMessageDecoder<CamMoveToArcV1> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_ARC_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveToArcV1 {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val centerXInBuildArea = buffer.g1()
        val centerZInBuildArea = buffer.g1()
        val duration = buffer.g2()
        val maintainFixedAltitude = buffer.gboolean()
        val function = buffer.g1()
        return CamMoveToArcV1(
            centerXInBuildArea,
            centerZInBuildArea,
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            duration,
            maintainFixedAltitude,
            function,
        )
    }
}
