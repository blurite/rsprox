package net.rsprox.protocol.v224.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.clan.ClanChannelDelta
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class ClanChannelDeltaDecoder : ProxyMessageDecoder<ClanChannelDelta> {
    override val prot: ClientProt = GameServerProt.CLANCHANNEL_DELTA

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanChannelDelta {
        val clanType = buffer.g1()
        val clanHash = buffer.g8()
        val updateNum = buffer.g8()
        val events =
            buildList {
                while (buffer.isReadable) {
                    when (val opcode = buffer.g1()) {
                        0 -> break
                        1 -> {
                            buffer.skipRead(1)
                            // TODO: Should name be nullable?
                            val name = buffer.gjstrnull() ?: ""
                            val world = buffer.g2()
                            val rank = buffer.g1()
                            add(
                                ClanChannelDelta.ClanChannelDeltaAddUserEvent(
                                    name,
                                    world,
                                    rank,
                                ),
                            )
                        }
                        2 -> {
                            val index = buffer.g2()
                            val rank = buffer.g1()
                            val world = buffer.g2()
                            buffer.skipRead(8)
                            val name = buffer.gjstr()
                            add(
                                ClanChannelDelta.ClanChannelDeltaUpdateUserDetailsEvent(
                                    index,
                                    name,
                                    rank,
                                    world,
                                ),
                            )
                        }
                        3 -> {
                            val index = buffer.g2()
                            buffer.skipRead(1)
                            buffer.skipRead(1)
                            add(
                                ClanChannelDelta.ClanChannelDeltaDeleteUserEvent(
                                    index,
                                ),
                            )
                        }
                        4 -> {
                            val name = buffer.gjstrnull()
                            if (name != null) {
                                buffer.skipRead(1)
                                val talkRank = buffer.g1()
                                val kickRank = buffer.g1()
                                add(
                                    ClanChannelDelta.ClanChannelDeltaUpdateBaseSettingsEvent(
                                        name,
                                        talkRank,
                                        kickRank,
                                    ),
                                )
                            } else {
                                add(ClanChannelDelta.ClanChannelDeltaUpdateBaseSettingsEvent())
                            }
                        }
                        5 -> {
                            buffer.skipRead(1)
                            val index = buffer.g2()
                            val rank = buffer.g1()
                            val world = buffer.g2()
                            buffer.skipRead(8)
                            val name = buffer.gjstr()
                            buffer.skipRead(1)
                            add(
                                ClanChannelDelta.ClanChannelDeltaUpdateUserDetailsV2Event(
                                    index,
                                    name,
                                    rank,
                                    world,
                                ),
                            )
                        }
                        else -> throw IllegalStateException("Unknown clanchannel delta update: $opcode")
                    }
                }
            }
        return ClanChannelDelta(
            clanType,
            clanHash,
            updateNum,
            events,
        )
    }
}
