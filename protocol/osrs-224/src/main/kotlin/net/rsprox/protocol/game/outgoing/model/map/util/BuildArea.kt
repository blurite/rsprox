package net.rsprox.protocol.game.outgoing.model.map.util

public class BuildArea(
    private val zones: Array<Array<IntArray>>,
) {
    public constructor() : this(
        Array(4) {
            Array(13) {
                IntArray(13) {
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

    internal operator fun set(
        level: Int,
        zoneX: Int,
        zoneZ: Int,
        value: RebuildRegionZone,
    ) {
        zones[level][zoneX][zoneZ] = value.packed
    }
}
