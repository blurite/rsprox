package net.rsprox.protocol.session

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfoDecoder
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfoDecoder
import net.rsprox.protocol.reflection.ReflectionCheck
import net.rsprox.protocol.world.World

private var Session.reflectionCheckMap: MutableMap<Int, List<ReflectionCheck>>? by attribute()
private var Session.trackedWorldMap: MutableMap<Int, World>? by attribute()
private var Session.currentActiveWorld: Int? by attribute()
private var Session.currentActiveLevel: Int? by attribute()
private var Session.activeNpcInfoBaseCoord: CoordGrid? by attribute()
private var Session.bytesInPacketGroup: Int? by attribute()
private var Session.bytesConsumedInPacketGroup: Int? by attribute()

public fun Session.getReflectionChecks(): MutableMap<Int, List<ReflectionCheck>> {
    val existingChecks = this.reflectionCheckMap
    if (existingChecks != null) {
        return existingChecks
    }
    val checks = mutableMapOf<Int, List<ReflectionCheck>>()
    this.reflectionCheckMap = checks
    return checks
}

public fun Session.getWorld(index: Int): World {
    return checkNotNull(trackedWorldMap)
        .getValue(index)
}

public fun Session.allocateWorld(
    worldIndex: Int,
    playerInfoDecoder: PlayerInfoDecoder,
    npcInfoDecoder: NpcInfoDecoder,
    sizeX: Int = 16384,
    sizeZ: Int = 16384,
): World {
    var worldMap = this.trackedWorldMap
    if (worldMap == null) {
        worldMap = mutableMapOf()
        this.trackedWorldMap = worldMap
    }
    val world = World(npcInfoDecoder, playerInfoDecoder, sizeX, sizeZ)
    worldMap[worldIndex] = world
    return world
}

public fun Session.getActiveWorld(): Int {
    return currentActiveWorld ?: -1
}

public fun Session.getActiveLevel(): Int {
    return currentActiveLevel ?: 0
}

public fun Session.setActiveWorld(
    id: Int,
    level: Int,
) {
    currentActiveWorld = id
    currentActiveLevel = level
}

public fun Session.getNpcInfoBaseCoord(): CoordGrid {
    return this.activeNpcInfoBaseCoord ?: error("Npc info base coord not set!")
}

public fun Session.setNpcInfoBaseCoord(coordGrid: CoordGrid) {
    this.activeNpcInfoBaseCoord = coordGrid
}

public fun Session.getRemainingBytesInPacketGroup(): Int? {
    return this.bytesInPacketGroup
}

public fun Session.setRemainingBytesInPacketGroup(num: Int?) {
    this.bytesInPacketGroup = num
}

public fun Session.getBytesConsumed(): Int? {
    return this.bytesConsumedInPacketGroup
}

public fun Session.setBytesConsumed(num: Int?) {
    this.bytesConsumedInPacketGroup = num
}
