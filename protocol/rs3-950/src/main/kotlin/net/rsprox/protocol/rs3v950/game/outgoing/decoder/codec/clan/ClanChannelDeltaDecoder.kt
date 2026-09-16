package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanChannelDelta
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.rs3v950.buffer.readNativeString

internal class ClanChannelDeltaDecoder : ProxyMessageDecoder<ClanChannelDelta> {
    override val prot: ClientProt = GameServerProt.CLANCHANNEL_DELTA

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanChannelDelta {
        val channelIndex = buffer.g1s()
        val discardedKey = buffer.g8()
        val updateNumber = buffer.g8()
        val records = mutableListOf<ClanChannelDelta.Record>()
        var terminator: Int
        while (true) {
            val type = buffer.g1()
            terminator = type
            val record =
                when (type) {
                    1 -> {
                        val sentinel = buffer.g1()
                        if (sentinel != 255) {
                            ClanChannelDelta.Rejected(type, sentinel)
                        } else {
                            val name = buffer.readNativeString()
                            val world = buffer.g2()
                            val rank = buffer.g1s()
                            val memberIdentity = buffer.g8()
                            ClanChannelDelta.Add(name, world, rank, memberIdentity)
                        }
                    }
                    3 -> {
                        val index = buffer.g2()
                        val auxiliary = buffer.g1()
                        val sentinel = buffer.g1()
                        ClanChannelDelta.Remove(index, auxiliary, sentinel)
                    }
                    4 -> {
                        val name = buffer.readNativeString()
                        val headerBoolean = buffer.g1() == 1
                        val kickRank = buffer.g1s()
                        val talkRank = buffer.g1s()
                        ClanChannelDelta.Header(name, headerBoolean, kickRank, talkRank)
                    }
                    5 -> {
                        val unused = buffer.g1()
                        val index = buffer.g2()
                        val rank = buffer.g1s()
                        val world = buffer.g2()
                        val memberIdentity = buffer.g8()
                        val name = buffer.readNativeString()
                        val memberBoolean = buffer.g1() == 1
                        ClanChannelDelta.Member(unused, index, rank, world, memberIdentity, name, memberBoolean)
                    }
                    else -> break
                }
            records += record
        }
        return ClanChannelDelta(channelIndex, discardedKey, updateNumber, records, terminator)
    }
}
