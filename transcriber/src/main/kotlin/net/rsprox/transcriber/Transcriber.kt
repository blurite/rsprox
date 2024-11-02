package net.rsprox.transcriber

import net.rsprox.cache.api.Cache
import net.rsprox.transcriber.interfaces.ClientPacketTranscriber
import net.rsprox.transcriber.interfaces.NpcInfoTranscriber
import net.rsprox.transcriber.interfaces.PlayerInfoTranscriber
import net.rsprox.transcriber.interfaces.ServerPacketTranscriber

public interface Transcriber :
    ClientPacketTranscriber,
    ServerPacketTranscriber,
    PlayerInfoTranscriber,
    NpcInfoTranscriber {
    public val cache: Cache

    public fun onTranscribeStart()

    public fun onTranscribeEnd()
}
