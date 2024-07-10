package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamShake

@Consistent
public class CamShakeDecoder : MessageDecoder<CamShake> {
    override val prot: ClientProt = GameServerProt.CAM_SHAKE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): CamShake {
        val type = buffer.g1()
        val randomAmount = buffer.g1()
        val sineAmount = buffer.g1()
        val sineFrequency = buffer.g1()
        return CamShake(
            type,
            randomAmount,
            sineAmount,
            sineFrequency,
        )
    }
}
