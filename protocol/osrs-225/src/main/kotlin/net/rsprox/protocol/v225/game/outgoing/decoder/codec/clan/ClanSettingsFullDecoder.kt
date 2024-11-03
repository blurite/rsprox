package net.rsprox.protocol.v225.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.clan.ClanSettingsFull
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class ClanSettingsFullDecoder : ProxyMessageDecoder<ClanSettingsFull> {
    override val prot: ClientProt = GameServerProt.CLANSETTINGS_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanSettingsFull {
        val clanType = buffer.g1()
        if (!buffer.isReadable) {
            return ClanSettingsFull(
                clanType,
                ClanSettingsFull.ClanSettingsFullLeaveUpdate,
            )
        }
        val version = buffer.g1()
        val flags = buffer.g1()
        val updateNum = buffer.g4()
        val creationTime = buffer.g4()
        val affinedMemberCount = buffer.g2()
        val bannedMemberCount = buffer.g1()
        val clanName = buffer.gjstr()
        if (version >= 4) {
            buffer.skipRead(4)
        }
        val allowUnaffined = buffer.g1() == 1
        val talkRank = buffer.g1()
        val kickRank = buffer.g1()
        val lootshareRank = buffer.g1()
        val coinshareRank = buffer.g1()
        val hasAffinedHashes = flags and ClanSettingsFull.FLAG_HAS_AFFINED_HASHES != 0
        val hasAffinedDisplayNames = flags and ClanSettingsFull.FLAG_HAS_AFFINED_DISPLAY_NAMES != 0
        check(hasAffinedHashes || hasAffinedDisplayNames) {
            "Unexpected behavior: No hashes or display names."
        }
        check(hasAffinedHashes != hasAffinedDisplayNames) {
            "Unexpected behavior: Two ways of writing names detected"
        }
        val affinedMembers =
            buildList {
                for (i in 0..<affinedMemberCount) {
                    val hash =
                        if (hasAffinedHashes) {
                            buffer.g8()
                        } else {
                            -1L
                        }
                    val displayName =
                        if (hasAffinedDisplayNames) {
                            buffer.gjstrnull()
                        } else {
                            null
                        }
                    val rank = buffer.g1()
                    val extraInfo =
                        if (version >= 2) {
                            buffer.g4()
                        } else {
                            0
                        }
                    val joinRuneDay =
                        if (version >= 5) {
                            buffer.g2()
                        } else {
                            0
                        }
                    val muted =
                        if (version >= 6) {
                            buffer.g1() == 1
                        } else {
                            false
                        }
                    if (hasAffinedHashes) {
                        add(
                            ClanSettingsFull.AffinedClanMember(
                                hash,
                                rank,
                                extraInfo,
                                joinRuneDay,
                                muted,
                            ),
                        )
                    } else {
                        add(
                            ClanSettingsFull.AffinedClanMember(
                                checkNotNull(displayName),
                                rank,
                                extraInfo,
                                joinRuneDay,
                                muted,
                            ),
                        )
                    }
                }
            }
        val bannedMembers =
            buildList {
                for (i in 0..<bannedMemberCount) {
                    val hash =
                        if (hasAffinedHashes) {
                            buffer.g8()
                        } else {
                            -1L
                        }
                    val displayName =
                        if (hasAffinedDisplayNames) {
                            buffer.gjstrnull()
                        } else {
                            null
                        }
                    if (hasAffinedHashes) {
                        add(ClanSettingsFull.BannedClanMember(hash))
                    } else {
                        add(ClanSettingsFull.BannedClanMember(checkNotNull(displayName)))
                    }
                }
            }
        val settings =
            if (version >= 3) {
                val settingsCount = buffer.g2()
                buildList {
                    for (i in 0..<settingsCount) {
                        val header = buffer.g4()
                        val id = header and 0x3FFFFFFF
                        when (val type = header ushr 30) {
                            0 -> {
                                val value = buffer.g4()
                                add(ClanSettingsFull.IntClanSetting(id, value))
                            }
                            1 -> {
                                val value = buffer.g8()
                                add(ClanSettingsFull.LongClanSetting(id, value))
                            }
                            2 -> {
                                val value = buffer.gjstr()
                                add(ClanSettingsFull.StringClanSetting(id, value))
                            }
                            else -> {
                                error("Unknown type: $type")
                            }
                        }
                    }
                }
            } else {
                emptyList()
            }
        return ClanSettingsFull(
            clanType,
            ClanSettingsFull.ClanSettingsFullJoinUpdate(
                updateNum,
                creationTime,
                clanName,
                allowUnaffined,
                talkRank,
                kickRank,
                lootshareRank,
                coinshareRank,
                affinedMembers,
                bannedMembers,
                settings,
                hasAffinedHashes,
                hasAffinedDisplayNames,
            ),
        )
    }
}
