package net.rsprox.proxy.js5

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.proxy.worlds.World

/**
 * A global storage for Js5 master index files.
 * As the connections between JS5 and login are spread apart, we cannot simply
 * store the JS5 master index info within the connection itself.
 * JS5 master index is always initialized when the client boots up. Furthermore,
 * if login fails due to a CRC mismatch, the client is re-initialized, in which
 * case the master index previously stored will also be overridden to the new value.
 * This ensures the master index is always up-to-date as long as the login succeeds.
 */
public data object Js5MasterIndexArchive {
    private val logger = InlineLogger()
    private val archive: MutableMap<WorldUid, ByteArray> = mutableMapOf()

    public fun getJs5MasterIndex(
        port: Int,
        world: World,
    ): ByteArray? {
        if (archive.isEmpty()) {
            return null
        }
        val uid = getWorldUid(port, world)
        val existing = archive[uid]
        if (existing != null) {
            return existing
        }
        val distinct =
            archive
                .entries
                .filterNot { it.key.port != port }
                .map { it.value }
                .distinct()
        if (distinct.size != 1) {
            return null
        }
        return distinct.single()
    }

    public fun setJs5MasterIndex(
        port: Int,
        world: World,
        value: ByteArray,
    ) {
        val uid = getWorldUid(port, world)
        val old = archive.put(uid, value)
        if (!old.contentEquals(value)) {
            logger.debug { "Updating JS5 master index for world $uid" }
        }
    }

    private fun getWorldUid(
        port: Int,
        world: World,
    ): WorldUid {
        return WorldUid(
            port,
            world.id,
            world.properties,
            world.host,
        )
    }

    private data class WorldUid(
        val port: Int,
        val id: Int,
        val properties: Int,
        val host: String,
    )
}
