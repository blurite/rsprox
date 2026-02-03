package net.rsprox.protocol.v236.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.specific.ObjEnabledOpsSpecific
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class ObjEnabledOpsSpecificDecoder : ProxyMessageDecoder<ObjEnabledOpsSpecific> {
    override val prot: ClientProt = GameServerProt.OBJ_ENABLED_OPS_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjEnabledOpsSpecific {
        val id = buffer.g2Alt2()
        val opFlags = OpFlags(buffer.g1Alt2())
        val coordGrid = CoordGrid(buffer.g4Alt3())
        return ObjEnabledOpsSpecific(
            id,
            opFlags,
            coordGrid,
        )
    }
}
