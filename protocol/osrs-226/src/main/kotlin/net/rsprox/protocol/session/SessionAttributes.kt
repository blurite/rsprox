package net.rsprox.protocol.session

import net.rsprot.compression.HuffmanCodec
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfoClient
import net.rsprox.protocol.reflection.ReflectionCheck
import net.rsprox.protocol.world.World

private var Session.reflectionCheckMap: MutableMap<Int, List<ReflectionCheck>>? by attribute()
private var Session.trackedWorldMap: MutableMap<Int, World>? by attribute()
private var Session.currentActiveWorld: Int? by attribute()
private var Session.activeNpcInfoBaseCoord: CoordGrid? by attribute()
private var Session.rootPlayerInfoClient: PlayerInfoClient? by attribute()

internal fun Session.getReflectionChecks(): MutableMap<Int, List<ReflectionCheck>> {
    val existingChecks = this.reflectionCheckMap
    if (existingChecks != null) {
        return existingChecks
    }
    val checks = mutableMapOf<Int, List<ReflectionCheck>>()
    this.reflectionCheckMap = checks
    return checks
}

internal fun Session.getWorld(index: Int): World {
    return checkNotNull(trackedWorldMap)
        .getValue(index)
}

internal fun Session.allocateWorld(
    worldIndex: Int,
    cache: CacheProvider,
): World {
    var worldMap = this.trackedWorldMap
    if (worldMap == null) {
        worldMap = mutableMapOf()
        this.trackedWorldMap = worldMap
    }
    val world = World(cache)
    worldMap[worldIndex] = world
    return world
}

internal fun Session.allocatePlayerInfoClient(
    localPlayerIndex: Int,
    huffmanCodec: HuffmanCodec,
): PlayerInfoClient {
    val client = PlayerInfoClient(localPlayerIndex, huffmanCodec)
    this.rootPlayerInfoClient = client
    return client
}

internal fun Session.getPlayerInfoClient(): PlayerInfoClient {
    return checkNotNull(rootPlayerInfoClient)
}

internal fun Session.getActiveWorld(): Int {
    return currentActiveWorld ?: -1
}

internal fun Session.getNpcInfoBaseCoord(): CoordGrid {
    return this.activeNpcInfoBaseCoord ?: error("Npc info base coord not set!")
}

internal fun Session.setNpcInfoBaseCoord(coordGrid: CoordGrid) {
    this.activeNpcInfoBaseCoord = coordGrid
}
