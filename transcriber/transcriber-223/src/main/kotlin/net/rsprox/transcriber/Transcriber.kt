package net.rsprox.transcriber

import net.rsprot.protocol.Prot
import net.rsprox.cache.api.Cache
import net.rsprox.shared.SessionMonitor

public interface Transcriber :
    ClientPacketTranscriber,
    ServerPacketTranscriber {
    public val cache: Cache
    public val monitor: SessionMonitor<*>

    public fun setCurrentProt(prot: Prot)

    public fun onTranscribeStart()

    public fun onTranscribeEnd()
}
