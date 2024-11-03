package net.rsprox.protocol.v224.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToCycles
import net.rsprox.protocol.session.Session

@Consistent
public class CamMoveToCyclesDecoder : ProxyMessageDecoder<CamMoveToCycles> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_CYCLES

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveToCycles {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val duration = buffer.g2()
        val maintainFixedAltitude = buffer.gboolean()
        val function = buffer.g1()
        return CamMoveToCycles(
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            duration,
            maintainFixedAltitude,
            function,
        )
    }
}
