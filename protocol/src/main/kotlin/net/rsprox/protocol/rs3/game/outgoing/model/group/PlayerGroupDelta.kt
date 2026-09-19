package net.rsprox.protocol.rs3.game.outgoing.model.group

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.common.TypedVariable

public data class PlayerGroupDelta(
    public val discardedKey: Long,
    public val revision: Int,
    public val records: List<Record>,
    public val terminator: Int,
) : IncomingServerGameMessage {
    /** Preserve wire order. No group mutation is performed by these transcript models. */
    public sealed interface Record

    public data class Rejected(
        public val type: Int,
        public val sentinel: Int,
    ) : Record

    public data class AddMember(
        public val member: PlayerGroupMember,
    ) : Record

    public data class RemoveMember(
        public val memberIndex: Int,
    ) : Record

    public data class AddBanned(
        public val name: String,
    ) : Record

    public data class RemoveBanned(
        public val bannedIndex: Int,
    ) : Record

    public data class MemberByte35(
        public val memberIndex: Int,
        public val value: Int,
    ) : Record

    public data class MemberWorld(
        public val memberIndex: Int,
        public val world: Int,
    ) : Record

    public data class MemberOffline(
        public val memberIndex: Int,
    ) : Record

    /** The client tests value == 1; retain the original byte for inspection. */
    public data class MemberState(
        public val memberIndex: Int,
        public val value: Int,
    ) : Record

    public data object AllMembersState2 : Record

    public data object AllMembersState3 : Record

    public data class UpdateMember(
        public val memberIndex: Int,
        public val member: PlayerGroupMember,
    ) : Record

    public data class Variable(
        public val variable: TypedVariable,
    ) : Record

    public data object NoVariable : Record

    public data class Varbit(
        public val id: Int,
        public val value: Int?,
    ) : Record

    public data class MemberByte3c(
        public val memberIndex: Int,
        public val value: Int,
    ) : Record
}
