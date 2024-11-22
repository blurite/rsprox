package net.rsprox.protocol.v227.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.MapProjAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

internal class MapProjAnimDecoder : ProxyMessageDecoder<MapProjAnim> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnim {
        val sourceIndex = buffer.g3sAlt3()
        val endTime = buffer.g2Alt2()
        val id = buffer.g2()
        val startTime = buffer.g2Alt2()
        val coordInZone = CoordInZone(buffer.g1Alt2())
        val progress = buffer.g2Alt2()
        val targetIndex = buffer.g3sAlt2()
        val startHeight = buffer.g1()
        val deltaX = buffer.g1Alt3()
        val endHeight = buffer.g1Alt3()
        val deltaZ = buffer.g1Alt3()
        val angle = buffer.g1Alt3()
        return MapProjAnim(
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
