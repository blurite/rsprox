package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupDelta
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.cache.readTypedVariable
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PlayerGroupDeltaDecoder : ProxyMessageDecoder<PlayerGroupDelta> {
    override val prot: ClientProt = GameServerProt.PLAYER_GROUP_DELTA

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerGroupDelta {
        val discardedKey = buffer.g8()
        val revision = buffer.g4()
        val records = mutableListOf<PlayerGroupDelta.Record>()
        var terminator: Int
        while (true) {
            val type = buffer.g1()
            terminator = type
            val record =
                when (type) {
                    1 -> {
                        val sentinel = buffer.g1()
                        if (sentinel == 255) {
                            PlayerGroupDelta.AddMember(buffer.readPlayerGroupMember(true, session))
                        } else {
                            PlayerGroupDelta.Rejected(type, sentinel)
                        }
                    }
                    2 -> PlayerGroupDelta.RemoveMember(buffer.g2())
                    3 -> {
                        val sentinel = buffer.g1()
                        if (sentinel == 255) {
                            PlayerGroupDelta.AddBanned(buffer.readNativeString())
                        } else {
                            PlayerGroupDelta.Rejected(type, sentinel)
                        }
                    }
                    4 -> PlayerGroupDelta.RemoveBanned(buffer.g2())
                    5 -> PlayerGroupDelta.MemberByte35(buffer.g2(), buffer.g1())
                    6 -> PlayerGroupDelta.MemberWorld(buffer.g2(), buffer.g2())
                    7 -> PlayerGroupDelta.MemberOffline(buffer.g2())
                    8 -> PlayerGroupDelta.MemberState(buffer.g2(), buffer.g1())
                    9 -> PlayerGroupDelta.AllMembersState2
                    10 -> PlayerGroupDelta.AllMembersState3
                    11 -> {
                        val memberIndex = buffer.g2()
                        PlayerGroupDelta.UpdateMember(
                            memberIndex,
                            buffer.readPlayerGroupMember(false, session),
                        )
                    }
                    12 -> {
                        val id = buffer.g2()
                        if (id == 65535) {
                            PlayerGroupDelta.NoVariable
                        } else {
                            PlayerGroupDelta.Variable(
                                buffer.readTypedVariable(session, Rs3VariableDomain.PLAYER_GROUP, id),
                            )
                        }
                    }
                    13 -> {
                        // Revision 950 has a three-byte varbit ID, not the older two-byte encoding.
                        val id = buffer.g3()
                        PlayerGroupDelta.Varbit(id, if (id == 16777215) null else buffer.g4())
                    }
                    14 -> PlayerGroupDelta.MemberByte3c(buffer.g2(), buffer.g1())
                    else -> break
                }
            records += record
        }
        return PlayerGroupDelta(discardedKey, revision, records, terminator)
    }
}
