package net.rsprox.protocol.v225.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetModel
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.outgoing.decoder.prot.GameServerProt

internal class IfSetModelDecoder : ProxyMessageDecoder<IfSetModel> {
    override val prot: ClientProt = GameServerProt.IF_SETMODEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetModel {
        val combinedId = buffer.gCombinedId()
        val model = buffer.g2Alt1()
        return IfSetModel(
            combinedId.interfaceId,
            combinedId.componentId,
            model,
        )
    }
}
