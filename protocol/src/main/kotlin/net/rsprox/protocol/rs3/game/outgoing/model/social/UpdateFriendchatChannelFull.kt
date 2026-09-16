package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class UpdateFriendchatChannelFull(
    public val channel: Channel?,
) : IncomingServerGameMessage {
    public data class Channel(
        public val owner: String,
        public val aliasFlag: Int,
        public val alias: String?,
        public val name: String,
        public val minimumKickRank: Int,
        public val memberCount: Int,
        public val members: List<Member>?,
    )

    public data class Member(
        public val name: String,
        public val aliasFlag: Int,
        public val alias: String?,
        public val world: Int,
        public val rank: Int,
        public val worldName: String,
    )
}
