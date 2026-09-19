package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapProjAnimHalfsqV2
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ProjectileOffset
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MapProjAnimHalfsqV2Decoder : ProxyMessageDecoder<MapProjAnimHalfsqV2> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM_HALFSQ_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnimHalfsqV2 {
        val coordinate = buffer.g1()
        val flags = buffer.g1()
        val deltaX = buffer.g1s()
        val deltaZ = buffer.g1s()
        val source = buffer.g3()
        val target = buffer.g3()
        val id = buffer.g2()
        val startHeight = buffer.g2s()
        val endHeight = buffer.g2s()
        val startTime = buffer.g2()
        val endTime = buffer.g2()
        val angle = buffer.g1().let { if (it == 255) -1 else it }
        val progress = buffer.g2()
        val startOffset = ProjectileOffset(buffer.g3())
        val endOffset = ProjectileOffset(buffer.g3())
        return MapProjAnimHalfsqV2(
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
            startOffset = startOffset,
            endOffset = endOffset,
        )
    }
}
