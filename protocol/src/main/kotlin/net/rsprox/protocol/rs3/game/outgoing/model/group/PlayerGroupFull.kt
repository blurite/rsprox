package net.rsprox.protocol.rs3.game.outgoing.model.group

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.common.TypedVariable

/** Null version means clear; versions above one install an empty group, not a clear. */
public data class PlayerGroupFull(
    public val version: Int?,
    public val group: Group?,
) : IncomingServerGameMessage {
    public data class Group(
        public val flags: Int,
        public val revision: Int,
        public val groupKey: Long,
        public val groupName: String,
        public val groupWord22: Int,
        public val groupInt98: Int,
        public val groupLongA0: Long,
        public val members: List<PlayerGroupMember>,
        public val bannedNames: List<String>,
        public val variables: List<TypedVariable>,
    )
}
