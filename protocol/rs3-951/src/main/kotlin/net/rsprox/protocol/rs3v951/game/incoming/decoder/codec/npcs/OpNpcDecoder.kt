package net.rsprox.protocol.rs3v951.game.incoming.decoder.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.npcs.OpNpc
import net.rsprox.protocol.rs3v951.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.session.Session

internal class OpNpcDecoder(
    override val prot: ClientProt,
    private val op: Int,
) : ProxyMessageDecoder<OpNpc> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpNpc {
        val index = buffer.g2Alt3()
        val run = (buffer.g1Alt1() and 1) != 0
        return OpNpc(
            index,
            op,
            run,
        )
    }

    internal companion object {
        internal fun all(): List<OpNpcDecoder> =
            listOf(
                OpNpcDecoder(GameClientProt.OPNPC1, 1),
                OpNpcDecoder(GameClientProt.OPNPC2, 2),
                OpNpcDecoder(GameClientProt.OPNPC3, 3),
                OpNpcDecoder(GameClientProt.OPNPC4, 4),
                OpNpcDecoder(GameClientProt.OPNPC5, 5),
                OpNpcDecoder(GameClientProt.OPNPC6, 6),
            )
    }
}
