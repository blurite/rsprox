package net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToCyclesV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v229.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamMoveToCyclesDecoder : ProxyMessageDecoder<CamMoveToCyclesV1> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_CYCLES_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveToCyclesV1 {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val duration = buffer.g2()
        val maintainFixedAltitude = buffer.gboolean()
        val function = buffer.g1()
        return CamMoveToCyclesV1(
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            duration,
            maintainFixedAltitude,
            function,
        )
    }
}
