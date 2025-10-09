package net.rsprox.protocol.v234.game.incoming.decoder.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntity
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.incoming.decoder.prot.GameClientProt

public class OpWorldEntity2Decoder : ProxyMessageDecoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpWorldEntity {
        val index = buffer.g2()
        val controlKey = buffer.g1Alt3() == 1
        return OpWorldEntity(
            index,
            controlKey,
            2,
        )
    }
}
