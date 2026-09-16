package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Target and flags retain their packed wire bits. Delay includes its high-bit mode;
 * height is signed, and id -1 denotes removal. Targets can be players, NPCs or map tiles.
 */
public data class SpotanimSpecific(
    public val height: Int,
    public val delay: Int,
    public val flags: Int,
    public val target: Int,
    public val id: Int,
    public val slot: Int,
) : IncomingServerGameMessage
