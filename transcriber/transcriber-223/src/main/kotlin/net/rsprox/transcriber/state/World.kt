package net.rsprox.transcriber.state

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityMoveSpeed

public class World(
    public val id: Int,
) {
    private var buildAreaSouthWestCoord: CoordGrid = CoordGrid.INVALID
    private var activeZoneSouthWestCoord: CoordGrid = CoordGrid.INVALID
    public var sizeX: Int = 800
    public var sizeZ: Int = 2048
    public var angle: Int = 0
    public var unknownProperty: Int = -1
    public var coord: CoordGrid = CoordGrid.INVALID
    public var moveSpeed: WorldEntityMoveSpeed = WorldEntityMoveSpeed.ZERO

    public fun rebuild(southWestCoord: CoordGrid) {
        this.buildAreaSouthWestCoord = southWestCoord
    }

    public fun activeLevel(): Int {
        return this.activeZoneSouthWestCoord.level
    }

    public fun setActiveZone(
        xInBuildArea: Int,
        zInBuildArea: Int,
        level: Int,
    ) {
        this.activeZoneSouthWestCoord = CoordGrid(level, xInBuildArea, zInBuildArea)
    }

    public fun relativizeZoneCoord(
        xInZone: Int,
        zInZone: Int,
        level: Int = -1,
    ): CoordGrid {
        return CoordGrid(
            if (level == -1) this.activeZoneSouthWestCoord.level else level,
            buildAreaSouthWestCoord.x + activeZoneSouthWestCoord.x + xInZone,
            buildAreaSouthWestCoord.z + activeZoneSouthWestCoord.z + zInZone,
        )
    }

    public fun relativizeBuildAreaCoord(
        xInBuildArea: Int,
        zInBuildArea: Int,
        level: Int,
    ): CoordGrid {
        return CoordGrid(
            level,
            buildAreaSouthWestCoord.x + xInBuildArea,
            buildAreaSouthWestCoord.z + zInBuildArea,
        )
    }
}
