package net.rsprox.protocol.v225.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetNpcHeadActive
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.outgoing.decoder.prot.GameServerProt

internal class IfSetNpcHeadActiveDecoder : ProxyMessageDecoder<IfSetNpcHeadActive> {
    override val prot: ClientProt = GameServerProt.IF_SETNPCHEAD_ACTIVE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetNpcHeadActive {
        val combinedId = buffer.gCombinedIdAlt2()
        val index = buffer.g2Alt2()
        return IfSetNpcHeadActive(
            combinedId.interfaceId,
            combinedId.componentId,
            index,
        )
    }
}
