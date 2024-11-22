package net.rsprox.protocol.v227.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamTargetV2Decoder : ProxyMessageDecoder<CamTargetV2> {
    override val prot: ClientProt = GameServerProt.CAM_TARGET_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamTargetV2 {
        val type = buffer.g1()
        val index = buffer.g2()
        val cameraLockedPlayerIndex = buffer.g2()
        return CamTargetV2(
            when (type) {
                0 ->
                    CamTargetV2.PlayerCamTarget(index)
                1 ->
                    CamTargetV2.NpcCamTarget(index)
                2 ->
                    CamTargetV2.WorldEntityTarget(
                        index,
                        cameraLockedPlayerIndex,
                    )
                else -> throw IllegalStateException("Unknown type: $type")
            },
        )
    }
}
