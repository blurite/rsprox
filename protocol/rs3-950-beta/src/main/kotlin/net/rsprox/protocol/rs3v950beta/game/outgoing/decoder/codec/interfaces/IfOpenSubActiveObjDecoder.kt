package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveObj
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubActiveObjDecoder : ProxyMessageDecoder<IfOpenSubActiveObj> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB_ACTIVE_OBJ_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActiveObj {
        // The native handler skips these fixed spans; preserve bytes without inventing XTEA transforms.
        val objId = buffer.g3Alt1()
        val layer = buffer.g1Alt3()
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        val reserved0 = List(12) { buffer.g1() }
        val coord = buffer.g4Alt2()
        val reserved1 = List(4) { buffer.g1() }
        val childId = buffer.g2Alt3()
        return IfOpenSubActiveObj(
            componentHash = componentHash,
            childId = childId,
            objId = objId,
            layer = layer,
            coord = coord,
            extra1 = null,
            extra2 = null,
            extra3 = null,
            extra4 = null,
            reserved = reserved0 + reserved1,
        )
    }
}
