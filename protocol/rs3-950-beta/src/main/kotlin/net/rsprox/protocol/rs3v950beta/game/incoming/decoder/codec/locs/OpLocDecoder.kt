package net.rsprox.protocol.rs3v950beta.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.rs3v950beta.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.session.Session

internal class OpLocDecoder(
    override val prot: ClientProt,
    private val op: Int,
) : ProxyMessageDecoder<OpLoc> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLoc {
        val run = (buffer.g1Alt3() and 1) != 0
        val locId = buffer.g4Alt2()
        val y = buffer.g2Alt3()
        val x = buffer.g2Alt2()
        return OpLoc(locId, x, y, op, run)
    }

    internal companion object {
        internal fun all(): List<OpLocDecoder> =
            listOf(
                OpLocDecoder(GameClientProt.OPLOC1, 1),
                OpLocDecoder(GameClientProt.OPLOC2, 2),
                OpLocDecoder(GameClientProt.OPLOC3, 3),
                OpLocDecoder(GameClientProt.OPLOC4, 4),
                OpLocDecoder(GameClientProt.OPLOC5, 5),
                OpLocDecoder(GameClientProt.OPLOC6, 6),
            )
    }
}
