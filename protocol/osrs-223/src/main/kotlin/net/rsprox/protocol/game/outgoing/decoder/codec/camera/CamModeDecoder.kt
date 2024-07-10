package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamMode

@Consistent
public class CamModeDecoder : MessageDecoder<CamMode> {
    override val prot: ClientProt = GameServerProt.CAM_MODE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): CamMode {
        val mode = buffer.g1()
        return CamMode(
            mode,
        )
    }
}
