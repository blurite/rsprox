package net.rsprox.protocol.v236.game.incoming.decoder.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntity
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class OpWorldEntity5Decoder : ProxyMessageDecoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY5

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpWorldEntity {
        val index = buffer.g2Alt2()
        val controlKey = buffer.g1Alt3() == 1
        return OpWorldEntity(
            index,
            controlKey,
            5,
        )
    }
}
