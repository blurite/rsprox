package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class LocPrefetch(
    public val id: Int,
    public val shape: Int,
    public val rotation: Int,
    public val rawBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "LocPrefetch(id=$id, shape=$shape, rotation=$rotation)"
    }
}
