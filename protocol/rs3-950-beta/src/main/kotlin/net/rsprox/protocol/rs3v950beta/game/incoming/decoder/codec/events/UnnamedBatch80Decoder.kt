package net.rsprox.protocol.rs3v950beta.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.UnnamedBatch80
import net.rsprox.protocol.session.Session

internal class UnnamedBatch80Decoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<UnnamedBatch80> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UnnamedBatch80 {
        val complete = buffer.g1()
        val count = buffer.g2()
        require(complete in 0..1 && count in 1..38 && buffer.readableBytes() == count * 13) {
            "Invalid plugin batch framing"
        }
        val entries =
            List(count) {
                val field0 = buffer.g4()
                val field1 = buffer.g4()
                val field2 = buffer.g4()
                val field3 = buffer.g1()
                UnnamedBatch80.Entry(field0, field1, field2, field3)
            }
        return UnnamedBatch80(complete == 1, entries)
    }
}
