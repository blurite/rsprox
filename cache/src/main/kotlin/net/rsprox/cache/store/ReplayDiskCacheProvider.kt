package net.rsprox.cache.store

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.Js5MasterIndex

public class ReplayDiskCacheProvider {
    private val primary = RSProxArchiveDiskCacheProvider()
    private val fallback = OpenRs2DiskCacheProvider()

    public fun get(masterIndex: Js5MasterIndex): ReplayDiskCacheStore? {
        val primaryStore =
            try {
                primary.get(masterIndex)
            } catch (error: Exception) {
                logger.warn(error) {
                    "Unable to resolve replay cache revision ${masterIndex.revision} " +
                        "through RSProx Archive; falling back to OpenRS2"
                }
                null
            }
        if (primaryStore != null) {
            try {
                primaryStore.open()
                return primaryStore
            } catch (error: Exception) {
                runCatching(primaryStore::close)
                logger.warn(error) {
                    "Unable to hydrate replay cache revision ${masterIndex.revision} " +
                        "through RSProx Archive; falling back to OpenRS2"
                }
            }
        } else {
            logger.info {
                "Replay cache revision ${masterIndex.revision} is not present in RSProx Archive; " +
                    "falling back to OpenRS2"
            }
        }

        return try {
            fallback.get(masterIndex)
        } catch (error: Exception) {
            logger.warn(error) {
                "Unable to resolve replay cache revision ${masterIndex.revision} through OpenRS2"
            }
            null
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
