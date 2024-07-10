package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.crc.CyclicRedundancyCheck
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateUid192

@Consistent
public class UpdateUid192Decoder : MessageDecoder<UpdateUid192> {
    override val prot: ClientProt = GameServerProt.UPDATE_UID192

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): UpdateUid192 {
        val data = ByteArray(24)
        buffer.gdata(data)
        val expectedCrc = buffer.g4()
        val crc = CyclicRedundancyCheck.computeCrc32(data)
        if (expectedCrc != crc) {
            throw IllegalStateException("CRC Mismatch: $expectedCrc != $crc")
        }
        return UpdateUid192(
            data,
        )
    }
}
