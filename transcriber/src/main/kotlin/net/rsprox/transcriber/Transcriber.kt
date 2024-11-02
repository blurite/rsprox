package net.rsprox.transcriber

import net.rsprox.cache.api.Cache
import net.rsprox.shared.SessionMonitor
import net.rsprox.transcriber.interfaces.ClientPacketTranscriber
import net.rsprox.transcriber.interfaces.NpcInfoTranscriber
import net.rsprox.transcriber.interfaces.PlayerInfoTranscriber
import net.rsprox.transcriber.interfaces.ServerPacketTranscriber
import net.rsprox.transcriber.prot.Prot

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
