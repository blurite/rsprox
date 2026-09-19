package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupMember
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.cache.readTypedVariable
import net.rsprox.protocol.session.Session

internal fun JagByteBuf.readPlayerGroupMember(
    named: Boolean,
    session: Session,
): PlayerGroupMember {
    val name = if (named) readNativeString() else null
    val flags = g1()
    val skillCount = g1()
    require(skillCount <= readableBytes() / 4) { "Truncated player-group skills" }
    // TODO Cache skill definitions gate native acceptance; retain all transmitted XP without guessing a maximum.
    val experience = List(skillCount) { g4() }
    val variableCount = g2()
    require(variableCount <= readableBytes() / 3) { "Truncated member variables" }
    val variables = List(variableCount) { readTypedVariable(session, Rs3VariableDomain.PLAYER) }
    return PlayerGroupMember(name, flags, experience, variables, g2(), g1(), g1(), g1())
}
