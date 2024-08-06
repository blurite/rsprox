package net.rsprox.proxy.worlds

import java.util.EnumSet

public data class RuneLiteWorld(
    public val id: Int,
    public val types: EnumSet<RuneLiteWorldType>,
    public val address: String,
    public val activity: String,
    public val location: Int,
    public val players: Int,
)
