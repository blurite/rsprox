package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.MapProjAnimHalfsq
import net.rsprox.protocol.session.Session

internal class MapProjAnimHalfsqDecoder : ProxyMessageDecoder<MapProjAnimHalfsq> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM_HALFSQ

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnimHalfsq {
        val startIndex = buffer.buffer.readerIndex()
        val srcCoordHalf = buffer.g1()
        val destYdeltaHalf = buffer.g1s()
        val destXdeltaHalf = buffer.g1s()
        val srcModel = buffer.g3()
        val idB = buffer.g3()
        val heightByte = buffer.g1()
        val spotAnimId = buffer.g2()
        val heightByte2 = buffer.g1()
        val trailing = ByteArray(buffer.readableBytes())
        buffer.buffer.readBytes(trailing)
        val xInZone = (srcCoordHalf ushr 3) and 0x7
        val zInZone = srcCoordHalf and 0x7
        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)
        return MapProjAnimHalfsq(
            srcCoordHalf, xInZone, zInZone, destXdeltaHalf, destYdeltaHalf,
            srcModel, idB, spotAnimId, heightByte, heightByte2, trailing, rawBytes,
        )
    }
}
