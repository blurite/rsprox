package net.rsprox.transcriber

import net.rsprot.protocol.Prot
import net.rsprox.cache.api.Cache

public interface Transcriber :
    ClientPacketTranscriber,
    ServerPacketTranscriber {
    public val cache: Cache

    public fun setCurrentProt(prot: Prot)
}
