package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.ObjAdd
import net.rsprox.protocol.session.Session

internal class ObjAddDecoder(
    override val prot: ClientProt,
    private val big: Boolean,
) : ProxyMessageDecoder<ObjAdd> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjAdd {
        val objId: Int
        val count: Int
        val packedCoord: Int
        if (big) {
            objId = buffer.g3Alt3()
            count = buffer.g2Alt2()
            packedCoord = buffer.g1Alt2()
        } else {
            packedCoord = buffer.g1()
            count = buffer.g2Alt3()
            objId = buffer.g2()
        }
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        return ObjAdd(big, objId, count, xInZone, zInZone)
    }

    internal companion object {
        internal fun all(): List<ObjAddDecoder> =
            listOf(
                ObjAddDecoder(GameServerProt.OBJ_ADD, big = false),
                ObjAddDecoder(GameServerProt.OBJ_ADD_BIG, big = true),
            )
    }
}
