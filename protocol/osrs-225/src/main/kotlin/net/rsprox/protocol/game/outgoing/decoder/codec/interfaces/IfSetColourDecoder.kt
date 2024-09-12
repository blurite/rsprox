package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetColour
import net.rsprox.protocol.session.Session

public class IfSetColourDecoder : ProxyMessageDecoder<IfSetColour> {
    override val prot: ClientProt = GameServerProt.IF_SETCOLOUR

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetColour {
        val colour15BitPacked = buffer.g2()
        val combinedId = buffer.gCombinedIdAlt1()
        return IfSetColour(
            combinedId.interfaceId,
            combinedId.componentId,
            colour15BitPacked,
        )
    }
}
