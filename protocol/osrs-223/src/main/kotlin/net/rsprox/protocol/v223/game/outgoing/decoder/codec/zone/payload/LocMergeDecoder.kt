package net.rsprox.protocol.v223.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocMerge
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

internal class LocMergeDecoder : ProxyMessageDecoder<LocMerge> {
    override val prot: ClientProt = GameServerProt.LOC_MERGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocMerge {
        val id = buffer.g2()
        val maxX = buffer.g1Alt3()
        val end = buffer.g2()
        val minX = buffer.g1Alt2()
        val maxZ = buffer.g1Alt1()
        val minZ = buffer.g1Alt2()
        val coordInZone = CoordInZone(buffer.g1Alt3())
        val locProperties = LocProperties(buffer.g1Alt3())
        val start = buffer.g2Alt3()
        val index = buffer.g2Alt2()
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