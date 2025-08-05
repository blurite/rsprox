package net.rsprox.protocol.v232.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.MapProjAnimV1
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.outgoing.decoder.prot.GameServerProt

internal class MapProjAnimV1Decoder : ProxyMessageDecoder<MapProjAnimV1> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnimV1 {
        val coordInZone = CoordInZone(buffer.g1())
        val deltaX = buffer.g1Alt2()
        val startHeight = buffer.g1()
        val id = buffer.g2()
        val angle = buffer.g1Alt1()
        val progress = buffer.g2()
        val endHeight = buffer.g1Alt1()
        val deltaZ = buffer.g1Alt1()
        val targetIndex = buffer.g3sAlt3()
        val startTime = buffer.g2()
        val endTime = buffer.g2Alt2()
        val sourceIndex = buffer.g3sAlt1()
        return MapProjAnimV1(
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
            deltaX,
            deltaZ,
        )
    }
}
