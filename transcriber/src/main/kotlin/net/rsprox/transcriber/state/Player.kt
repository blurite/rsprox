package net.rsprox.transcriber.state

import net.rsprox.protocol.common.CoordGrid

public data class Player(
    public val index: Int,
    public val name: String,
    public val coord: CoordGrid,
)
