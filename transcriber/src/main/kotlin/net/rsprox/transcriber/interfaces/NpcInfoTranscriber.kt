package net.rsprox.transcriber.interfaces

import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfo

public fun interface NpcInfoTranscriber {
    public fun npcInfoV6(message: NpcInfo)
}
