package net.rsprox.protocol.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.zone.payload.MapProjAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session

public class MapProjAnimDecoder : ProxyMessageDecoder<MapProjAnim> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnim {
        val angle = buffer.g1()
        val endTime = buffer.g2Alt1()
        val targetIndex = buffer.g3Alt3()
        val coordInZone = CoordInZone(buffer.g1Alt2())
        val endHeight = buffer.g1Alt1()
        val startHeight = buffer.g1Alt3()
        val progress = buffer.g2()
        val startTime = buffer.g2Alt3()
        val sourceIndex = buffer.g3Alt2()
        val deltaX = buffer.g1()
        val deltaZ = buffer.g1Alt1()
        val id = buffer.g2Alt3()
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
