package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveObj
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubActiveObjDecoder : ProxyMessageDecoder<IfOpenSubActiveObj> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB_ACTIVE_OBJ_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActiveObj {
        // The native handler skips these fixed spans; preserve bytes without inventing XTEA transforms.
        val reserved0 = List(4) { buffer.g1() }
        val coord = buffer.g4()
        val reserved1 = List(4) { buffer.g1() }
        val objId = buffer.g3Alt1()
        val reserved2 = List(4) { buffer.g1() }
        val layer = buffer.g1Alt2()
        val reserved3 = List(4) { buffer.g1() }
        val childId = buffer.g2Alt1()
        val componentHash = buffer.g4().toLong() and 0xFFFF_FFFFL
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
            reserved = reserved0 + reserved1 + reserved2 + reserved3,
        )
    }
}
