package net.rsprox.protocol.v224.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.crc.CyclicRedundancyCheck
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateUid192
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class UpdateUid192Decoder : ProxyMessageDecoder<UpdateUid192> {
    override val prot: ClientProt = GameServerProt.UPDATE_UID192

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
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
