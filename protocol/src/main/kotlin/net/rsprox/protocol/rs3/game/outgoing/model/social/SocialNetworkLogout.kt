package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class SocialNetworkLogout(public val url: String) : IncomingServerGameMessage
