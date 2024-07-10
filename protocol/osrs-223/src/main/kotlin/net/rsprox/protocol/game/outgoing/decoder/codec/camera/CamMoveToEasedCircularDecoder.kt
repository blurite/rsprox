package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToEasedCircular

@Consistent
public class CamMoveToEasedCircularDecoder : MessageDecoder<CamMoveToEasedCircular> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_EASED_CIRCULAR

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): CamMoveToEasedCircular {
        val destinationXInBuildArea = buffer.g1()
        val destinationZInBuildArea = buffer.g1()
        val height = buffer.g2()
        val centerXInBuildArea = buffer.g1()
        val centerZInBuildArea = buffer.g1()
        val duration = buffer.g2()
        val maintainFixedAltitude = !buffer.gboolean()
        val function = buffer.g1()
        return CamMoveToEasedCircular(
            centerXInBuildArea,
            centerZInBuildArea,
            destinationXInBuildArea,
            destinationZInBuildArea,
            height,
            duration,
            maintainFixedAltitude,
            function,
        )
    }
}
