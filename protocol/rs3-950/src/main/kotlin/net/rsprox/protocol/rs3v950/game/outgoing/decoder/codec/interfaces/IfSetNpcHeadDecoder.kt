package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetNpcHead
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetNpcHeadDecoder : ProxyMessageDecoder<IfSetNpcHead> {
    override val prot: ClientProt = GameServerProt.IF_SETNPCHEAD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetNpcHead {
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFFFFFFL
        val npcId = buffer.g4Alt3()
        return IfSetNpcHead(
            componentHash,
            npcId,
        )
    }
}
