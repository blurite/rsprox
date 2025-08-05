package net.rsprox.protocol.v232.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjEnabledOps
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.outgoing.decoder.prot.GameServerProt

internal class ObjEnabledOpsDecoder : ProxyMessageDecoder<ObjEnabledOps> {
    override val prot: ClientProt = GameServerProt.OBJ_ENABLED_OPS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjEnabledOps {
        val id = buffer.g2Alt2()
        val coordInZone = CoordInZone(buffer.g1())
        val opFlags = OpFlags(buffer.g1())
        return ObjEnabledOps(
            id,
            opFlags,
            coordInZone,
        )
    }
}
