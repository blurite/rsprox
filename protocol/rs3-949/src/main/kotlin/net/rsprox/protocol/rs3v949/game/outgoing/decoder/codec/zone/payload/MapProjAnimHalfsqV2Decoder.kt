package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.MapProjAnimHalfsqV2
import net.rsprox.protocol.session.Session

internal class MapProjAnimHalfsqV2Decoder : ProxyMessageDecoder<MapProjAnimHalfsqV2> {
    override val prot: ClientProt = GameServerProt.MAP_PROJANIM_HALFSQ_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapProjAnimHalfsqV2 {
        val srcCoordHalf = buffer.g1()
        val destYdeltaHalf = buffer.g1s()
        val destXdeltaHalf = buffer.g1s()
        val flags = buffer.g1()
        val sourceType = buffer.g1()
        val sourceIndex = buffer.g2()
        val targetType = buffer.g1()
        val targetIndex = buffer.g2()
        val spotAnimId = buffer.g2()
        val startHeight = buffer.g2()
        val endHeight = buffer.g2()
        val startTime = buffer.g2()
        val endTime = buffer.g2()
        val alpha = buffer.g1s()
        val angle = buffer.g2() shl 2
        val startOffset = buffer.g3()
        val endOffset = buffer.g3()
        val xInZoneHalf = (srcCoordHalf ushr 4) and 0xF
        val zInZoneHalf = srcCoordHalf and 0xF
        return MapProjAnimHalfsqV2(
            srcCoordHalf, xInZoneHalf, zInZoneHalf, destXdeltaHalf, destYdeltaHalf,
            flags, sourceType, sourceIndex, targetType, targetIndex, spotAnimId,
            startHeight, endHeight, startTime, endTime, alpha, angle, startOffset, endOffset
        )
    }
}
