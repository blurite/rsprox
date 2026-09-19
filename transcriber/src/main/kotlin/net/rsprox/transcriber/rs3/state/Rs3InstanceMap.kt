package net.rsprox.transcriber.rs3.state

import net.rsprox.protocol.common.CoordGrid

/** Native terrain bounds are inclusive map-square indices, not the network window. */
internal data class Rs3SceneBounds(
    val minX: Int,
    val minZ: Int,
    val maxX: Int,
    val maxZ: Int,
)

/** Immutable snapshot of a four-plane, variable-size REBUILD_REGION template grid. */
internal class Rs3InstanceMap(
    val bounds: Rs3SceneBounds,
    private val rows: Int,
    private val columns: Int,
    templates: List<List<List<Int>>>,
) {
    private val cells: IntArray

    init {
        require(rows in 0..255 && columns in 0..255)
        require(
            templates.size == 4 &&
                templates.all { plane ->
                    plane.size == rows && plane.all { it.size == columns }
                },
        ) { "Invalid instance template dimensions" }
        cells =
            IntArray(4 * rows * columns) { index ->
                templates[index / (rows * columns)][index / columns % rows][index % columns]
            }
        require(cells.all { it == -1 || it in 0 until (1 shl 26) }) { "Invalid instance template" }
    }

    internal data class Point(
        val level: Int,
        val x: Int,
        val z: Int,
    )

    fun tile(coord: CoordGrid): CoordGrid? {
        if (coord == CoordGrid.INVALID || coord.level !in 0..3) return null
        // Tile coordinates identify cell centres, not the grid vertices.
        val point = point(coord.level, coord.x * 2 + 1, coord.z * 2 + 1, 2) ?: return null
        return CoordGrid(point.level, point.x / 2, point.z / 2)
    }

    /** Coordinates are absolute fine units, with the caller supplying units per tile. */
    fun point(
        level: Int,
        x: Int,
        z: Int,
        unitsPerTile: Int,
    ): Point? {
        require(unitsPerTile in 1..1024)
        if (level !in 0..3 || x !in 0 until 16384 * unitsPerTile || z !in 0 until 16384 * unitsPerTile) {
            return null
        }
        val tileX = x / unitsPerTile
        val tileZ = z / unitsPerTile
        if ((tileX shr 6) !in bounds.minX..bounds.maxX || (tileZ shr 6) !in bounds.minZ..bounds.maxZ) {
            return null
        }
        val row = (tileX shr 3) - (bounds.minX shl 3)
        val column = (tileZ shr 3) - (bounds.minZ shl 3)
        if (row !in 0 until rows || column !in 0 until columns) return null
        val template = cells[(level * rows + row) * columns + column]
        if (template == -1) return null

        val edge = 8 * unitsPerTile
        val localX = x % edge
        val localZ = z % edge
        // Inverse of native source->destination rotation at Ghidra 0x006d8c4c.
        val rotation = template ushr 1 and 3
        val sourceX =
            when (rotation) {
                1 -> edge - localZ
                2 -> edge - localX
                3 -> localZ
                else -> localX
            }
        val sourceZ =
            when (rotation) {
                1 -> localX
                2 -> edge - localZ
                3 -> edge - localX
                else -> localZ
            }
        val resultX = (template ushr 14 and 0x3ff) * edge + sourceX
        val resultZ = (template ushr 3 and 0x7ff) * edge + sourceZ
        if (resultX !in 0 until 16384 * unitsPerTile || resultZ !in 0 until 16384 * unitsPerTile) return null
        return Point(template ushr 24 and 3, resultX, resultZ)
    }
}
