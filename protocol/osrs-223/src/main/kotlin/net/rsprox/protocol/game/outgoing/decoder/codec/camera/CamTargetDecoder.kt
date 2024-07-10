package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamTarget

@Consistent
public class CamTargetDecoder : MessageDecoder<CamTarget> {
    override val prot: ClientProt = GameServerProt.CAM_TARGET

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): CamTarget {
        val type = buffer.g1()
        val index = buffer.g2()
        val cameraLockedPlayerIndex = buffer.g2()
        return CamTarget(
            when (type) {
                0 ->
                    CamTarget.PlayerCamTarget(index)
                1 ->
                    CamTarget.NpcCamTarget(index)
                2 ->
                    CamTarget.WorldEntityTarget(
                        index,
                        cameraLockedPlayerIndex,
                    )
                else -> throw IllegalStateException("Unknown type: $type")
            },
        )
    }
}
