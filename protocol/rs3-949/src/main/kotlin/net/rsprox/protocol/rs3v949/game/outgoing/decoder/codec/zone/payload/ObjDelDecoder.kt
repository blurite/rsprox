package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.ObjDel
import net.rsprox.protocol.session.Session

internal class ObjDelDecoder(
    override val prot: ClientProt,
    private val big: Boolean,
) : ProxyMessageDecoder<ObjDel> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjDel {
        val objId: Int
        val packedCoord: Int
        if (big) {
            objId = buffer.g3()
            packedCoord = buffer.g1Alt3()
        } else {
            packedCoord = buffer.g1()
            objId = buffer.g2Alt1()
        }
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        return ObjDel(big, objId, xInZone, zInZone)
    }

    internal companion object {
        internal fun all(): List<ObjDelDecoder> =
            listOf(
                ObjDelDecoder(GameServerProt.OBJ_DEL, big = false),
                ObjDelDecoder(GameServerProt.OBJ_DEL_V2, big = true),
            )
    }
}
