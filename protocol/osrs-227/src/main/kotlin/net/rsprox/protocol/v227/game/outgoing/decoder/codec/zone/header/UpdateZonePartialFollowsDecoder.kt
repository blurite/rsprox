package net.rsprox.protocol.v227.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

internal class UpdateZonePartialFollowsDecoder : ProxyMessageDecoder<UpdateZonePartialFollows> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_FOLLOWS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialFollows {
        val level = buffer.g1Alt3()
        val zoneX = buffer.g1Alt1()
        val zoneZ = buffer.g1()
        return UpdateZonePartialFollows(
            zoneX,
            zoneZ,
            level,
        )
    }
}
