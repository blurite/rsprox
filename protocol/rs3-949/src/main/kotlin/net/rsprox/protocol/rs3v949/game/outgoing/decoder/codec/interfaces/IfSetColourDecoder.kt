package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfSetColour
import net.rsprox.protocol.session.Session

internal class IfSetColourDecoder : ProxyMessageDecoder<IfSetColour> {
    override val prot: ClientProt = GameServerProt.IF_SET_COLOUR

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetColour {
        val packedColor = buffer.g2Alt1()
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFFFFFFL
        return IfSetColour(
            componentHash,
            packedColor,
        )
    }
}
