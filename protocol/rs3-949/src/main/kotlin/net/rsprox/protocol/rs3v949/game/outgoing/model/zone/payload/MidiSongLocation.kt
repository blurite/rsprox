package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MidiSongLocation(
    public val id: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val maxDistance: Int,
    public val minDistance: Int,
    public val volume: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiSongLocation

        if (id != other.id) return false
        if (xInZone != other.xInZone) return false
        if (zInZone != other.zInZone) return false
        if (maxDistance != other.maxDistance) return false
        if (minDistance != other.minDistance) return false
        if (volume != other.volume) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + xInZone
        result = 31 * result + zInZone
        result = 31 * result + maxDistance
        result = 31 * result + minDistance
        result = 31 * result + volume
        return result
    }

    override fun toString(): String {
        return "MidiSongLocation(id=$id, xInZone=$xInZone, zInZone=$zInZone, " +
            "maxDistance=$maxDistance, minDistance=$minDistance, volume=$volume)"
    }
}
