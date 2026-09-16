package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetGraphic
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetGraphicDecoder : ProxyMessageDecoder<IfSetGraphic> {
    override val prot: ClientProt = GameServerProt.IF_SETGRAPHIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetGraphic {
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFF_FFFFL
        val graphicId = buffer.g4Alt2()
        return IfSetGraphic(
            componentHash,
            graphicId,
        )
    }
}
