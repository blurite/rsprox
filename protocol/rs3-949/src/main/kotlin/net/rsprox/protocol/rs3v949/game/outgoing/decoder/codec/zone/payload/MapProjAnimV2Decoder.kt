package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapProjAnimV2
import net.rsprox.protocol.session.Session

internal class MapProjAnimV2Decoder : ProxyMessageDecoder<MapProjAnimV2> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnimV2 {
        val startIndex = buffer.buffer.readerIndex()
        val srcCoord = buffer.g1()
        val targetDeltaY = buffer.g1s()
        val targetDeltaX = buffer.g1s()
        val idMedium = buffer.g3()
        val spotAnimId = buffer.g2()
        val trailing = ByteArray(TRAILING_LENGTH)
        buffer.buffer.readBytes(trailing)
        val xInZone = (srcCoord ushr 3) and 0x7
        val zInZone = srcCoord and 0x7
        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)
        return MapProjAnimV2(xInZone, zInZone, targetDeltaX, targetDeltaY, idMedium, spotAnimId, trailing, rawBytes)
    }

    private companion object {
        const val TRAILING_LENGTH = 20
    }
}
