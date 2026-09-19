package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapProjAnim
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MapProjAnimDecoder : ProxyMessageDecoder<MapProjAnim> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnim {
        val coordinate = buffer.g1()
        val deltaX = buffer.g1s()
        val deltaZ = buffer.g1s()
        val target = buffer.g3()
        val id = buffer.g2()
        val startHeight = buffer.g1s()
        val endHeight = buffer.g1s()
        val startTime = buffer.g2()
        val endTime = buffer.g2()
        val angle = buffer.g1().let { if (it == 255) -1 else it }
        val progress = buffer.g2()
        val unused0 = buffer.g1()
        val unused1 = buffer.g1()
        val unused2 = buffer.g1()
        return MapProjAnim(
            coordinate = coordinate,
            deltaX = deltaX,
            deltaZ = deltaZ,
            target = target,
            id = id,
            startHeight = startHeight,
            endHeight = endHeight,
            startTime = startTime,
            endTime = endTime,
            angle = angle,
            progress = progress,
            unused0 = unused0,
            unused1 = unused1,
            unused2 = unused2,
        )
    }
}
