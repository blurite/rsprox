package net.rsprox.transcriber.rs3.interfaces

import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcInfo

public interface Rs3NpcInfoTranscriber {
    public fun npcInfo(message: NpcInfo)
}
