package net.rsprox.protocol.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjOpFilter
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session

public class ObjOpFilterDecoder : ProxyMessageDecoder<ObjOpFilter> {
    override val prot: ClientProt = GameServerProt.OBJ_OPFILTER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjOpFilter {
        val opFlags = OpFlags(buffer.g1Alt2())
        val coordInZone = CoordInZone(buffer.g1Alt2())
        val id = buffer.g2()
        return ObjOpFilter(
            id,
            opFlags,
            coordInZone,
        )
    }
}
