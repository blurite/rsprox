package net.rsprox.protocol.v224.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelBodyType
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

public class IfSetPlayerModelBodyTypeDecoder : ProxyMessageDecoder<IfSetPlayerModelBodyType> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_BODYTYPE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelBodyType {
        val bodyType = buffer.g1Alt3()
        val combinedId = buffer.gCombinedIdAlt2()
        return IfSetPlayerModelBodyType(
            combinedId.interfaceId,
            combinedId.componentId,
            bodyType,
        )
    }
}
