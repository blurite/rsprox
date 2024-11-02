@file:Suppress("DEPRECATION")

package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetV1
import net.rsprox.protocol.session.Session

@Consistent
public class CamTargetV1Decoder : ProxyMessageDecoder<CamTargetV1> {
    override val prot: ClientProt = GameServerProt.CAM_TARGET_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamTargetV1 {
        val type = buffer.g1()
        val index = buffer.g2()
        return CamTargetV1(
            when (type) {
                0 ->
                    CamTargetV1.PlayerCamTarget(index)
                1 ->
                    CamTargetV1.NpcCamTarget(index)
                2 ->
                    CamTargetV1.WorldEntityTarget(
                        index,
                    )
                else -> throw IllegalStateException("Unknown type: $type")
            },
        )
    }
}
