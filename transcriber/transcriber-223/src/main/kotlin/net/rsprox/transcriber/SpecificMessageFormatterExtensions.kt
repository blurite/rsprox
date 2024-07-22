package net.rsprox.transcriber

import net.rsprox.protocol.common.CoordGrid

public fun MessageFormatter.coord(coordGrid: CoordGrid): String {
    return coord(coordGrid.level, coordGrid.x, coordGrid.z)
}
