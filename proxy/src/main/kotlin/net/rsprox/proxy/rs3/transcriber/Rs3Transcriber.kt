package net.rsprox.proxy.rs3.transcriber

import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3ClientPacketTranscriber
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3NpcInfoTranscriber
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3PlayerInfoTranscriber
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3ServerPacketTranscriber

public interface Rs3Transcriber :
    Rs3ClientPacketTranscriber,
    Rs3ServerPacketTranscriber,
    Rs3NpcInfoTranscriber,
    Rs3PlayerInfoTranscriber {
    public fun onTranscribeStart(): Boolean

    public fun onTranscribeEnd()
}
