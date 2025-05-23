package net.rsprox.protocol.v223.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelBodyType
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

internal class IfSetPlayerModelBodyTypeDecoder : ProxyMessageDecoder<IfSetPlayerModelBodyType> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_BODYTYPE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelBodyType {
        val combinedId = buffer.gCombinedIdAlt3()
        val bodyType = buffer.g1Alt3()
        return IfSetPlayerModelBodyType(
            combinedId.interfaceId,
            combinedId.componentId,
            bodyType,
        )
    }
}
