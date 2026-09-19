package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanChannelFull
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ClanChannelFullDecoder : ProxyMessageDecoder<ClanChannelFull> {
    override val prot: ClientProt = GameServerProt.CLANCHANNEL_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanChannelFull {
        val channelIndex = buffer.g1s()
        if (!buffer.isReadable) return ClanChannelFull(channelIndex, null)
        val flags = buffer.g1()
        val version = if (flags and 4 != 0) buffer.g1() else 2
        val discardedKey = buffer.g8()
        val updateNumber = buffer.g8()
        val name = buffer.readNativeString()
        val headerBoolean = buffer.g1() == 1
        val talkRank = buffer.g1s()
        val kickRank = buffer.g1s()
        val count = buffer.g2()
        val minimumMemberSize = if (version > 2) 5 else 4
        require(count <= buffer.readableBytes() / minimumMemberSize) { "Truncated clan-channel members" }
        // Native always transmits names, without the optional hashes used by other clients.
        val members =
            List(count) {
                val memberName = buffer.readNativeString()
                val rank = buffer.g1s()
                val world = buffer.g2()
                val memberBoolean = if (version > 2) buffer.g1() == 1 else null
                ClanChannelFull.Member(memberName, rank, world, memberBoolean)
            }
        return ClanChannelFull(
            channelIndex,
            ClanChannelFull.Channel(
                flags,
                version,
                discardedKey,
                updateNumber,
                name,
                headerBoolean,
                talkRank,
                kickRank,
                members,
            ),
        )
    }
}
