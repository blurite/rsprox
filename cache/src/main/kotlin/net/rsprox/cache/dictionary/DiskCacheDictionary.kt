package net.rsprox.cache.dictionary

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.util.atomicWrite
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.exists

@Suppress("MemberVisibilityCanBePrivate")
public class DiskCacheDictionary private constructor(
    private val dictionaryPath: Path,
    private val cachePath: Path,
    private val dictionary: MutableMap<Js5MasterIndex, Entry> = mutableMapOf(),
) : AutoCloseable {
    public fun getEntry(masterIndex: Js5MasterIndex): Entry? {
        return dictionary[masterIndex]
    }

    public fun getDirectory(masterIndex: Js5MasterIndex): Path? {
        val entry = getEntry(masterIndex) ?: return null
        return cachePath.resolve(entry.directory)
    }

    public fun exists(masterIndex: Js5MasterIndex): Boolean {
        return masterIndex in dictionary
    }

    public fun store(
        masterIndex: Js5MasterIndex,
        openrs2Id: Int? = null,
    ): Path {
        if (masterIndex in dictionary) {
            throw IllegalArgumentException("Dictionary entry for $masterIndex already exists.")
        }
        val directory = findUnusedDirectory(masterIndex)
        val entry = Entry(masterIndex, directory, openrs2Id)
        this.dictionary[masterIndex] = entry
        return cachePath.resolve(directory)
    }

    public fun getOrPut(
        masterIndex: Js5MasterIndex,
        openrs2Id: Int?,
    ): Path {
        val existing = dictionary[masterIndex]
        if (existing != null) {
            return cachePath.resolve(existing.directory)
        }
        return store(masterIndex, openrs2Id)
    }

    private fun findUnusedDirectory(masterIndex: Js5MasterIndex): String {
        val shortHash = masterIndex.shortHash()
        val parent = cachePath
        val baseDirectory = parent.resolve(shortHash)
        if (!baseDirectory.exists(LinkOption.NOFOLLOW_LINKS)) {
            return shortHash
        }
        for (i in 0..<256) {
            val name = "$shortHash-$i"
            val alternativeDirectory = parent.resolve(name)
            if (!alternativeDirectory.exists(LinkOption.NOFOLLOW_LINKS)) {
                return name
            }
        }
        throw IllegalArgumentException("Master index space saturated.")
    }

    public fun write() {
        val text =
            try {
                mapper.writeValueAsString(this.dictionary.values)
            } catch (e: Exception) {
                logger.error(e) {
                    "Unable to serialize ${this.dictionary}"
                }
                return
            }
        dictionaryPath.atomicWrite(text)
    }

    override fun close() {
        write()
    }

    public data class Entry(
        public val js5MasterIndex: Js5MasterIndex,
        public val directory: String,
        public val openrs2Id: Int?,
    )

    public companion object {
        private val logger = InlineLogger()
        private val mapper = jacksonObjectMapper()

        public fun create(
            dictionaryPath: Path,
            cachePath: Path,
        ): DiskCacheDictionary {
            if (!dictionaryPath.exists(LinkOption.NOFOLLOW_LINKS)) {
                return DiskCacheDictionary(dictionaryPath, cachePath)
            }
            val dictionary =
                try {
                    mapper.readValue<List<Entry>>(dictionaryPath.toFile())
                } catch (e: Exception) {
                    logger.warn(e) { "Unable to deserialize existing cache dictionary" }
                    return DiskCacheDictionary(dictionaryPath, cachePath)
                }
            val map = dictionary.associateByTo(LinkedHashMap()) { it.js5MasterIndex }
            return DiskCacheDictionary(dictionaryPath, cachePath, map)
        }
    }
}
