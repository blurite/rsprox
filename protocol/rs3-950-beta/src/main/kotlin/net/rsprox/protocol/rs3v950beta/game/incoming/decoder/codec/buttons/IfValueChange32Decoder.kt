package net.rsprox.protocol.rs3v950beta.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfValueChange32
import net.rsprox.protocol.session.Session

internal class IfValueChange32Decoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<IfValueChange32> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfValueChange32 {
        val flags = buffer.g1()
        val combinedId = buffer.g4Alt3()
        val value = buffer.g4Alt2()
        val sub = buffer.g2Alt2()
        return IfValueChange32(
            value,
            sub,
            combinedId,
            flags,
        )
    }
}
