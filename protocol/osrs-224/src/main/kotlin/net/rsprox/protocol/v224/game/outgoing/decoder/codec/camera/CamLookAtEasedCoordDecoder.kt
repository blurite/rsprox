package net.rsprox.protocol.v224.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtEasedCoord
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Consistent
public class CamLookAtEasedCoordDecoder : ProxyMessageDecoder<CamLookAtEasedCoord> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT_EASED_COORD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamLookAtEasedCoord {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val duration = buffer.g2()
        val function = buffer.g1()
        return CamLookAtEasedCoord(
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            duration,
            function,
        )
    }
}
