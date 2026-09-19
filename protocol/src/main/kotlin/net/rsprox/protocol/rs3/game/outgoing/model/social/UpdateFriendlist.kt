package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class UpdateFriendlist(
    public val friends: List<Friend>,
) : IncomingServerGameMessage {
    public data class Friend(
        public val rename: Int,
        public val name: String,
        public val previousName: String,
        public val world: Int,
        public val rank: Int,
        public val flags: Int,
        public val worldName: String?,
        public val platform: Int?,
        public val worldMetadata: Int?,
        public val note: String,
    )
}
