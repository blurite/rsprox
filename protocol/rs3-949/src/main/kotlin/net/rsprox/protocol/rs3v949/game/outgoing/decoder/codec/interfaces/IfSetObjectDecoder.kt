package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetObject
import net.rsprox.protocol.session.Session

internal class IfSetObjectDecoder : ProxyMessageDecoder<IfSetObject> {
    override val prot: ClientProt = GameServerProt.IF_SETOBJECT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetObject {
        val objId = buffer.g2()
        val count = buffer.g4()
        val componentHash = buffer.g4().toLong() and 0xFFFFFFFFL
        return IfSetObject(
            componentHash,
            objId,
            count,
        )
    }
}
