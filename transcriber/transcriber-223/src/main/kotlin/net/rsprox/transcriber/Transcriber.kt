package net.rsprox.transcriber

import net.rsprot.protocol.Prot

public interface Transcriber :
    ClientPacketTranscriber,
    ServerPacketTranscriber {
    public fun setCurrentProt(prot: Prot)
}
