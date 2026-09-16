package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class MidiSongLocation(
    public val id: Int,
    public val radius: Int,
    public val coordinate: Int,
    public val volume: Int,
    public val range: Int,
) : IncomingServerGameMessage {
    public val level: Int
        get() = (coordinate ushr 28) and 0x3

    public val x: Int
        get() = (coordinate ushr 14) and 0x3FFF

    public val z: Int
        get() = coordinate and 0x3FFF
}
