package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetObject
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetObjectDecoder : ProxyMessageDecoder<IfSetObject> {
    override val prot: ClientProt = GameServerProt.IF_SETOBJECT_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetObject {
        val count = buffer.g4Alt2()
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFFFFFFL
        val rawObjId = buffer.g3Alt2()
        val objId = if (rawObjId == 0xFFFFFF) -1 else rawObjId
        return IfSetObject(
            componentHash,
            objId,
            count,
        )
    }
}
