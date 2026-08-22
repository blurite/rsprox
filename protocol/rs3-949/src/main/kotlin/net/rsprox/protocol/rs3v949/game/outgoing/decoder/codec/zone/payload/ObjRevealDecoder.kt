package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.ObjReveal
import net.rsprox.protocol.session.Session

internal class ObjRevealDecoder(
    override val prot: ClientProt,
    private val big: Boolean,
) : ProxyMessageDecoder<ObjReveal> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjReveal {
        val startIndex = buffer.buffer.readerIndex()
        val objId: Int
        val count: Int
        val packedCoord: Int
        val ownerIndex: Int
        if (big) {
            count = buffer.g2Alt1()
            objId = buffer.g3Alt1()
            packedCoord = buffer.g1Alt1()
            ownerIndex = buffer.g2Alt2()
        } else {
            objId = buffer.g2()
            count = buffer.g2Alt2()
            packedCoord = buffer.g1Alt2()
            ownerIndex = buffer.g2Alt1()
        }
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)
        return ObjReveal(big, objId, count, ownerIndex, xInZone, zInZone, rawBytes)
    }

    internal companion object {
        internal fun all(): List<ObjRevealDecoder> =
            listOf(
                ObjRevealDecoder(GameServerProt.OBJ_REVEAL, big = false),
                ObjRevealDecoder(GameServerProt.OBJ_REVEAL_BIG, big = true),
            )
    }
}
