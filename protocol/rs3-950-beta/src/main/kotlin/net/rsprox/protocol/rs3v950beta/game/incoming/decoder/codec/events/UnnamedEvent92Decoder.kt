package net.rsprox.protocol.rs3v950beta.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.UnnamedEvent92
import net.rsprox.protocol.session.Session

internal class UnnamedEvent92Decoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<UnnamedEvent92> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UnnamedEvent92 {
        require(buffer.readableBytes() == 14) { "Invalid plugin event length" }
        val field0 = buffer.g1Alt2()
        val field4 = buffer.g1Alt3()
        val field1 = buffer.g4Alt2()
        val field3 = buffer.g4()
        val field2 = buffer.g4()
        return UnnamedEvent92(field0, field1, field2, field3, field4)
    }
}
