package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.MapAnim
import net.rsprox.protocol.session.Session

internal class MapAnimDecoder : ProxyMessageDecoder<MapAnim> {
    override val prot: ClientProt = GameServerProt.MAP_ANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapAnim {
        val packedCoord = buffer.g1()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        val id = buffer.g4()
        val unk1 = buffer.g1()
        val unk2 = buffer.g1()
        val unk3 = buffer.g1()
        val unk4 = buffer.g2()
        val unk5 = buffer.g1()
        return MapAnim(
            id = id,
            xInZone = xInZone,
            zInZone = zInZone,
            unk1 = unk1,
            unk2 = unk2,
            unk3 = unk3,
            unk4 = unk4,
            unk5 = unk5,
        )
    }
}
