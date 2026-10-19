package net.rsprox.protocol.rs3v951.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfButtonD
import net.rsprox.protocol.session.Session

internal class IfButtonDDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<IfButtonD> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfButtonD {
        val sourceSub = buffer.g2()
        val targetSub = buffer.g2()
        val targetCombinedId = buffer.g4Alt1()
        val sourceObj = buffer.g3Alt3()
        val sourceCombinedId = buffer.g4Alt1()
        val targetObj = buffer.g3()
        return IfButtonD(
            sourceSub,
            sourceCombinedId,
            sourceObj,
            targetSub,
            targetCombinedId,
            targetObj,
        )
    }
}
