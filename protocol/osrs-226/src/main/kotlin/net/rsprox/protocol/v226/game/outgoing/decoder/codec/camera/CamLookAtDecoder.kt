package net.rsprox.protocol.v226.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAt
import net.rsprox.protocol.session.Session

@Consistent
public class CamLookAtDecoder : ProxyMessageDecoder<CamLookAt> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamLookAt {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val speed = buffer.g1()
        val acceleration = buffer.g1()
        return CamLookAt(
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            speed,
            acceleration,
        )
    }
}
