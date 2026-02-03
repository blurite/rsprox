package net.rsprox.protocol.v235.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamLookAtDecoder : ProxyMessageDecoder<CamLookAtV1> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamLookAtV1 {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val speed = buffer.g1()
        val acceleration = buffer.g1()
        return CamLookAtV1(
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            speed,
            acceleration,
        )
    }
}
