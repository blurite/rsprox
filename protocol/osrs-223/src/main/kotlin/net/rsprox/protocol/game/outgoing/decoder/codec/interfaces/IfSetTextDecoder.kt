package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetText
import net.rsprox.protocol.session.Session

public class IfSetTextDecoder : ProxyMessageDecoder<IfSetText> {
    override val prot: ClientProt = GameServerProt.IF_SETTEXT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetText {
        val combinedId = buffer.gCombinedIdAlt3()
        val text = buffer.gjstr()
        return IfSetText(
            combinedId.interfaceId,
            combinedId.componentId,
            text,
        )
    }
}
