package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamSmoothReset

@Consistent
public class CamSmoothResetDecoder : MessageDecoder<CamSmoothReset> {
    override val prot: ClientProt = GameServerProt.CAM_SMOOTHRESET

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): CamSmoothReset {
        val cameraMoveConstantSpeed = buffer.g1()
        val cameraMoveProportionalSpeed = buffer.g1()
        val cameraLookConstantSpeed = buffer.g1()
        val cameraLookProportionalSpeed = buffer.g1()
        return CamSmoothReset(
            cameraMoveConstantSpeed,
            cameraMoveProportionalSpeed,
            cameraLookConstantSpeed,
            cameraLookProportionalSpeed,
        )
    }
}
