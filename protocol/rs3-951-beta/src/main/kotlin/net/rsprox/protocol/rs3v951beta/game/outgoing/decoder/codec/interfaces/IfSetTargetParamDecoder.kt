package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetTargetParam
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetTargetParamDecoder : ProxyMessageDecoder<IfSetTargetParam> {
    override val prot: ClientProt = GameServerProt.IF_SETTARGETPARAM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetTargetParam {
        val fromSlot = buffer.g2().let { if (it == 65535) -1 else it }
        val toSlot = buffer.g2Alt2().let { if (it == 65535) -1 else it }
        val targetParam = buffer.g2Alt2()
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFF_FFFFL
        return IfSetTargetParam(
            componentHash,
            targetParam,
            fromSlot,
            toSlot,
        )
    }
}
