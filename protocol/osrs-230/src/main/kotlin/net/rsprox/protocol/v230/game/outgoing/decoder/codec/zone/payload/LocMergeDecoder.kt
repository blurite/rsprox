package net.rsprox.protocol.v230.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocMerge
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v230.game.outgoing.decoder.prot.GameServerProt

internal class LocMergeDecoder : ProxyMessageDecoder<LocMerge> {
    override val prot: ClientProt = GameServerProt.LOC_MERGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocMerge {
        val maxX = buffer.g1sAlt1()
        val locProperties = LocProperties(buffer.g1Alt1())
        val id = buffer.g2Alt3()
        val start = buffer.g2Alt3()
        val maxZ = buffer.g1sAlt1()
        val minX = buffer.g1s()
        val index = buffer.g2Alt3()
        val coordInZone = CoordInZone(buffer.g1())
        val minZ = buffer.g1sAlt2()
        val end = buffer.g2()
        return LocMerge(
            index,
            id,
            coordInZone,
            locProperties,
            start,
            end,
            minX,
            minZ,
            maxX,
            maxZ,
        )
    }
}
