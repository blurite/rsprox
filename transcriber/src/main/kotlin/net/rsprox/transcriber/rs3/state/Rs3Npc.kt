package net.rsprox.transcriber.rs3.state

import net.rsprox.protocol.common.CoordGrid

public data class Rs3Npc(
    public val index: Int,
    public val id: Int,
    public val name: String? = null,
    public val coord: CoordGrid? = null,
    public val spawnAngle: Int = 0,
)
