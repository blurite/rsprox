package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.header.UpdateZoneFollows
import net.rsprox.protocol.session.Session

internal class UpdateZoneFollowsDecoder(
    override val prot: ClientProt,
    private val full: Boolean,
) : ProxyMessageDecoder<UpdateZoneFollows> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZoneFollows {
        val level: Int
        val zoneX: Int
        if (full) {
            level = buffer.g1()
            zoneX = buffer.g1s()
        } else {
            level = buffer.g1Alt3()
            zoneX = buffer.g1Alt2()
        }
        val zoneZRaw = buffer.g1()
        val zoneZ = zoneZRaw - 128
        return UpdateZoneFollows(full, level, zoneX, zoneZ)
    }

    internal companion object {
        internal fun all(): List<UpdateZoneFollowsDecoder> =
            listOf(
                UpdateZoneFollowsDecoder(GameServerProt.UPDATE_ZONE_FULL_FOLLOWS, full = true),
                UpdateZoneFollowsDecoder(GameServerProt.UPDATE_ZONE_PARTIAL_FOLLOWS, full = false),
            )
    }
}
