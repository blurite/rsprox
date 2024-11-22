package net.rsprox.protocol.v227.game.outgoing.decoder.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.worldentity.ClearEntities
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class ClearEntitiesDecoder : ProxyMessageDecoder<ClearEntities> {
    override val prot: ClientProt = GameServerProt.CLEAR_ENTITIES

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClearEntities {
        return ClearEntities
    }
}
