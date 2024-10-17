package net.rsprox.transcriber.impl

import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfoV5

public fun interface NpcInfoTranscriber {
    public fun npcInfo(message: NpcInfoV5)
}
