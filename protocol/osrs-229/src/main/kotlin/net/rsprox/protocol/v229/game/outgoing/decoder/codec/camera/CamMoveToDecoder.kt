package net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveTo
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v229.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamMoveToDecoder : ProxyMessageDecoder<CamMoveTo> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveTo {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val speed = buffer.g1()
        val acceleration = buffer.g1()
        return CamMoveTo(
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            speed,
            acceleration,
        )
    }
}
