package net.rsprox.proxy.rs3.transcriber.state

import net.rsprox.protocol.common.CoordGrid

public class Rs3World {
    private val npcs: MutableMap<Int, Rs3Npc> = mutableMapOf()

    private var buildAreaSouthWestCoord: CoordGrid = CoordGrid.INVALID
    private var activeZoneSouthWestCoord: CoordGrid = CoordGrid.INVALID

    public fun updateNpc(
        index: Int,
        npc: Rs3Npc,
    ) {
        this.npcs[index] = npc
    }

    public fun removeNpc(index: Int) {
        this.npcs.remove(index)
    }

    public fun getNpc(index: Int): Rs3Npc {
        return checkNotNull(this.npcs[index]) { "No npc tracked at index $index" }
    }

    public fun getNpcOrNull(index: Int): Rs3Npc? {
        return this.npcs[index]
    }

    public fun rebuild(southWestCoord: CoordGrid) {
        this.buildAreaSouthWestCoord = southWestCoord
    }

    public fun setActiveZone(
        zoneX: Int,
        zoneZ: Int,
        level: Int,
    ) {
        this.activeZoneSouthWestCoord = CoordGrid(level, zoneX * 8, zoneZ * 8)
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
}
