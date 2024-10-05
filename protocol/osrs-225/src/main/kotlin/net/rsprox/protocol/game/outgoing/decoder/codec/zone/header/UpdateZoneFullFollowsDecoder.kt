package net.rsprox.protocol.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.session.Session

public class UpdateZoneFullFollowsDecoder : ProxyMessageDecoder<UpdateZoneFullFollows> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_FULL_FOLLOWS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZoneFullFollows {
        val zoneX = buffer.g1Alt1()
        val level = buffer.g1Alt3()
        val zoneZ = buffer.g1Alt3()
        return UpdateZoneFullFollows(
            zoneX,
            zoneZ,
            level,
        )
    }
}
