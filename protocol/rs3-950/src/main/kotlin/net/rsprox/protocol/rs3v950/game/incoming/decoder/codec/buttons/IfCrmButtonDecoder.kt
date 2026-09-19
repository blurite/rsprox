package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfCrmButton
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class IfCrmButtonDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<IfCrmButton> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfCrmButton {
        val crmName = buffer.readNativeString()
        val combinedId = buffer.g4Alt2()
        val operation = buffer.g1Alt1()
        val sub = buffer.g2Alt2()
        val crmType = buffer.g1Alt2()
        return IfCrmButton(
            crmName,
            combinedId,
            operation,
            sub,
            crmType,
        )
    }
}
