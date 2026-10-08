package net.rsprox.protocol.rs3v950beta.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfCrmViewOp
import net.rsprox.protocol.session.Session

internal class IfCrmViewOpDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<IfCrmViewOp> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfCrmViewOp {
        val selectedCrmEntry = buffer.g4Alt1()
        val sub = buffer.g2Alt3()
        val crmValue0 = buffer.g4()
        val crmValue2 = buffer.g4Alt2()
        val combinedId = buffer.g4Alt2()
        val crmValue1 = buffer.g4Alt2()
        return IfCrmViewOp(
            sub,
            crmValue0,
            crmValue2,
            crmValue1,
            combinedId,
            selectedCrmEntry,
        )
    }
}
