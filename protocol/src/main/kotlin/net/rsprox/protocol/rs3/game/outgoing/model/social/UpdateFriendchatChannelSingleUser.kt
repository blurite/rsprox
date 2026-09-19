package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class UpdateFriendchatChannelSingleUser(
    public val name: String,
    public val aliasFlag: Int,
    public val alias: String?,
    public val world: Int,
    public val rank: Int,
    public val worldName: String?,
) : IncomingServerGameMessage
