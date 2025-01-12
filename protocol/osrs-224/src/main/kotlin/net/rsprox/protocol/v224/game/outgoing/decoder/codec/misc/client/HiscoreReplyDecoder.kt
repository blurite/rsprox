package net.rsprox.protocol.v224.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.HiscoreReply
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class HiscoreReplyDecoder : ProxyMessageDecoder<HiscoreReply> {
    override val prot: ClientProt = GameServerProt.HISCORE_REPLY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HiscoreReply {
        val requestId = buffer.g1()
        return when (val responseType = buffer.g1()) {
            0 -> {
                val version = buffer.g1()
                val statCount = buffer.g1()
                val stats =
                    buildList {
                        for (i in 0..<statCount) {
                            val id = buffer.g2()
                            val rank = buffer.g4()
                            val result = buffer.g4()
                            add(
                                HiscoreReply.HiscoreResult(
                                    id,
                                    rank,
                                    result,
                                ),
                            )
                        }
                    }
                val overallRank = buffer.g4()
                val overallExperience = buffer.g8()
                val activityCount = buffer.g2()
                val activities =
                    buildList {
                        for (i in 0..<activityCount) {
                            val id = buffer.g2()
                            val rank = buffer.g4()
                            val result = buffer.g4()
                            add(
                                HiscoreReply.HiscoreResult(
                                    id,
                                    rank,
                                    result,
                                ),
                            )
                        }
                    }
                HiscoreReply(
                    requestId,
                    HiscoreReply.SuccessfulHiscoreReply(
                        version,
                        stats,
                        overallRank,
                        overallExperience,
                        activities,
                    ),
                )
            }
            1 -> {
                val reason = buffer.gjstr()
                HiscoreReply(
                    requestId,
                    HiscoreReply.FailedHiscoreReply(reason),
                )
            }
            else -> error("Invalid hiscore reply type: $responseType")
        }
    }
}
