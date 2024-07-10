package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamReset

@Consistent
public class CamResetDecoder : MessageDecoder<CamReset> {
    override val prot: ClientProt = GameServerProt.CAM_RESET

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): CamReset {
        return CamReset
    }
}
