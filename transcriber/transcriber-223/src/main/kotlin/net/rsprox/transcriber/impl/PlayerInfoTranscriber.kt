package net.rsprox.transcriber.impl

import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfo

public fun interface PlayerInfoTranscriber {
    public fun playerInfo(message: PlayerInfo)
}
