package net.rsprox.protocol.rs3.game.outgoing.model.clan

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class ClanChannelFull(
    public val channelIndex: Int,
    public val channel: Channel?,
) : IncomingServerGameMessage {
    public data class Channel(
        public val flags: Int,
        public val version: Int,
        public val discardedKey: Long,
        public val updateNumber: Long,
        public val name: String,
        public val headerBoolean: Boolean,
        public val talkRank: Int,
        public val kickRank: Int,
        public val members: List<Member>,
    )

    public data class Member(
        public val name: String,
        public val rank: Int,
        public val world: Int,
        public val memberBoolean: Boolean?,
    )
}
