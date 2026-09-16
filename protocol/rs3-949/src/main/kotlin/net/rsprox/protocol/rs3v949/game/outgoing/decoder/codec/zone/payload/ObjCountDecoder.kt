package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ObjCount
import net.rsprox.protocol.session.Session

internal class ObjCountDecoder(
    override val prot: ClientProt,
    private val big: Boolean,
) : ProxyMessageDecoder<ObjCount> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjCount {
        val packedCoord = buffer.g1()
        val objId = if (big) buffer.g3() else buffer.g2()
        val oldQuantity = buffer.g2()
        val newQuantity = buffer.g2()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        return ObjCount(big, objId, oldQuantity, newQuantity, xInZone, zInZone)
    }

    internal companion object {
        internal fun all(): List<ObjCountDecoder> =
            listOf(
                ObjCountDecoder(GameServerProt.OBJ_COUNT, big = false),
                ObjCountDecoder(GameServerProt.OBJ_COUNT_V2, big = true),
            )
    }
}
