package net.rsprox.protocol.session

import net.rsprot.compression.HuffmanCodec
import net.rsprox.protocol.reflection.ReflectionCheck
import net.rsprox.protocol.world.World

private var Session.reflectionCheckMap: MutableMap<Int, List<ReflectionCheck>>? by attribute()
private var Session.trackedWorldMap: MutableMap<Int, World>? by attribute()
private var Session.currentActiveWorld: Int? by attribute()

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
    localPlayerIndex: Int,
    huffmanCodec: HuffmanCodec,
): World {
    var worldMap = this.trackedWorldMap
    if (worldMap == null) {
        worldMap = mutableMapOf()
        this.trackedWorldMap = worldMap
    }
    val world = World(localPlayerIndex, huffmanCodec)
    worldMap[worldIndex] = world
    return world
}

internal fun Session.getActiveWorld(): Int {
    return currentActiveWorld ?: -1
}
