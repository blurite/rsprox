package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetObject
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetObjectDecoder : ProxyMessageDecoder<IfSetObject> {
    override val prot: ClientProt = GameServerProt.IF_SETOBJECT_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetObject {
        val componentHash = buffer.g4Alt1().toLong() and 0xFFFFFFFFL
        val rawObjId = buffer.g3Alt3()
        val count = buffer.g4Alt3()
        val objId = if (rawObjId == 0xFFFFFF) -1 else rawObjId
        return IfSetObject(
            componentHash,
            objId,
            count,
        )
    }
}
