package net.rsprox.protocol.v234.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelObj
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.outgoing.decoder.prot.GameServerProt

internal class IfSetPlayerModelObjDecoder : ProxyMessageDecoder<IfSetPlayerModelObj> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_OBJ

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelObj {
        val combinedId = buffer.gCombinedId()
        val obj = buffer.g4()
        return IfSetPlayerModelObj(
            combinedId.interfaceId,
            combinedId.componentId,
            obj,
        )
    }
}
