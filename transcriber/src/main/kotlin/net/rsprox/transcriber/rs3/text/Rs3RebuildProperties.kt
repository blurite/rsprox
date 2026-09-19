package net.rsprox.transcriber.rs3.text

import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildRegion
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.any
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.zoneCoordGrid
import net.rsprox.transcriber.rs3.state.Rs3SceneBounds

internal fun Property.appendBuildArea(
    message: RebuildRegion,
    bounds: Rs3SceneBounds?,
    collapse: Boolean,
) {
    group("BUILD_AREA") {
        for ((level, plane) in message.templates.withIndex()) {
            val rectangle = if (collapse) plane.simpleRectangle() else null
            if (rectangle != null) {
                group {
                    templateSource("minsource", plane[rectangle.minX][rectangle.minZ])
                    templateDestination("mindest", bounds, level, rectangle.minX, rectangle.minZ)
                    int("widthinzones", rectangle.maxX - rectangle.minX + 1)
                    int("lengthinzones", rectangle.maxZ - rectangle.minZ + 1)
                    int("rotation", 0)
                    filteredInt("firstbit", plane[rectangle.minX][rectangle.minZ] and 1, 0)
                }
            } else {
                for ((x, row) in plane.withIndex()) {
                    for ((z, template) in row.withIndex()) {
                        if (template == -1) continue
                        group {
                            templateSource("source", template)
                            templateDestination("dest", bounds, level, x, z)
                            int("rotation", template ushr 1 and 3)
                            filteredInt("firstbit", template and 1, 0)
                        }
                    }
                }
            }
        }
    }
}

private fun Property.templateSource(
    name: String,
    template: Int,
) {
    zoneCoordGrid(
        template ushr 24 and 3,
        (template ushr 14 and 0x3ff) shl 3,
        (template ushr 3 and 0x7ff) shl 3,
        name,
    )
}

private fun Property.templateDestination(
    name: String,
    bounds: Rs3SceneBounds?,
    level: Int,
    x: Int,
    z: Int,
) {
    // Use retained terrain bounds, not the network window or the latest wire origin.
    // Rebuild mappings must bypass instance-to-source coordinate translation.
    if (bounds != null) {
        zoneCoordGrid(level, (bounds.minX shl 6) + (x shl 3), (bounds.minZ shl 6) + (z shl 3), name)
    } else {
        group(name) {
            int("level", level)
            int("xoffsetinzones", x)
            int("zoffsetinzones", z)
            any("origin", "unknown")
        }
    }
}

private data class SimpleRectangle(
    val minX: Int,
    val minZ: Int,
    val maxX: Int,
    val maxZ: Int,
)

/** OSRS-style collapse: one complete, unrotated, consistently translated rectangle per plane. */
private fun List<List<Int>>.simpleRectangle(): SimpleRectangle? {
    var minX = Int.MAX_VALUE
    var minZ = Int.MAX_VALUE
    var maxX = -1
    var maxZ = -1
    for ((x, row) in withIndex()) {
        for ((z, template) in row.withIndex()) {
            if (template == -1) continue
            if (template ushr 1 and 3 != 0) return null
            minX = minOf(minX, x)
            minZ = minOf(minZ, z)
            maxX = maxOf(maxX, x)
            maxZ = maxOf(maxZ, z)
        }
    }
    if (maxX == -1) return null
    val first = this[minX][minZ]
    if (first == -1) return null
    val sourceX = first ushr 14 and 0x3ff
    val sourceZ = first ushr 3 and 0x7ff
    for (x in minX..maxX) {
        for (z in minZ..maxZ) {
            val template = this[x][z]
            if (template == -1 || template ushr 24 != first ushr 24 || template and 1 != first and 1) {
                return null
            }
            if (
                template ushr 14 and 0x3ff != sourceX + x - minX ||
                template ushr 3 and 0x7ff != sourceZ + z - minZ
            ) {
                return null
            }
        }
    }
    return SimpleRectangle(minX, minZ, maxX, maxZ)
}
