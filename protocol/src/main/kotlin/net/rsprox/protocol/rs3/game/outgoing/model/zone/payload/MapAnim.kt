package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MapAnim(
    public val id: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val height: Int,
    public val delay: Int,
    public val rotation: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapAnim

        if (id != other.id) return false
        if (xInZone != other.xInZone) return false
        if (zInZone != other.zInZone) return false
        if (height != other.height) return false
        if (delay != other.delay) return false
        if (rotation != other.rotation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + xInZone
        result = 31 * result + zInZone
        result = 31 * result + height
        result = 31 * result + delay
        result = 31 * result + rotation
        return result
    }

    override fun toString(): String {
        return "MapAnim(id=$id, xInZone=$xInZone, zInZone=$zInZone, " +
            "height=$height, delay=$delay, rotation=$rotation)"
    }
}
