package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Coordinates are the transmitted offsets, before the client applies its scene origin and scale.
 * Type is signed; sourceX 255 disables the source coordinate, but sourceZ is still transmitted.
 */
public data class SetMapFlag(
    public val targetX: Int,
    public val type: Int,
    public val flags: Int,
    public val sourceZ: Int,
    public val id: Int,
    public val sourceX: Int,
    public val targetZ: Int,
) : IncomingServerGameMessage
