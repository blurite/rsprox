package net.rsprox.protocol.v228.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetObject
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.outgoing.decoder.prot.GameServerProt

internal class IfSetObjectDecoder : ProxyMessageDecoder<IfSetObject> {
    override val prot: ClientProt = GameServerProt.IF_SETOBJECT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetObject {
        val count = buffer.g4Alt1()
        val obj = buffer.g2Alt2()
        val combinedId = buffer.gCombinedIdAlt3()
        return IfSetObject(
            combinedId.interfaceId,
            combinedId.componentId,
            obj,
            count,
        )
    }
}
