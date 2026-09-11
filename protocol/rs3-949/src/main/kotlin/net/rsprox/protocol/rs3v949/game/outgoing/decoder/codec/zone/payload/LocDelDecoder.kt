package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.LocDel
import net.rsprox.protocol.session.Session

internal class LocDelDecoder : ProxyMessageDecoder<LocDel> {
    override val prot: ClientProt = GameServerProt.LOC_DEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocDel {
        val packedCoord = buffer.g1Alt2()
        val rotData = buffer.g1Alt1()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        val shape = (rotData ushr 2) and 0x1F
        val rotation = rotData and 0x3
        return LocDel(xInZone, zInZone, shape, rotation)
    }
}
