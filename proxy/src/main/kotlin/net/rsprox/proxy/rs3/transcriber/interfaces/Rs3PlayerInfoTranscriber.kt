package net.rsprox.proxy.rs3.transcriber.interfaces

import net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.PlayerInfo

public interface Rs3PlayerInfoTranscriber {
    public fun playerInfo(message: PlayerInfo)
}
