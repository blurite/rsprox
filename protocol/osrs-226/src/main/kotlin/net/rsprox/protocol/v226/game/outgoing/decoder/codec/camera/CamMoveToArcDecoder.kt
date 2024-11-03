package net.rsprox.protocol.v226.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToArc
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamMoveToArcDecoder : ProxyMessageDecoder<CamMoveToArc> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_ARC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveToArc {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val centerXInBuildArea = buffer.g1()
        val centerZInBuildArea = buffer.g1()
        val duration = buffer.g2()
        val maintainFixedAltitude = buffer.gboolean()
        val function = buffer.g1()
        return CamMoveToArc(
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
