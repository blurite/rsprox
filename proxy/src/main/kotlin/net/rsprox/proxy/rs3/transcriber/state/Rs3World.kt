package net.rsprox.proxy.rs3.transcriber.state

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildRegion

public class Rs3World {
    private val npcs: MutableMap<Int, Rs3Npc> = mutableMapOf()

    // A scene can extend outside the packed CoordGrid range (e.g. a centre
    // chunk below 16). Keep its signed origin until resolving an actual tile.
    private var sceneOrigin: SceneOrigin? = null
    private var activeZoneSouthWestCoord: ZoneOrigin? = null
    private var sceneBounds: Rs3SceneBounds? = null
    private var instanceMap: Rs3InstanceMap? = null

    internal val instanceBounds: Rs3SceneBounds?
        get() = instanceMap?.bounds

    private data class ZoneOrigin(
        val level: Int,
        val x: Int,
        val z: Int,
    )

    private data class SceneOrigin(
        val x: Int,
        val z: Int,
    )

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

    public fun rebuild(
        baseTileX: Int,
        baseTileZ: Int,
    ) {
        sceneBounds = null
        instanceMap = null
        sceneOrigin = SceneOrigin(baseTileX, baseTileZ)
        activeZoneSouthWestCoord = null
    }

    public fun rebuild(message: RebuildNormal) {
        rebuild(message.baseTileX, message.baseTileZ)
        // Native packed-coordinate -1 unpacks to zero X/Z bounds.
        val minimum = if (message.minimumCoordinate == -1) CoordGrid(0) else CoordGrid(message.minimumCoordinate)
        val maximum = if (message.maximumCoordinate == -1) CoordGrid(0) else CoordGrid(message.maximumCoordinate)
        sceneBounds = Rs3SceneBounds(minimum.x shr 6, minimum.z shr 6, maximum.x shr 6, maximum.z shr 6)
    }

    public fun rebuild(message: RebuildRegion) {
        require(message.mode in 1..4)
        // Native modes 2/4 update the selected scene without changing its bounds.
        // Modes 1/3 (or no previous scene) construct a new terrain scene.
        val previous = if (message.playerInfoInit == null) sceneBounds else null
        val bounds =
            if (message.mode % 2 == 0 && previous != null) {
                previous
            } else {
                val x = message.regionOriginX shr 3
                val z = message.regionOriginZ shr 3
                Rs3SceneBounds(x, z, x + (message.rows shr 3), z + (message.columns shr 3))
            }
        val map = Rs3InstanceMap(bounds, message.rows, message.columns, message.templates)
        rebuild(message.baseTileX, message.baseTileZ)
        sceneBounds = bounds
        instanceMap = map
    }

    /** Presentation only: never replace player/NPC or decoder state with source-map positions. */
    public fun instanceCoord(coord: CoordGrid): CoordGrid? = instanceMap?.tile(coord)

    internal fun instanceFineCoord(
        level: Int,
        x: Int,
        z: Int,
        unitsPerTile: Int,
    ): Rs3InstanceMap.Point? = instanceMap?.point(level, x, z, unitsPerTile)

    public fun setActiveZone(
        zoneX: Int,
        zoneZ: Int,
        level: Int,
    ) {
        this.activeZoneSouthWestCoord = ZoneOrigin(level, zoneX * 8, zoneZ * 8)
    }

    public fun relativizeZoneCoord(
        xInZone: Int,
        zInZone: Int,
        level: Int = -1,
    ): CoordGrid {
        val zone = activeZoneSouthWestCoord ?: return CoordGrid.INVALID
        val origin = sceneOrigin ?: return CoordGrid.INVALID
        val resolvedLevel = if (level == -1) zone.level else level
        val x = origin.x + zone.x + xInZone
        val z = origin.z + zone.z + zInZone
        if (resolvedLevel !in 0..3 || x !in 0..16383 || z !in 0..16383) return CoordGrid.INVALID
        return CoordGrid(resolvedLevel, x, z)
    }
}
