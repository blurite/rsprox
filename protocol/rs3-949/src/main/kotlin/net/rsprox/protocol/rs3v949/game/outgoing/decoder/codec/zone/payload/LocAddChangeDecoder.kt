package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.LocAddChange
import net.rsprox.protocol.session.Session

internal class LocAddChangeDecoder : ProxyMessageDecoder<LocAddChange> {
    override val prot: ClientProt = GameServerProt.LOC_ADD_CHANGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocAddChange {
        val startIndex = buffer.buffer.readerIndex()
        val locFlags = buffer.g1Alt2()
        val packedCoord = buffer.g1Alt1()
        val locId = buffer.g4Alt2()
        val rotData = buffer.g1Alt1()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        val shape = (rotData ushr 2) and 0x1F
        val rotation = rotData and 0x3
        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)
        return LocAddChange(locId, xInZone, zInZone, shape, rotation, rotData, locFlags, rawBytes)
    }
}
