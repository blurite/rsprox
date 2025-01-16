package net.rsprox.protocol.v228.game.incoming.decoder.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.game.incoming.model.npcs.OpNpc
import net.rsprox.protocol.v228.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder

public class OpNpc2Decoder : ProxyMessageDecoder<OpNpc> {
    override val prot: ClientProt = GameClientProt.OPNPC2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpNpc {
        val index = buffer.g2Alt3()
        val controlKey = buffer.g1Alt2() == 1
        return OpNpc(
            index,
            controlKey,
            2,
        )
    }
}
