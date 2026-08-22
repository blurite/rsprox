package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MapAnim(
    public val id: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val rotation: Int,
    public val delay: Int,
    public val height: Int,
    public val scale: Int,
    public val priority: Int,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "MapAnim(id=$id, xInZone=$xInZone, zInZone=$zInZone, rotation=$rotation, delay=$delay, height=$height, scale=$scale, priority=$priority)"
    }
}
