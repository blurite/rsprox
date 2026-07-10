package net.rsprox.cache.dictionary

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.util.downloadRSProxArchiveCacheIndex
import java.security.MessageDigest

internal class RSProxArchiveCacheDictionary {
    @Volatile
    private var index: RSProxArchiveCacheIndex? = null

    fun find(masterIndex: Js5MasterIndex): RSProxArchiveCacheEntry? {
        val sha256 = sha256(masterIndex.data)
        val matches =
            load()
                .caches
                .filter { entry ->
                    entry.game == OLDSCHOOL_GAME &&
                        entry.revision == masterIndex.revision &&
                        entry.masterIndexSha256.equals(sha256, ignoreCase = true)
                }
        check(matches.size <= 1) {
            "RSProx Archive contains multiple Old School caches for revision " +
                "${masterIndex.revision} and master index $sha256"
        }
        return matches.singleOrNull()
    }

    private fun load(): RSProxArchiveCacheIndex {
        val existing = index
        if (existing != null) {
            return existing
        }
        return synchronized(this) {
            index ?: downloadRSProxArchiveCacheIndex().also { loaded ->
                require(loaded.kind == CACHE_INDEX_KIND) {
                    "Unexpected RSProx Archive index kind: ${loaded.kind}"
                }
                index = loaded
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun sha256(data: ByteArray): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(data)
            .toHexString()

    private companion object {
        private const val OLDSCHOOL_GAME: String = "oldschool"
        private const val CACHE_INDEX_KIND: String = "cache-index"
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class RSProxArchiveCacheIndex(
    val kind: String,
    val caches: List<RSProxArchiveCacheEntry>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class RSProxArchiveCacheEntry(
    val game: String,
    val revision: Int,
    val variant: Int,
    val masterIndexSha256: String,
    val cacheZipUrl: String,
    val cacheZipSha256: String,
    val cacheZipSize: Long,
)
