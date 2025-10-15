package net.rsprox.proxy.target

import net.rsprox.proxy.config.CONFIGURATION_PATH
import kotlin.io.path.exists
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

internal object ProxyTargetSourceRegistry {
    private val sourcesFile: Path = CONFIGURATION_PATH.resolve("proxy-target-sources.properties")

    fun syncFromExistingConfig() {
        val path =
            when {
                PROXY_TARGETS_FILE.exists() -> PROXY_TARGETS_FILE
                ALT_PROXY_TARGETS_FILE.exists() -> ALT_PROXY_TARGETS_FILE
                else -> null
            } ?: return

        val entries = YamlProxyTargetConfig.load(path).entries
        if (entries.isEmpty()) {
            return
        }

        val grouped =
            entries
                .mapNotNull { entry ->
                    val name = entry.name.trim()
                    val url = entry.sourceUrl?.trim()?.ifEmpty { null }
                    if (name.isEmpty() || url == null) {
                        return@mapNotNull null
                    }
                    url to name.lowercase()
                }
                .groupBy({ it.first }) { it.second }

        if (grouped.isEmpty()) {
            return
        }

        val data = load()
        var changed = false

        for ((url, names) in grouped) {
            val normalizedNames = names.toSet()
            val existingKeys = data.filterValues { it == url }.keys.toList()
            for (key in existingKeys) {
                if (key !in normalizedNames) {
                    data.remove(key)
                    changed = true
                }
            }
            for (key in normalizedNames) {
                val previous = data.put(key, url)
                if (previous != url) {
                    changed = true
                }
            }
        }

        if (changed) {
            persist(data)
        }
    }

    fun load(): MutableMap<String, String> {
        if (!sourcesFile.exists()) {
            return mutableMapOf()
        }
        return sourcesFile.inputStream().use { stream ->
            val properties = Properties()
            properties.load(stream)
            val result = mutableMapOf<String, String>()
            for ((key, value) in properties) {
                val name = (key as? String)?.trim().orEmpty()
                val url = (value as? String)?.trim().orEmpty()
                if (name.isNotEmpty() && url.isNotEmpty()) {
                    result[name.lowercase()] = url
                }
            }
            result
        }
    }

    fun replaceForUrl(url: String, names: Collection<String>) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isEmpty()) {
            return
        }

        val data = load()
        val normalizedNames = names.mapNotNull { name ->
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                null
            } else {
                trimmed.lowercase()
            }
        }

        val existingKeys = data.filterValues { it == trimmedUrl }.keys
        for (key in existingKeys) {
            if (key !in normalizedNames) {
                data.remove(key)
            }
        }

        for (key in normalizedNames) {
            data[key] = trimmedUrl
        }

        persist(data)
    }

    fun remove(names: Collection<String>) {
        val data = load()
        var changed = false
        for (name in names) {
            val key = name.trim().lowercase()
            if (data.remove(key) != null) {
                changed = true
            }
        }
        if (changed) {
            persist(data)
        }
    }

    fun entries(): Map<String, String> {
        return load()
    }

    private fun persist(data: Map<String, String>) {
        if (data.isEmpty()) {
            sourcesFile.deleteIfExists()
            return
        }

        val properties = Properties()
        for ((key, value) in data) {
            properties[key] = value
        }
        sourcesFile.parent?.createDirectories()
        sourcesFile.outputStream().use { stream ->
            properties.store(stream, "Proxy target source URLs")
        }
    }
}

