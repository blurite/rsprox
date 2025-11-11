package net.rsprox.processor.state

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.map.RebuildLogin
import net.rsprox.protocol.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.game.outgoing.model.map.RebuildRegion
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea
import net.rsprox.protocol.game.outgoing.model.misc.client.ServerTickEnd
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialFollows

public data class BinarySessionState(
    public val currentTick: Int,
    public val localPlayerIndex: Int,
    public val buildArea: CoordGrid,
    public val updateZone: CoordGrid,
    public val regionZones: BuildArea?,
) {
    public fun absCoord(xInZone: Int, zInZone: Int): CoordGrid {
        val zoneBase = CoordGrid(updateZone.level, buildArea.x + updateZone.x, buildArea.z + updateZone.z)
        val coord = CoordGrid(zoneBase.level, zoneBase.x + xInZone, zoneBase.z + zInZone)
        return absCoord(buildArea, regionZones, coord)
    }

    internal fun nextState(message: IncomingMessage): BinarySessionState {
        when (message) {
            is ServerTickEnd -> {
                return copy(currentTick = currentTick + 1)
            }
            is RebuildNormal -> {
                val buildAreaX = (message.zoneX - 6) shl 3
                val buildAreaZ = (message.zoneZ - 6) shl 3
                val buildArea = CoordGrid(0, buildAreaX, buildAreaZ)
                return copy(buildArea = buildArea, regionZones = null)
            }
            is RebuildRegion -> {
                val buildAreaX = (message.zoneX - 6) shl 3
                val buildAreaZ = (message.zoneZ - 6) shl 3
                val buildArea = CoordGrid(0, buildAreaX, buildAreaZ)
                return copy(buildArea = buildArea, regionZones = message.buildArea)
            }
            is RebuildLogin -> {
                val localIndex = message.playerInfoInitBlock.localPlayerIndex
                val buildAreaX = (message.zoneX - 6) shl 3
                val buildAreaZ = (message.zoneZ - 6) shl 3
                val buildArea = CoordGrid(0, buildAreaX, buildAreaZ)
                return copy(localPlayerIndex = localIndex, buildArea = buildArea, regionZones = null)
            }
            is UpdateZoneFullFollows -> {
                val updateZone = CoordGrid(message.level, message.zoneX, message.zoneZ)
                return copy(updateZone = updateZone)
            }
            is UpdateZonePartialEnclosed -> {
                val updateZone = CoordGrid(message.level, message.zoneX, message.zoneZ)
                return copy(updateZone = updateZone)
            }
            is UpdateZonePartialFollows -> {
                val updateZone = CoordGrid(message.level, message.zoneX, message.zoneZ)
                return copy(updateZone = updateZone)
            }
        }
        return this
    }

    public companion object {
        public val DEFAULT: BinarySessionState =
            BinarySessionState(
                currentTick = 0,
                localPlayerIndex = -1,
                buildArea = CoordGrid.INVALID,
                updateZone = CoordGrid.INVALID,
                regionZones = null,
            )

        private fun absCoord(buildArea: CoordGrid, regionZones: BuildArea?, coord: CoordGrid): CoordGrid {
            if (regionZones == null || coord.x < 6400) {
                return coord
            }
            val localX = coord.x - buildArea.x
            val localZ = coord.z - buildArea.z
            if (localX < 0 || localX >= 104 || localZ < 0 || localZ >= 104) {
                return CoordGrid.INVALID
            }
            val localZoneX = localX ushr 3
            val localZoneZ = localZ ushr 3
            val template = regionZones[coord.level, localZoneX, localZoneZ]
            if (template.invalid) {
                return CoordGrid.INVALID
            }
            val copiedRotation = template.rotation
            val copiedZoneX = template.zoneX
            val copiedZoneZ = template.zoneZ
            val copiedLevel = template.level
            val unrotatedX = (copiedZoneX shl 3) or (localX and 0x7)
            val unrotatedZ = (copiedZoneZ shl 3) or (localZ and 0x7)
            return rotate(CoordGrid(copiedLevel, unrotatedX, unrotatedZ), copiedRotation)
        }

        private fun rotate(
            coordGrid: CoordGrid,
            rotation: Int,
        ): CoordGrid {
            val zoneAbsX = coordGrid.x and 0x7.inv()
            val zoneAbsZ = coordGrid.z and 0x7.inv()
            val xInZone = coordGrid.x and 0x7
            val zInZone = coordGrid.z and 0x7
            return when (rotation) {
                1 -> CoordGrid(coordGrid.level, zoneAbsX + zInZone, zoneAbsZ + (7 - xInZone))
                2 -> CoordGrid(coordGrid.level, zoneAbsX + (7 - xInZone), zoneAbsZ + (7 - zInZone))
                3 -> CoordGrid(coordGrid.level, zoneAbsX + (7 - zInZone), zoneAbsZ + xInZone)
                else -> coordGrid
            }
        }
    }
}
