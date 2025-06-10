package net.rsprox.protocol.v231.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.zone.payload.MapProjAnimV2
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

internal class MapProjAnimV2Decoder : ProxyMessageDecoder<MapProjAnimV2> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnimV2 {
        val endHeight = buffer.g1Alt1()
        val end = CoordGrid(buffer.g4())
        val startHeight = buffer.g1()
        val id = buffer.g2()
        val coordInZone = CoordInZone(buffer.g1())
        val startTime = buffer.g2()
        val targetIndex = buffer.g3sAlt3()
        val endTime = buffer.g2Alt2()
        val angle = buffer.g1Alt1()
        val progress = buffer.g2()
        val sourceIndex = buffer.g3sAlt1()
        return MapProjAnimV2(
            id,
            startHeight,
            endHeight,
            startTime,
            endTime,
            angle,
            progress,
            sourceIndex,
            targetIndex,
            coordInZone,
            end,
        )
    }
}
