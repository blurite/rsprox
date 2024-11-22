package net.rsprox.protocol.v227.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV3
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

internal class ProjAnimSpecificV3Decoder : ProxyMessageDecoder<ProjAnimSpecificV3> {
    override val prot: ClientProt = GameServerProt.PROJANIM_SPECIFIC_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ProjAnimSpecificV3 {
        val coordInBuildArea = CoordInBuildArea(buffer.g3Alt1())
        val deltaZ = buffer.g1()
        val targetIndex = buffer.g3sAlt3()
        val progress = buffer.g2Alt1()
        val angle = buffer.g1Alt3()
        val endHeight = buffer.g1Alt2()
        val sourceIndex = buffer.g3sAlt2()
        val startTime = buffer.g2Alt2()
        val id = buffer.g2Alt1()
        val deltaX = buffer.g1()
        val endTime = buffer.g2Alt1()
        val startHeight = buffer.g1Alt1()
        return ProjAnimSpecificV3(
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
