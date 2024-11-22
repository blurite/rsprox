package net.rsprox.protocol.v227.game.incoming.decoder.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.players.OpPlayer
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

internal class OpPlayer1Decoder : ProxyMessageDecoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpPlayer {
        val controlKey = buffer.g1Alt2() == 1
        val index = buffer.g2Alt3()
        return OpPlayer(
            index,
            controlKey,
            1,
        )
    }
}
