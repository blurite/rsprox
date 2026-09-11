package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.session.Session

internal class ProjAnimSpecificV2Decoder : ProxyMessageDecoder<ProjAnimSpecificV2> {
    override val prot: ClientProt = GameServerProt.PROJANIM_SPECIFIC_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ProjAnimSpecificV2 {
        val field1 = buffer.g2()
        val field2 = buffer.g3()
        val spotAnimId = buffer.g2Alt1()
        val field4 = buffer.g2()
        val field5 = buffer.g3()
        val field6 = buffer.g3()
        val field7 = buffer.g1()
        val field8 = buffer.g1()
        val field9 = buffer.g1()
        val field10 = buffer.g2()
        val field11 = buffer.g1()
        val field12 = buffer.g2()
        val field13 = buffer.g3()
        val field14 = buffer.g2()
        val field15 = buffer.g2()
        val field16 = buffer.g2()
        val field17 = buffer.g1()
        return ProjAnimSpecificV2(
            spotAnimId,
            field1,
            field2,
            field4,
            field5,
            field6,
            field7,
            field8,
            field9,
            field10,
            field11,
            field12,
            field13,
            field14,
            field15,
            field16,
            field17,
        )
    }
}
