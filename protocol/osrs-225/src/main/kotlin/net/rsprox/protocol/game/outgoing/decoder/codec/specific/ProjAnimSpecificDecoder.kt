package net.rsprox.protocol.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecific
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea
import net.rsprox.protocol.session.Session

public class ProjAnimSpecificDecoder : ProxyMessageDecoder<ProjAnimSpecific> {
    override val prot: ClientProt = GameServerProt.PROJANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ProjAnimSpecific {
        val angle = buffer.g1Alt1()
        val deltaZ = buffer.g1Alt1()
        val deltaX = buffer.g1()
        val sourceIndex = buffer.g3sAlt2()
        val progress = buffer.g2()
        val endTime = buffer.g2Alt1()
        val endHeight = buffer.g1Alt1()
        val targetIndex = buffer.g3sAlt1()
        val id = buffer.g2Alt1()
        val startHeight = buffer.g1()
        val coordInBuildArea = CoordInBuildArea(buffer.g3())
        val startTime = buffer.g2Alt3()
        return ProjAnimSpecific(
            id,
            startHeight,
            endHeight,
            startTime,
            endTime,
            angle,
            progress,
            sourceIndex,
            targetIndex,
            coordInBuildArea,
            deltaX,
            deltaZ,
        )
    }
}
