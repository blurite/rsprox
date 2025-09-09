package net.rsprox.protocol.v233.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.clan.ClanSettingsDelta
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class ClanSettingsDeltaDecoder : ProxyMessageDecoder<ClanSettingsDelta> {
    override val prot: ClientProt = GameServerProt.CLANSETTINGS_DELTA

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanSettingsDelta {
        val clanType = buffer.g1()
        val owner = buffer.g8()
        val updateNum = buffer.g4()
        val updates =
            buildList {
                while (buffer.isReadable) {
                    when (val opcode = buffer.g1()) {
                        0 -> break
                        1 -> {
                            val hashOpcode = buffer.g1()
                            val hash =
                                if (hashOpcode != 0xFF) {
                                    buffer.readerIndex(buffer.readerIndex() - 1)
                                    buffer.g8()
                                } else {
                                    -1L
                                }
                            val name = buffer.gjstrnull()
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaAddMemberV1Update(
                                    hash,
                                    name,
                                ),
                            )
                        }
                        2 -> {
                            val index = buffer.g2()
                            val rank = buffer.g1()
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaSetMemberRankUpdate(
                                    index,
                                    rank,
                                ),
                            )
                        }
                        3 -> {
                            val hashOpcode = buffer.g1()
                            val hash =
                                if (hashOpcode != 0xFF) {
                                    buffer.readerIndex(buffer.readerIndex() - 1)
                                    buffer.g8()
                                } else {
                                    -1L
                                }
                            val name = buffer.gjstrnull()
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaAddBannedUpdate(
                                    hash,
                                    name,
                                ),
                            )
                        }
                        4 -> {
                            val allowUnaffined = buffer.g1() == 1
                            val talkRank = buffer.g1()
                            val kickRank = buffer.g1()
                            val lootshareRank = buffer.g1()
                            val coinshareRank = buffer.g1()
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaBaseSettingsUpdate(
                                    allowUnaffined,
                                    talkRank,
                                    kickRank,
                                    lootshareRank,
                                    coinshareRank,
                                ),
                            )
                        }
                        5 -> {
                            val index = buffer.g2()
                            add(ClanSettingsDelta.ClanSettingsDeltaDeleteMemberUpdate(index))
                        }
                        6 -> {
                            val index = buffer.g2()
                            add(ClanSettingsDelta.ClanSettingsDeltaDeleteBannedUpdate(index))
                        }
                        7 -> {
                            val index = buffer.g2()
                            val value = buffer.g4()
                            val startBit = buffer.g1()
                            val endBit = buffer.g1()
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaSetMemberExtraInfoUpdate(
                                    index,
                                    value,
                                    startBit,
                                    endBit,
                                ),
                            )
                        }
                        8 -> {
                            val setting = buffer.g4()
                            val value = buffer.g4()
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaSetIntSettingUpdate(
                                    setting,
                                    value,
                                ),
                            )
                        }
                        9 -> {
                            val setting = buffer.g4()
                            val value = buffer.g8()
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaSetLongSettingUpdate(
                                    setting,
                                    value,
                                ),
                            )
                        }
                        10 -> {
                            val setting = buffer.g4()
                            val value = buffer.gjstr()
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaSetStringSettingUpdate(
                                    setting,
                                    value,
                                ),
                            )
                        }
                        11 -> {
                            val setting = buffer.g4()
                            val value = buffer.g4()
                            val startBit = buffer.g1()
                            val endBit = buffer.g1()
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaSetVarbitSettingUpdate(
                                    setting,
                                    value,
                                    startBit,
                                    endBit,
                                ),
                            )
                        }
                        12 -> {
                            val clanName = buffer.gjstr()
                            buffer.skipRead(4)
                            add(ClanSettingsDelta.ClanSettingsDeltaSetClanNameUpdate(clanName))
                        }
                        13 -> {
                            val hashOpcode = buffer.g1()
                            val hash =
                                if (hashOpcode != 0xFF) {
                                    buffer.readerIndex(buffer.readerIndex() - 1)
                                    buffer.g8()
                                } else {
                                    -1L
                                }
                            val name = buffer.gjstrnull()
                            val joinRuneDay = buffer.g2()
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaAddMemberV2Update(
                                    hash,
                                    name,
                                    joinRuneDay,
                                ),
                            )
                        }
                        14 -> {
                            val index = buffer.g2()
                            val muted = buffer.g1() == 1
                            add(
                                ClanSettingsDelta.ClanSettingsDeltaSetMemberMutedUpdate(
                                    index,
                                    muted,
                                ),
                            )
                        }
                        15 -> {
                            val index = buffer.g2()
                            add(ClanSettingsDelta.ClanSettingsDeltaSetClanOwnerUpdate(index))
                        }
                        else -> error("Unknown opcode: $opcode")
                    }
                }
            }
        return ClanSettingsDelta(
            clanType,
            owner,
            updateNum,
            updates,
        )
    }
}
