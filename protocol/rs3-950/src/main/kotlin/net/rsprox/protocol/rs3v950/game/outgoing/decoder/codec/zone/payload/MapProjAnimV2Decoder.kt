package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapProjAnimV2
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MapProjAnimV2Decoder : ProxyMessageDecoder<MapProjAnimV2> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnimV2 {
        val coordinate = buffer.g1()
        val deltaX = buffer.g1s()
        val deltaZ = buffer.g1s()
        val target = buffer.g3()
        val id = buffer.g2()
        val startHeight = buffer.g2s()
        val endHeight = buffer.g2s()
        val startTime = buffer.g2()
        val endTime = buffer.g2()
        val slope = buffer.g1()
        val distance = buffer.g2()
        val unused0 = buffer.g1()
        val unused1 = buffer.g1()
        val unused2 = buffer.g1()
        val startAttachment = buffer.g3()
        val endAttachment = buffer.g3()
        return MapProjAnimV2(
            coordinate = coordinate,
            deltaX = deltaX,
            deltaZ = deltaZ,
            target = target,
            id = id,
            startHeight = startHeight,
            endHeight = endHeight,
            startTime = startTime,
            endTime = endTime,
            slope = slope,
            distance = distance,
            unused0 = unused0,
            unused1 = unused1,
            unused2 = unused2,
            startAttachment = startAttachment,
            endAttachment = endAttachment,
        )
    }
}
