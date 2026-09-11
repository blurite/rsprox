package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetPlayerHead
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetPlayerHeadDecoder : ProxyMessageDecoder<IfSetPlayerHead> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERHEAD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerHead {
        val componentHash = buffer.g4().toLong() and 0xFFFFFFFFL
        return IfSetPlayerHead(componentHash)
    }
}
