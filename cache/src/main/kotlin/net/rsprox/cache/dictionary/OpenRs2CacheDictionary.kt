package net.rsprox.cache.dictionary

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.dictionary.openrs2.CacheGame
import net.rsprox.cache.dictionary.openrs2.CacheLanguage
import net.rsprox.cache.dictionary.openrs2.CacheScope
import net.rsprox.cache.util.downloadCacheListings

public class OpenRs2CacheDictionary {
    private lateinit var dictionary: List<Entry>

    private fun tryLoad() {
        check(!this::dictionary.isInitialized) {
            "Dictionary already initialized."
        }
        try {
            load()
        } catch (e: Exception) {
            logger.error(e) {
                "Unable to load the dictionary from OpenRS2"
            }
        }
        if (!this::dictionary.isInitialized) {
            // Set an empty dictionary, so we don't attempt to re-load it
            // This should only happen if OpenRS2 archive is down, or we failed to deserialize results
            this.dictionary = emptyList()
        }
    }

    private fun load() {
        this.dictionary = downloadCacheListings()
    }

    public fun list(
        scope: CacheScope,
        game: CacheGame,
        language: CacheLanguage,
        revision: Int,
    ): List<Entry> {
        if (!this::dictionary.isInitialized) {
            tryLoad()
        }
        return this.dictionary
            .asSequence()
            .filter { it.scope == scope.label }
            .filter { it.game == game.label }
            .filter { it.language == language.label }
            .filter { it.builds.any { build -> build.major == revision } }
            .toList()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Entry(
        public val id: Int,
        public val scope: String,
        public val game: String,
        public val environment: String,
        public val language: String,
        public val builds: List<Build>,
        public val timestamp: String?,
        public val sources: List<String>,
        @field:JsonAlias("valid_indexes")
        public val validIndexes: Int?,
        public val indexes: Int?,
        @field:JsonAlias("valid_groups")
        public val validGroups: Int?,
        public val groups: Int?,
        @field:JsonAlias("valid_keys")
        public val validKeys: Int?,
        public val keys: Int?,
        public val size: Long?,
        public val blocks: Int?,
        @field:JsonAlias("disk_store_valid")
        public val diskStoreValid: Boolean?,
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class Build(
            public val major: Int,
            public val minor: Int?,
        )
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
