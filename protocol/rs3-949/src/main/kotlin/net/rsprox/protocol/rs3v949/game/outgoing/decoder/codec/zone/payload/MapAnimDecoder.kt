package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.MapAnim
import net.rsprox.protocol.session.Session

internal class MapAnimDecoder : ProxyMessageDecoder<MapAnim> {
    override val prot: ClientProt = GameServerProt.MAP_ANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapAnim {
        val startIndex = buffer.buffer.readerIndex()
        val packedCoord = buffer.g1()
        val id = buffer.g4()
        val byte5 = buffer.g1()
        val rotation = byte5 and 0x7
        val delay = buffer.g1()
        val height = buffer.g1()
        val scale = buffer.g2()
        val priority = buffer.g1()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)
        return MapAnim(id, xInZone, zInZone, rotation, delay, height, scale, priority, rawBytes)
    }
}
