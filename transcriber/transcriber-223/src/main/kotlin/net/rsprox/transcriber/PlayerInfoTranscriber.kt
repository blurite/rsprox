package net.rsprox.transcriber

import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfo

public fun interface PlayerInfoTranscriber {
    public fun playerInfo(message: PlayerInfo)
}
