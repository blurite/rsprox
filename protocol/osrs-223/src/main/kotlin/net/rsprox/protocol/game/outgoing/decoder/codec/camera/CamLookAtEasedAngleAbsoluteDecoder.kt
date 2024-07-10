package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtEasedAngleAbsolute

@Consistent
public class CamLookAtEasedAngleAbsoluteDecoder : MessageDecoder<CamLookAtEasedAngleAbsolute> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT_EASED_ANGLE_ABSOLUTE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): CamLookAtEasedAngleAbsolute {
        val yAngle = buffer.g2()
        val xAngle = buffer.g2()
        val duration = buffer.g2()
        val function = buffer.g1()
        return CamLookAtEasedAngleAbsolute(
            xAngle,
            yAngle,
            duration,
            function,
        )
    }
}
