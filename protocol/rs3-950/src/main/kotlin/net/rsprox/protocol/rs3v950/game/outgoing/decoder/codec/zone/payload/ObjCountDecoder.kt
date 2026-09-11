package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ObjCount
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ObjCountDecoder : ProxyMessageDecoder<ObjCount> {
    override val prot: ClientProt = GameServerProt.OBJ_COUNT_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjCount {
        val startIndex = buffer.buffer.readerIndex()
        val packedCoord = buffer.g1()
        val objId = buffer.g3()
        val oldQuantity = buffer.g2()
        val newQuantity = buffer.g2()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)
        return ObjCount(true, objId, oldQuantity, newQuantity, xInZone, zInZone, rawBytes)
    }
}
