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
        val srcCoord = buffer.g1()
        val targetDeltaY = buffer.g1s()
        val targetDeltaX = buffer.g1s()
        val mediumId = buffer.g3()
        val spotAnimId = buffer.g2()
        val startHeight = buffer.g1s()
        val endHeight = buffer.g1s()
        val startTime = buffer.g2()
        val endTime = buffer.g2()
        val alpha = buffer.g1()
        val lockonSlot = buffer.g2()
        buffer.skipRead(3)
        val xInZone = (srcCoord ushr 3) and 0x7
        val zInZone = srcCoord and 0x7
        return MapProjAnim(
            mediumId, spotAnimId, xInZone, zInZone, targetDeltaX, targetDeltaY,
            startHeight, endHeight, startTime, endTime, alpha, lockonSlot
        )
    }
}
