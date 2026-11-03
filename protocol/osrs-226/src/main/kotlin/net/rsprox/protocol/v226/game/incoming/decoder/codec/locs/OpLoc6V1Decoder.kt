package net.rsprox.protocol.v226.game.incoming.decoder.codec.locs
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLoc6V1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.incoming.decoder.prot.GameClientProt

internal class OpLoc6V1Decoder : ProxyMessageDecoder<OpLoc6V1> {
    override val prot: ClientProt = GameClientProt.OPLOC6_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLoc6V1 {
        val id = buffer.g2Alt1()
        return OpLoc6V1(id)
    }
}
