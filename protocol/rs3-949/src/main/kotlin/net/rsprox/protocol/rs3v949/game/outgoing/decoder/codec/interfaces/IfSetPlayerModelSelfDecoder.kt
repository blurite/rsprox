package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfSetPlayerModelSelf
import net.rsprox.protocol.session.Session

internal class IfSetPlayerModelSelfDecoder : ProxyMessageDecoder<IfSetPlayerModelSelf> {
    override val prot: ClientProt = GameServerProt.IF_SET_PLAYER_MODEL_SELF

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelSelf {
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFFFFFFL
        return IfSetPlayerModelSelf(componentHash)
    }
}
