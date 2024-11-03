package net.rsprox.protocol.v224.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.Base37
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.clan.ClanChannelFull
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Consistent
public class ClanChannelFullDecoder : ProxyMessageDecoder<ClanChannelFull> {
    override val prot: ClientProt = GameServerProt.CLANCHANNEL_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanChannelFull {
        val clanType = buffer.g1()
        if (!buffer.isReadable) {
            return ClanChannelFull(
                clanType,
                ClanChannelFull.ClanChannelFullLeaveUpdate,
            )
        }
        val flags = buffer.g1()
        val version =
            if (flags and ClanChannelFull.FLAG_HAS_VERSION != 0) {
                buffer.g1()
            } else {
                ClanChannelFull.DEFAULT_OLDSCHOOL_VERSION
            }
        val clanHash = buffer.g8()
        val updateNum = buffer.g8()
        val clanName = buffer.gjstr()
        val discardedBoolean = buffer.gboolean()
        val kickRank = buffer.g1()
        val talkRank = buffer.g1()
        val memberCount = buffer.g2()
        val base37 = flags and ClanChannelFull.FLAG_USE_BASE_37_NAMES != 0
        val displayNames = flags and ClanChannelFull.FLAG_USE_DISPLAY_NAMES != 0
        check(base37 || displayNames) {
            "Unexpected behavior: Names not included in packet."
        }
        val members =
            buildList {
                for (i in 0..<memberCount) {
                    var name: String? = null
                    if (base37) {
                        name = Base37.decode(buffer.g8())
                    }
                    if (displayNames) {
                        name = buffer.gjstr()
                    }
                    val rank = buffer.g1()
                    val world = buffer.g2()
                    val discardedMemberBoolean =
                        if (version >= 3) {
                            buffer.gboolean()
                        } else {
                            false
                        }
                    add(
                        ClanChannelFull.ClanMember(
                            checkNotNull(name),
                            rank,
                            world,
                            discardedMemberBoolean,
                        ),
                    )
                }
            }
        return ClanChannelFull(
            clanType,
            ClanChannelFull.ClanChannelFullJoinUpdate(
                clanHash,
                updateNum,
                clanName,
                discardedBoolean,
                kickRank,
                talkRank,
                members,
                version,
                base37,
            ),
        )
    }
}
