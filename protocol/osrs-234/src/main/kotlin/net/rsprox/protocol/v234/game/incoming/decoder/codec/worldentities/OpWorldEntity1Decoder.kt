package net.rsprox.protocol.v234.game.incoming.decoder.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntity
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.incoming.decoder.prot.GameClientProt

public class OpWorldEntity1Decoder : ProxyMessageDecoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpWorldEntity {
        val controlKey = buffer.g1() == 1
        val index = buffer.g2Alt1()
        return OpWorldEntity(
            index,
            controlKey,
            1,
        )
    }
}
