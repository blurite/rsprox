package net.rsprox.transcriber

public interface Transcriber :
    ClientPacketTranscriber,
    ServerPacketTranscriber {
    public fun format(
        cycle: Int,
        name: String,
        properties: List<String>,
    ): String
}
