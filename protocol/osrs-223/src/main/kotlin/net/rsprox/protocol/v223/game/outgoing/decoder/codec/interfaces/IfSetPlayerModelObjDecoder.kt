package net.rsprox.protocol.v223.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelObj
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

public class IfSetPlayerModelObjDecoder : ProxyMessageDecoder<IfSetPlayerModelObj> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_OBJ

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelObj {
        val combinedId = buffer.gCombinedIdAlt1()
        val obj = buffer.g4Alt3()
        return IfSetPlayerModelObj(
            combinedId.interfaceId,
            combinedId.componentId,
            obj,
        )
    }
}
