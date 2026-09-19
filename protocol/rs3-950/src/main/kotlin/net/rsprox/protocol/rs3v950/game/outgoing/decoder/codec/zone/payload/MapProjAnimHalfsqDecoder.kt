package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapProjAnimHalfsq
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MapProjAnimHalfsqDecoder : ProxyMessageDecoder<MapProjAnimHalfsq> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM_HALFSQ

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnimHalfsq {
        val coordinate = buffer.g1()
        val flags = buffer.g1()
        val deltaX = buffer.g1s()
        val deltaZ = buffer.g1s()
        val source = buffer.g3()
        val target = buffer.g3()
        val id = buffer.g2()
        val startHeight = buffer.g1()
        val endHeight = buffer.g1()
        val startTime = buffer.g2()
        val endTime = buffer.g2()
        // The native handler maps 255 to -1; this does not establish a straight-line flight mode.
        val angle = buffer.g1().let { if (it == 255) -1 else it }
        val progress = buffer.g2()
        return MapProjAnimHalfsq(
            coordinate = coordinate,
            flags = flags,
            deltaX = deltaX,
            deltaZ = deltaZ,
            source = source,
            target = target,
            id = id,
            startHeight = startHeight,
            endHeight = endHeight,
            startTime = startTime,
            endTime = endTime,
            angle = angle,
            progress = progress,
        )
    }
}
