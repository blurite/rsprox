package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.session.Session

internal class UpdateZonePartialFollowsDecoder : ProxyMessageDecoder<UpdateZonePartialFollows> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_FOLLOWS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialFollows {
        val level = buffer.g1Alt3()
        val zoneX = buffer.g1Alt2()
        val zoneZ = buffer.g1sAlt1()
        return UpdateZonePartialFollows(level, zoneX, zoneZ)
    }
}
