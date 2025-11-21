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
        val x = zones.getOrNull(level) ?: return INVALID
        val y = x.getOrNull(zoneX) ?: return INVALID
        val packed = y.getOrNull(zoneZ) ?: return INVALID
        return RebuildRegionZone(packed)
    }

    public operator fun set(
        level: Int,
        zoneX: Int,
        zoneZ: Int,
        value: RebuildRegionZone,
    ) {
        zones[level][zoneX][zoneZ] = value.packed
    }

    private companion object {
        private val INVALID = RebuildRegionZone(-1)
    }
}
