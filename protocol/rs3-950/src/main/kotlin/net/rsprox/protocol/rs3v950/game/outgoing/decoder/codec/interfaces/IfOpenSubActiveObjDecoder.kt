package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveObj
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubActiveObjDecoder : ProxyMessageDecoder<IfOpenSubActiveObj> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB_ACTIVE_OBJ_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActiveObj {
        val childId = buffer.g2Alt2()
        val extra1 = buffer.g4Alt3()
        val objId = buffer.g3Alt3()
        val layer = buffer.g1()
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFFFFFFL
        val extra2 = buffer.g4()
        val coord = buffer.g4Alt2()
        val extra3 = buffer.g4Alt1()
        val extra4 = buffer.g4Alt1()

        return IfOpenSubActiveObj(
            componentHash = componentHash,
            childId = childId,
            objId = objId,
            layer = layer,
            coord = coord,
            extra1 = extra1,
            extra2 = extra2,
            extra3 = extra3,
            extra4 = extra4,
        )
    }
}
