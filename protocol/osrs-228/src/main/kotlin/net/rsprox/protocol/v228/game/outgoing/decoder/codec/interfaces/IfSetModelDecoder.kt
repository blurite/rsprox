package net.rsprox.protocol.v228.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetModelV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.outgoing.decoder.prot.GameServerProt

internal class IfSetModelDecoder : ProxyMessageDecoder<IfSetModelV1> {
    override val prot: ClientProt = GameServerProt.IF_SETMODEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetModelV1 {
        val combinedId = buffer.gCombinedId()
        val model = buffer.g2Alt3()
        return IfSetModelV1(
            combinedId.interfaceId,
            combinedId.componentId,
            model,
        )
    }
}
