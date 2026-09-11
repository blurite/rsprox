package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapAnimV2
import net.rsprox.protocol.session.Session

internal class MapAnimV2Decoder : ProxyMessageDecoder<MapAnimV2> {
    override val prot: ClientProt = GameServerProt.MAP_ANIM_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapAnimV2 {
        val startIndex = buffer.buffer.readerIndex()
        val packedCoord = buffer.g1()
        val id = buffer.g2()
        val height = buffer.g2s()
        val delay = buffer.g2()
        val rotation = buffer.g1()
        buffer.skipRead(3)
        val fineOffsetPacked = buffer.g3()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)
        return MapAnimV2(xInZone, zInZone, id, height, delay, rotation, fineOffsetPacked, rawBytes)
    }
}
