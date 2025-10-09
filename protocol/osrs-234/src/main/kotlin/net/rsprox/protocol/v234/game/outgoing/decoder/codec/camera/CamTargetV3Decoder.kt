package net.rsprox.protocol.v234.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetV3
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamTargetV3Decoder : ProxyMessageDecoder<CamTargetV3> {
    override val prot: ClientProt = GameServerProt.CAM_TARGET_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamTargetV3 {
        val type = buffer.g1()
        val worldEntityIndex = buffer.g2s()
        val targetIndex = buffer.g2()
        return CamTargetV3(
            when (type) {
                0 -> CamTargetV3.PlayerCamTarget(worldEntityIndex, if (targetIndex == 65535) -1 else targetIndex)
                1 -> CamTargetV3.NpcCamTarget(worldEntityIndex, if (targetIndex == 65535) -1 else targetIndex)
                2 -> CamTargetV3.WorldEntityTarget(worldEntityIndex, if (targetIndex == 65535) -1 else targetIndex)
                else -> throw IllegalStateException("Unknown type: $type")
            },
        )
    }
}
