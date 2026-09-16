package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons

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
        val sourceSub = buffer.g2Alt1()
        val sourceCombinedId = buffer.g4Alt2()
        val sourceObj = buffer.g3Alt1()
        val targetSub = buffer.g2Alt1()
        val targetCombinedId = buffer.g4Alt3()
        val targetObj = buffer.g3Alt2()
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
