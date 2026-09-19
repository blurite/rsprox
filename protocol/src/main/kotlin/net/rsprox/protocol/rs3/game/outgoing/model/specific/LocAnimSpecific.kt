package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class LocAnimSpecific(
    public val shapeRotation: Int,
    /** Animation start delay in 20 ms client cycles. */
    public val delay: Int,
    public val animation: Int,
    public val coordinate: Int,
) : IncomingServerGameMessage {
    public val shape: Int get() = shapeRotation ushr 2
    public val rotation: Int get() = shapeRotation and 3
}
