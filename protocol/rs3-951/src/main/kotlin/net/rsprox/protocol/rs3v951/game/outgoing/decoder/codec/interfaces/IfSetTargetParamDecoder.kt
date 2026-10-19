package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetTargetParam
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetTargetParamDecoder : ProxyMessageDecoder<IfSetTargetParam> {
    override val prot: ClientProt = GameServerProt.IF_SETTARGETPARAM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetTargetParam {
        val fromSlot = buffer.g2Alt1().let { if (it == 65535) -1 else it }
        val targetParam = buffer.g2Alt3()
        val toSlot = buffer.g2().let { if (it == 65535) -1 else it }
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        return IfSetTargetParam(
            componentHash,
            targetParam,
            fromSlot,
            toSlot,
        )
    }
}
