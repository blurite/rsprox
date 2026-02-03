package net.rsprox.protocol.v223.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtEasedCoordV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamLookAtEasedCoordDecoder : ProxyMessageDecoder<CamLookAtEasedCoordV1> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT_EASED_COORD_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamLookAtEasedCoordV1 {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val duration = buffer.g2()
        val function = buffer.g1()
        return CamLookAtEasedCoordV1(
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            duration,
            function,
        )
    }
}
