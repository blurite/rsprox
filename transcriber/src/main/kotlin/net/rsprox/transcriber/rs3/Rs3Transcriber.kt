package net.rsprox.transcriber.rs3

import net.rsprox.transcriber.rs3.interfaces.Rs3ClientPacketTranscriber
import net.rsprox.transcriber.rs3.interfaces.Rs3NpcInfoTranscriber
import net.rsprox.transcriber.rs3.interfaces.Rs3PlayerInfoTranscriber
import net.rsprox.transcriber.rs3.interfaces.Rs3ServerPacketTranscriber

public interface Rs3Transcriber :
    Rs3ClientPacketTranscriber,
    Rs3ServerPacketTranscriber,
    Rs3NpcInfoTranscriber,
    Rs3PlayerInfoTranscriber {
    public fun onTranscribeStart(): Boolean

    public fun onTranscribeEnd()

    public fun onTranscribeFailure(exception: Exception)
}
