package net.rsprox.protocol.v237.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetModelV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.outgoing.decoder.prot.GameServerProt

internal class IfSetModelV2Decoder : ProxyMessageDecoder<IfSetModelV2> {
    override val prot: ClientProt = GameServerProt.IF_SETMODEL_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetModelV2 {
        val model = buffer.g4Alt1()
        val combinedId = buffer.gCombinedIdAlt3()
        return IfSetModelV2(
            combinedId.interfaceId,
            combinedId.componentId,
            model,
        )
    }
}
