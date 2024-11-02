package net.rsprox.transcriber.impl

import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfo

public fun interface NpcInfoTranscriber {
    public fun npcInfoV5(message: NpcInfo)
}
