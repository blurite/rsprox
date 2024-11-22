package net.rsprox.protocol.v227.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelObj
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

internal class IfSetPlayerModelObjDecoder : ProxyMessageDecoder<IfSetPlayerModelObj> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_OBJ

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelObj {
        val obj = buffer.g4Alt1()
        val combinedId = buffer.gCombinedIdAlt1()
        return IfSetPlayerModelObj(
            combinedId.interfaceId,
            combinedId.componentId,
            obj,
        )
    }
}
