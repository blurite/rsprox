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
    public fun calculateSimpleBlockOrNull(level: Int): SimpleBlock? {
        var minX = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var minZ = Int.MAX_VALUE
        var maxZ = Int.MIN_VALUE

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
            }
        }

        if (minX == Int.MAX_VALUE) return null
        val minBlock = get(level, minX, minZ)
        val minCopiedLevel = minBlock.level
        val minCopiedX = minBlock.zoneX
        val minCopiedZ = minBlock.zoneZ
        for (zoneX in minX..maxX) {
            for (zoneZ in minZ..maxZ) {
                val zone = get(level, zoneX, zoneZ)
                if (zone.mapsquareId == 32767) {
                    return null
                }
                // Also verify that we're copying with a fixed offset
                if (zone.level != minCopiedLevel) {
                    return null
                }
                val dx = zoneX - minX
                if (zone.zoneX != minCopiedX + dx) {
                    return null
                }
                val dz = zoneZ - minZ
                if (zone.zoneZ != minCopiedZ + dz) {
                    return null
                }
            }
        }

        return SimpleBlock(level, minX, maxX, minZ, maxZ)
    }

    public data class SimpleBlock(
        val level: Int,
        val minX: Int,
        val maxX: Int,
        val minZ: Int,
        val maxZ: Int,
    )

    private companion object {
        private val INVALID = RebuildRegionZone(-1)
    }
}
