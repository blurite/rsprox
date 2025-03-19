package net.rsprox.protocol.game.outgoing.model.map.util

public class BuildArea(
    private val zones: Array<Array<IntArray>>,
) {
    public constructor(
        levels: Int = 4,
        width: Int = 13,
        length: Int = 13,
    ) : this(
        Array(levels) {
            Array(width) {
                IntArray(length) {
                    -1
                }
            }
        },
    )

    public operator fun get(
        level: Int,
        zoneX: Int,
        zoneZ: Int,
    ): RebuildRegionZone {
        return RebuildRegionZone(zones[level][zoneX][zoneZ])
    }

    public operator fun set(
        level: Int,
        zoneX: Int,
        zoneZ: Int,
        value: RebuildRegionZone,
    ) {
        zones[level][zoneX][zoneZ] = value.packed
    }
}
