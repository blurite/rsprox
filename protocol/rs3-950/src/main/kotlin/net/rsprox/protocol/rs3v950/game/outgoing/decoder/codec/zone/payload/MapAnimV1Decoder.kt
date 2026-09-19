package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapAnimV1
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MapAnimV1Decoder : ProxyMessageDecoder<MapAnimV1> {
    override val prot: ClientProt = GameServerProt.MAP_ANIM_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapAnimV1 {
        val packedCoord = buffer.g1()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7

        val id = buffer.g2()
        val height = buffer.g2s()
        val packedDelay = buffer.g2()
        val rotation = buffer.g1()
        val unused0 = buffer.g1()
        val unused1 = buffer.g1()
        val unused2 = buffer.g1()

        return MapAnimV1(
            id = id,
            xInZone = xInZone,
            zInZone = zInZone,
            height = height,
            packedDelay = packedDelay,
            rotation = rotation,
            unused0 = unused0,
            unused1 = unused1,
            unused2 = unused2,
        )
    }
}
