package net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.rs3v949.game.incoming.model.players.OpPlayer
import net.rsprox.protocol.session.Session

internal class OpPlayerDecoder(
    override val prot: ClientProt,
    private val op: Int,
) : ProxyMessageDecoder<OpPlayer> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpPlayer {
        val run = (buffer.g1() and 1) != 0
        val index = buffer.g2Alt1()
        return OpPlayer(index, op, run)
    }

    internal companion object {
        internal fun all(): List<OpPlayerDecoder> =
            listOf(
                OpPlayerDecoder(GameClientProt.OPPLAYER1, 1),
                OpPlayerDecoder(GameClientProt.OPPLAYER2, 2),
                OpPlayerDecoder(GameClientProt.OPPLAYER3, 3),
                OpPlayerDecoder(GameClientProt.OPPLAYER4, 4),
                OpPlayerDecoder(GameClientProt.OPPLAYER5, 5),
                OpPlayerDecoder(GameClientProt.OPPLAYER6, 6),
                OpPlayerDecoder(GameClientProt.OPPLAYER7, 7),
                OpPlayerDecoder(GameClientProt.OPPLAYER8, 8),
                OpPlayerDecoder(GameClientProt.OPPLAYER9, 9),
                OpPlayerDecoder(GameClientProt.OPPLAYER10, 10),
            )
    }
}
