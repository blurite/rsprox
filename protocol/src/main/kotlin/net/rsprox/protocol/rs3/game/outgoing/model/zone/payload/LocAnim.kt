package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class LocAnim(
    public val id: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val shape: Int,
    public val rotation: Int,
    public val delay: Int
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "LocAnim(id=$id, xInZone=$xInZone, zInZone=$zInZone, shape=$shape, rotation=$rotation, delay=$delay)"
    }
}
