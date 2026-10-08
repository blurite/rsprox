package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Unnamed2
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class Unnamed2Decoder : ProxyMessageDecoder<Unnamed2> {
    override val prot: ClientProt = GameServerProt.UNNAMED_2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Unnamed2 {
        val count = buffer.g4()
        val recordCount = count.coerceAtLeast(0)
        require(buffer.readableBytes() >= 4 && recordCount <= (buffer.readableBytes() - 4) / 4) {
            "Truncated counted ignored-word block"
        }
        // Beta skips both the records and footer; retain their bytes without assigning an integer encoding.
        val records = List(recordCount) { Unnamed2.Record(buffer.g1(), buffer.g1(), buffer.g1(), buffer.g1()) }
        val reservedFooter = List(4) { buffer.g1() }
        return Unnamed2(count, records, null, reservedFooter)
    }
}
