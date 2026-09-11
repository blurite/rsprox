package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class LocDel(
    public val xInZone: Int,
    public val zInZone: Int,
    public val shape: Int,
    public val rotation: Int,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "LocDel(xInZone=$xInZone, zInZone=$zInZone, shape=$shape, rotation=$rotation)"
    }
}
