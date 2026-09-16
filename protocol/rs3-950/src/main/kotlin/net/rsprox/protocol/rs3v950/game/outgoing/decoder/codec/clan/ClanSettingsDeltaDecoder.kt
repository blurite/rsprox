package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanSettingsDelta
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.rs3v950.buffer.readNativeString

internal class ClanSettingsDeltaDecoder : ProxyMessageDecoder<ClanSettingsDelta> {
    override val prot: ClientProt = GameServerProt.CLANSETTINGS_DELTA

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanSettingsDelta {
        val channelIndex = buffer.g1s()
        val discardedKey = buffer.g8()
        val revision = buffer.g4()
        val records = mutableListOf<ClanSettingsDelta.Record>()
        var terminator: Int
        while (true) {
            val type = buffer.g1()
            terminator = type
            val record =
                when (type) {
                    1, 3, 13 -> {
                        val sentinel = buffer.g1()
                        if (sentinel != 255) {
                            ClanSettingsDelta.Rejected(type, sentinel)
                        } else {
                            val name = buffer.readNativeString()
                            val joinedDay = if (type == 13) buffer.g2() else null
                            ClanSettingsDelta.AddName(type, name, joinedDay)
                        }
                    }
                    2 -> ClanSettingsDelta.Rank(buffer.g2(), buffer.g1s())
                    4 -> {
                        val allowGuests = buffer.g1() == 1
                        val rank0 = buffer.g1s()
                        val rank1 = buffer.g1s()
                        val rank2 = buffer.g1s()
                        val headerBoolean = buffer.g1() == 1
                        ClanSettingsDelta.Permissions(allowGuests, rank0, rank1, rank2, headerBoolean)
                    }
                    5, 6 -> ClanSettingsDelta.Remove(type, buffer.g2())
                    7 -> ClanSettingsDelta.MemberBits(buffer.g2(), buffer.g4(), buffer.g1(), buffer.g1())
                    8 -> ClanSettingsDelta.IntParameter(buffer.g4(), buffer.g4())
                    9 -> ClanSettingsDelta.LongParameter(buffer.g4(), buffer.g8())
                    10 -> ClanSettingsDelta.StringParameter(buffer.g4(), buffer.readNativeString())
                    11 -> ClanSettingsDelta.IntBits(buffer.g4(), buffer.g4(), buffer.g1(), buffer.g1())
                    12 -> ClanSettingsDelta.Name(buffer.readNativeString(), buffer.g4())
                    14 -> ClanSettingsDelta.Mute(buffer.g2(), buffer.g1() == 1)
                    else -> break
                }
            records += record
        }
        return ClanSettingsDelta(channelIndex, discardedKey, revision, records, terminator)
    }
}
