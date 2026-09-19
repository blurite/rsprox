package net.rsprox.protocol.rs3.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock

public data class Reconnect(
    public val playerInfoInit: PlayerInfoInitBlock,
) : IncomingServerGameMessage
