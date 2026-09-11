package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.objs.OpObj
import net.rsprox.protocol.rs3v950.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.session.Session

internal class OpObjDecoder(
    override val prot: ClientProt,
    private val op: Int,
) : ProxyMessageDecoder<OpObj> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObj {
        val run = (buffer.g1Alt3() and 1) != 0
        val x = buffer.g2()
        val y = buffer.g2()
        val objId = buffer.g3()
        return OpObj(objId, x, y, op, run)
    }

    internal companion object {
        internal fun all(): List<OpObjDecoder> =
            listOf(
                OpObjDecoder(GameClientProt.OPOBJ1_V2, 1),
                OpObjDecoder(GameClientProt.OPOBJ2_V2, 2),
                OpObjDecoder(GameClientProt.OPOBJ3_V2, 3),
                OpObjDecoder(GameClientProt.OPOBJ4_V2, 4),
                OpObjDecoder(GameClientProt.OPOBJ5_V2, 5),
                OpObjDecoder(GameClientProt.OPOBJ6_V2, 6),
            )
    }
}
