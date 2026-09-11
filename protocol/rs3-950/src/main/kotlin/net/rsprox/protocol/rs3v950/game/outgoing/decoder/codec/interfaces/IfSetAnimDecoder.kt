package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetAnim
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetAnimDecoder : ProxyMessageDecoder<IfSetAnim> {
    override val prot: ClientProt = GameServerProt.IF_SETANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetAnim {
        val animId = buffer.g4Alt3()
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFFFFFFL
        return IfSetAnim(
            componentHash,
            animId,
        )
    }
}
