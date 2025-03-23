@file:Suppress("DuplicatedCode")

package net.rsprox.transcriber.state

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.crypto.xtea.XteaKey
import net.rsprox.protocol.game.outgoing.model.map.RebuildRegion
import net.rsprox.protocol.game.outgoing.model.map.StaticRebuildMessage
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea

public class KeyStorage {
    private val keys: MutableMap<Int, IntArray> = mutableMapOf()

    private fun add(
        mapsquareId: Int,
        keys: IntArray,
    ) {
        // Don't bother storing 0,0,0,0 keys - that's only possible if there's no map.
        val isZero = keys.all { it == 0 }
        if (isZero) return
        val old = this.keys.put(mapsquareId, keys)
        if (old != null && !old.contentEquals(keys)) {
            logger.warn {
                "Duplicate mapsquare keys in the same login session: " +
                    "$mapsquareId, ${old.contentToString()}, ${keys.contentToString()}"
            }
        }
    }

    public fun onStaticRebuild(message: StaticRebuildMessage) {
        val minMapsquareX = (message.zoneX - 6) ushr 3
        val maxMapsquareX = (message.zoneX + 6) ushr 3
        val minMapsquareZ = (message.zoneZ - 6) ushr 3
        val maxMapsquareZ = (message.zoneZ + 6) ushr 3
        val iterator = message.keys.listIterator()
        for (mapsquareX in minMapsquareX..maxMapsquareX) {
            for (mapsquareZ in minMapsquareZ..maxMapsquareZ) {
                val mapsquareId = (mapsquareX shl 8) or mapsquareZ
                val key = iterator.next()
                add(mapsquareId, key.key)
            }
        }
    }

    public fun onRebuildRegion(message: RebuildRegion) {
        val mapsquares = mutableSetOf<Int>()
        val startZoneX = message.zoneX - 6
        val startZoneZ = message.zoneZ - 6
        for (level in 0..<4) {
            for (zoneX in startZoneX..(message.zoneX + 6)) {
                for (zoneZ in startZoneZ..(message.zoneZ + 6)) {
                    val block = message.buildArea[level, zoneX - startZoneX, zoneZ - startZoneZ]
                    // Invalid zone
                    if (block.mapsquareId == 32767) continue
                    mapsquares += block.mapsquareId
                }
            }
        }
        val iterator = message.keys.listIterator()
        for (mapsquareId in mapsquares) {
            val key = iterator.next()
            add(mapsquareId, key.key)
        }
    }

    public fun onRebuildWorldEntity(
        sizeX: Int,
        sizeZ: Int,
        buildArea: BuildArea,
        keys: List<XteaKey>,
    ) {
        val mapsquares = mutableSetOf<Int>()
        for (level in 0..<4) {
            for (x in 0..<(sizeX / 8)) {
                for (z in 0..<(sizeZ / 8)) {
                    val block = buildArea[level, x, z]
                    // Invalid zone
                    if (block.mapsquareId == 32767) continue
                    mapsquares += block.mapsquareId
                }
            }
        }
        val iterator = keys.listIterator()
        for (mapsquareId in mapsquares) {
            val key = iterator.next()
            add(mapsquareId, key.key)
        }
    }

    public fun get(): Map<Int, IntArray> {
        return keys
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
