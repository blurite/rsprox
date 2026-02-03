package net.rsprox.protocol.v236.game.incoming.decoder.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntity
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class OpWorldEntity4Decoder : ProxyMessageDecoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY4

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpWorldEntity {
        val controlKey = buffer.g1Alt2() == 1
        val index = buffer.g2()
        return OpWorldEntity(
            index,
            controlKey,
            4,
        )
    }
}
