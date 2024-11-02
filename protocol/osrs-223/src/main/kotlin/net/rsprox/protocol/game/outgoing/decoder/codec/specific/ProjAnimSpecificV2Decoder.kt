package net.rsprox.protocol.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea
import net.rsprox.protocol.session.Session

public class ProjAnimSpecificV2Decoder : ProxyMessageDecoder<ProjAnimSpecificV2> {
    override val prot: ClientProt = GameServerProt.PROJANIM_SPECIFIC_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ProjAnimSpecificV2 {
        val angle = buffer.g1()
        val targetIndex = buffer.g3sAlt2()
        val deltaZ = buffer.g1Alt1()
        val endTime = buffer.g2Alt1()
        val endHeight = buffer.g1Alt2()
        val deltaX = buffer.g1Alt3()
        val startHeight = buffer.g1Alt3()
        val id = buffer.g2Alt1()
        val progress = buffer.g2Alt3()
        val startTime = buffer.g2Alt2()
        val coordInBuildArea = CoordInBuildArea(buffer.g3())
        return ProjAnimSpecificV2(
            id,
            startHeight,
            endHeight,
            startTime,
            endTime,
            angle,
            progress,
            targetIndex,
            coordInBuildArea,
            deltaX,
            deltaZ,
        )
    }
}
