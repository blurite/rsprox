package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToEased

@Consistent
public class CamMoveToEasedDecoder : MessageDecoder<CamMoveToEased> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_EASED

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): CamMoveToEased {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val duration = buffer.g2()
        val maintainFixedAltitude = !buffer.gboolean()
        val function = buffer.g1()
        return CamMoveToEased(
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            duration,
            maintainFixedAltitude,
            function,
        )
    }
}
