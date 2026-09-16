package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class TextCoord(
    public val duration: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val height: Int,
    public val rgb: Int,
    public val text: String,
    public val ignored: Int? = null,
) : IncomingServerGameMessage
