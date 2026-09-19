package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanSettingsFull
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ClanSettingsFullDecoder : ProxyMessageDecoder<ClanSettingsFull> {
    override val prot: ClientProt = GameServerProt.CLANSETTINGS_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanSettingsFull {
        val channelIndex = buffer.g1s()
        if (!buffer.isReadable) return ClanSettingsFull(channelIndex, null)
        val version = buffer.g1()
        require(version in 1..6) { "Unsupported clan-settings version $version" }
        val flags = buffer.g1()
        require(flags and 3 == 2) { "Unsupported clan-settings name/hash flags $flags" }
        val updateNumber = buffer.g4()
        val legacyTimestamp = buffer.g4()
        val memberCount = buffer.g2()
        val bannedCount = buffer.g1()
        val name = buffer.readNativeString()
        val extra = if (version >= 4) buffer.g4() else null
        val allowGuests = buffer.g1() == 1
        val rank0 = buffer.g1s()
        val rank1 = buffer.g1s()
        val rank2 = buffer.g1s()
        val headerBoolean = buffer.g1() == 1
        val minimumMemberSize =
            2 + (if (version >= 2) 4 else 0) +
                (if (version >= 5) 2 else 0) + (if (version >= 6) 1 else 0)
        require(memberCount <= buffer.readableBytes() / minimumMemberSize) { "Truncated clan-settings members" }
        val members =
            List(memberCount) {
                val memberName = buffer.readNativeString()
                val rank = buffer.g1s()
                val bits = if (version >= 2) buffer.g4() else null
                val joinedDay = if (version >= 5) buffer.g2() else null
                val muted = if (version >= 6) buffer.g1() == 1 else null
                ClanSettingsFull.Member(memberName, rank, bits, joinedDay, muted)
            }
        require(bannedCount <= buffer.readableBytes()) { "Truncated clan-settings banned names" }
        val banned = List(bannedCount) { buffer.readNativeString() }
        val parameters =
            if (version >= 3) {
                val count = buffer.g2()
                require(count <= buffer.readableBytes() / 4) { "Truncated clan-settings parameters" }
                List(count) {
                    val keyAndType = buffer.g4()
                    val key = keyAndType and 0x3FFF_FFFF
                    when (keyAndType ushr 30) {
                        0 -> ClanSettingsFull.IntParameter(key, buffer.g4())
                        1 -> ClanSettingsFull.LongParameter(key, buffer.g8())
                        2 -> ClanSettingsFull.StringParameter(key, buffer.readNativeString())
                        else -> ClanSettingsFull.NullParameter(key)
                    }
                }
            } else {
                emptyList()
            }
        return ClanSettingsFull(
            channelIndex,
            ClanSettingsFull.Settings(
                version,
                flags,
                updateNumber,
                legacyTimestamp,
                name,
                extra,
                allowGuests,
                rank0,
                rank1,
                rank2,
                headerBoolean,
                members,
                banned,
                parameters,
            ),
        )
    }
}
