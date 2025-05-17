package net.rsprox.transcriber.state

import net.rsprox.protocol.common.CoordGrid

public data class Npc(
    public val index: Int,
    public val id: Int,
    public val creationCycle: Int,
    public val angle: Int,
    public val coord: CoordGrid,
    public val name: String? = null,
)
