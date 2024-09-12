package net.rsprox.protocol.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjEnabledOps
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session

public class ObjEnabledOpsDecoder : ProxyMessageDecoder<ObjEnabledOps> {
    override val prot: ClientProt = GameServerProt.OBJ_ENABLED_OPS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjEnabledOps {
        val coordInZone = CoordInZone(buffer.g1Alt1())
        val id = buffer.g2Alt2()
        val opFlags = OpFlags(buffer.g1Alt2())
        return ObjEnabledOps(
            id,
            opFlags,
            coordInZone,
        )
    }
}
