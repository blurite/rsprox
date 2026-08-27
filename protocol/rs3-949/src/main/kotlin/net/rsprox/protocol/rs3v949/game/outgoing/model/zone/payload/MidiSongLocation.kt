package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MidiSongLocation(
    public val id: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val heightAdjust: Int,
    public val flagsValue: Int,
    public val rotationByte: Int,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "MidiSongLocation(id=$id, xInZone=$xInZone, zInZone=$zInZone, heightAdjust=$heightAdjust, flagsValue=$flagsValue, rotationByte=$rotationByte)"
    }
}
