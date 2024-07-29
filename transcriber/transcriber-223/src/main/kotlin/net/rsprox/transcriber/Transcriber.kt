package net.rsprox.transcriber

import net.rsprot.protocol.Prot
import net.rsprox.cache.api.Cache
import net.rsprox.shared.SessionMonitor
import net.rsprox.transcriber.impl.ClientPacketTranscriber
import net.rsprox.transcriber.impl.NpcInfoTranscriber
import net.rsprox.transcriber.impl.PlayerInfoTranscriber
import net.rsprox.transcriber.impl.ServerPacketTranscriber

public interface Transcriber :
    ClientPacketTranscriber,
    ServerPacketTranscriber,
    PlayerInfoTranscriber,
    NpcInfoTranscriber {
    public val cache: Cache
    public val monitor: SessionMonitor<*>

    public fun setCurrentProt(prot: Prot)

    public fun onTranscribeStart()

    public fun onTranscribeEnd()
}
