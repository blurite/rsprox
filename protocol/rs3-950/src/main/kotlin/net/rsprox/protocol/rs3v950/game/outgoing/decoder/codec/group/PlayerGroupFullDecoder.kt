package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupFull
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.cache.readTypedVariable
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PlayerGroupFullDecoder : ProxyMessageDecoder<PlayerGroupFull> {
    override val prot: ClientProt = GameServerProt.PLAYER_GROUP_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerGroupFull {
        if (buffer.readableBytes() == 0) return PlayerGroupFull(null, null)
        val version = buffer.g1()
        if (version > 1) return PlayerGroupFull(version, null)
        val flags = buffer.g1()
        val revision = buffer.g4()
        val groupKey = buffer.g8()
        val groupName = buffer.readNativeString()
        val groupWord22 = buffer.g2()
        val groupInt98 = buffer.g4()
        val groupLongA0 = buffer.g8()
        val memberCount = buffer.g2()
        require(memberCount <= buffer.readableBytes() / 9) { "Truncated player-group members" }
        val members =
            List(memberCount) {
                buffer.readPlayerGroupMember(flags and 4 != 0, session)
            }
        val bannedCount = buffer.g2()
        require(bannedCount <= buffer.readableBytes()) { "Truncated player-group banned names" }
        val bannedNames = List(bannedCount) { buffer.readNativeString() }
        val variableCount = buffer.g2()
        require(variableCount <= buffer.readableBytes() / 3) { "Truncated group variables" }
        val variables = List(variableCount) { buffer.readTypedVariable(session, Rs3VariableDomain.PLAYER_GROUP) }
        return PlayerGroupFull(
            version,
            PlayerGroupFull.Group(
                flags,
                revision,
                groupKey,
                groupName,
                groupWord22,
                groupInt98,
                groupLongA0,
                members,
                bannedNames,
                variables,
            ),
        )
    }
}
