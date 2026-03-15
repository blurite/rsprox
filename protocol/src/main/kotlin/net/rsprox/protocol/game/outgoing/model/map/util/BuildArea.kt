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

    /**
     * Calculates a simple, complete block of the land being instanced.
     * This is primarily to avoid spamming the logs with (x, y) -> (a, b); (x + 1, y) -> (a + 1, b) and so on.
     * If the zones are being copied in a 1:1 formation, this method will return that formation composition.
     * Any other scenario, null is returned and full description is required.
     */
    public fun calculateSimpleBlockOrNull(): SimpleBlock? {
        var minX = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var minZ = Int.MAX_VALUE
        var maxZ = Int.MIN_VALUE
        var minLevel = Int.MAX_VALUE
        var maxLevel = Int.MIN_VALUE

        for (level in zones.indices) {
            for (zoneX in zones[level].indices) {
                for (zoneZ in zones[level][zoneX].indices) {
                    val zone = get(level, zoneX, zoneZ)
                    if (zone.mapsquareId == 32767) continue
                    // Assume non-simple block if something is rotated.
                    // The simple blocks we're going for are instances like fight caves
                    // Where it just copies a static map 1:1
                    if (zone.rotation != 0) return null
                    minX = minOf(minX, zoneX)
                    maxX = maxOf(maxX, zoneX)
                    minZ = minOf(minZ, zoneZ)
                    maxZ = maxOf(maxZ, zoneZ)
                    minLevel = minOf(minLevel, level)
                    maxLevel = maxOf(maxLevel, level)
                }
            }
        }

        if (minX == Int.MAX_VALUE) return null

        for (level in minLevel..maxLevel) {
            for (zoneX in minX..maxX) {
                for (zoneZ in minZ..maxZ) {
                    val zone = get(level, zoneX, zoneZ)
                    if (zone.mapsquareId == 32767) {
                        return null
                    }
                }
            }
        }

        return SimpleBlock(minLevel, maxLevel, minX, maxX, minZ, maxZ)
    }

    public data class SimpleBlock(
        val minLevel: Int,
        val maxLevel: Int,
        val minX: Int,
        val maxX: Int,
        val minZ: Int,
        val maxZ: Int,
    )

    private companion object {
        private val INVALID = RebuildRegionZone(-1)
    }
}
