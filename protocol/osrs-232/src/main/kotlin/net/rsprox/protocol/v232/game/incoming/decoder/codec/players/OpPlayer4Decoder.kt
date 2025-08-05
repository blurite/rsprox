package net.rsprox.protocol.v232.game.incoming.decoder.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.game.incoming.model.players.OpPlayer
import net.rsprox.protocol.v232.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder

public class OpPlayer4Decoder : ProxyMessageDecoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER4

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpPlayer {
        val controlKey = buffer.g1() == 1
        val index = buffer.g2Alt2()
        return OpPlayer(
            index,
            controlKey,
            4,
        )
    }
}
