package net.rsprox.protocol.v235.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV4
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.outgoing.decoder.prot.GameServerProt

internal class ProjAnimSpecificV4Decoder : ProxyMessageDecoder<ProjAnimSpecificV4> {
    override val prot: ClientProt = GameServerProt.PROJANIM_SPECIFIC_V4

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ProjAnimSpecificV4 {
        val sourceIndex = buffer.g3sAlt2()
        val startTime = buffer.g2Alt3()
        val progress = buffer.g2()
        val endTime = buffer.g2Alt1()
        val endHeight = buffer.g2Alt1()
        val startHeight = buffer.g2Alt1()
        val angle = buffer.g1Alt3()
        val targetIndex = buffer.g3sAlt1()
        val id = buffer.g2Alt2()
        val start = CoordGrid(buffer.g4Alt2())
        val end = CoordGrid(buffer.g4Alt1())
        return ProjAnimSpecificV4(
            id,
            startHeight,
            endHeight,
            startTime,
            endTime,
            angle,
            progress,
            start,
            sourceIndex,
            end,
            targetIndex,
        )
    }
}
