package net.rsprox.transcriber.state

import net.rsprox.protocol.common.CoordFine
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore

public class World(
    public val index: Int,
    private val settingSetStore: SettingSetStore,
) {
    private var buildAreaSouthWestCoord: CoordGrid = CoordGrid.INVALID
    private var activeZoneSouthWestCoord: CoordGrid = CoordGrid.INVALID
    public var sizeX: Int = 800
    public var sizeZ: Int = 2048
    public var angle: Int = 0
    public var level: Int = 0
    public var id: Int = -1
    public var priority: Int = -1
    public var centerFineOffsetX: Int? = null
    public var centerFineOffsetZ: Int? = null
    public var coordFine: CoordFine = CoordFine.INVALID
    public var coord: CoordGrid = CoordGrid.INVALID
    private val npcs: MutableMap<Int, Npc> = mutableMapOf()
    private var rebuildRegionBuildArea: BuildArea? = null

    private val settings: SettingSet
        get() = settingSetStore.getActive()

    public fun setBuildArea(buildArea: BuildArea?) {
        this.rebuildRegionBuildArea = buildArea
    }

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

    public fun getInstancedCoordOrSelf(coordGrid: CoordGrid): CoordGrid {
        return instanceCoord(coordGrid) ?: coordGrid
    }

    public fun instanceCoord(coordGrid: CoordGrid): CoordGrid? {
        val buildArea = this.rebuildRegionBuildArea
        if (buildArea == null || coordGrid.x < 6400 || !settings[Setting.TRANSLATE_INSTANCED_COORDS]) {
            return null
        }
        val localX = coordGrid.x - buildAreaSouthWestCoord.x
        val localZ = coordGrid.z - buildAreaSouthWestCoord.z
        if (localX < 0 || localX >= 104 || localZ < 0 || localZ >= 104) {
            return null
        }
        val localZoneX = localX ushr 3
        val localZoneZ = localZ ushr 3
        val template = buildArea[coordGrid.level, localZoneX, localZoneZ]
        if (template.invalid) return null
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

    public fun getNpc(index: Int): Npc {
        return npcs[index] ?: error("Npc $index does not exist in world ${this.index}")
    }

    public fun getNpcOrNull(index: Int): Npc? {
        return npcs[index]
    }

    public fun createNpc(
        index: Int,
        id: Int,
        name: String?,
        creationCycle: Int,
        coordGrid: CoordGrid,
    ): Npc {
        val new = Npc(index, id, creationCycle, coordGrid, name)
        val old = this.npcs.put(index, new)
        check(old == null) {
            "Overriding npc $index"
        }
        return new
    }

    public fun updateNpc(
        index: Int,
        coordGrid: CoordGrid,
    ) {
        val old = getNpc(index)
        this.npcs[index] = Npc(old.index, old.id, old.creationCycle, coordGrid, old.name)
    }

    public fun updateNpcName(
        index: Int,
        newId: Int,
        newName: String?,
    ) {
        val old = getNpc(index)
        this.npcs[index] = Npc(old.index, newId, old.creationCycle, old.coord, newName)
    }

    public fun removeNpc(index: Int) {
        val old = this.npcs.remove(index)
        check(old != null) {
            "Npc $index does not exist."
        }
    }
}
