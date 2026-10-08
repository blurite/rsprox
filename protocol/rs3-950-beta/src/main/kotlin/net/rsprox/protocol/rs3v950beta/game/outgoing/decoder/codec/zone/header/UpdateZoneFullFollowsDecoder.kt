package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateZoneFullFollowsDecoder : ProxyMessageDecoder<UpdateZoneFullFollows> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_FULL_FOLLOWS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZoneFullFollows {
        val zoneZ = buffer.g1Alt2().toByte().toInt()
        val zoneX = buffer.g1Alt1().toByte().toInt()
        val level = buffer.g1Alt2()
        return UpdateZoneFullFollows(level, zoneX, zoneZ)
    }
}
