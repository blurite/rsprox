@file:Suppress("DEPRECATION")

package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetOld

@Consistent
public class CamTargetOldDecoder : MessageDecoder<CamTargetOld> {
    override val prot: ClientProt = GameServerProt.CAM_TARGET_OLD

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): CamTargetOld {
        val type = buffer.g1()
        val index = buffer.g2()
        return CamTargetOld(
            when (type) {
                0 ->
                    CamTargetOld.PlayerCamTarget(index)
                1 ->
                    CamTargetOld.NpcCamTarget(index)
                2 ->
                    CamTargetOld.WorldEntityTarget(
                        index,
                    )
                else -> throw IllegalStateException("Unknown type: $type")
            },
        )
    }
}
