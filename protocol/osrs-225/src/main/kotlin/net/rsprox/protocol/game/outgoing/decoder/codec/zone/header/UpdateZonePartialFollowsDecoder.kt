package net.rsprox.protocol.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.session.Session

public class UpdateZonePartialFollowsDecoder : ProxyMessageDecoder<UpdateZonePartialFollows> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_FOLLOWS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialFollows {
        val zoneX = buffer.g1()
        val level = buffer.g1Alt1()
        val zoneZ = buffer.g1Alt1()
        return UpdateZonePartialFollows(
            zoneX,
            zoneZ,
            level,
        )
    }
}
