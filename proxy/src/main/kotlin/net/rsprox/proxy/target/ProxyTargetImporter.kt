package net.rsprox.proxy.target

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.net.URL
import java.nio.file.Path
import java.util.LinkedHashMap
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.extension

public class ProxyTargetImporter {
    private val objectMapper =
        ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .findAndRegisterModules()

    public fun import(source: Path): ProxyTargetImportResult {
        require(source.exists()) {
            "Proxy target configuration '$source' does not exist."
        }
        val extension = source.extension.lowercase()
        require(extension == "yaml" || extension == "yml") {
            "Unsupported file extension: .$extension"
        }

        val imported = YamlProxyTargetConfig.load(source)
        return merge(imported)
    }

    public fun import(source: URL): ProxyTargetImportResult {
        val extension = source.path.substringAfterLast('.', "").lowercase()
        if (extension.isNotEmpty()) {
            require(extension == "yaml" || extension == "yml") {
                "Unsupported file extension: .$extension"
            }
        }

        val imported = source.openStream().use { stream ->
            YamlProxyTargetConfig.parse(stream)
        }
        return merge(imported)
    }

    private fun merge(imported: YamlProxyTargetConfigList): ProxyTargetImportResult {
        if (imported.entries.isEmpty()) {
            return ProxyTargetImportResult(0, 0, listOf("Empty configuration"), determineDestinationFile())
        }

        val destinationFile = determineDestinationFile()
        destinationFile.parent.createDirectories()

        val existingEntries = YamlProxyTargetConfig.load(destinationFile).entries
        val mergedEntries = LinkedHashMap<String, YamlProxyTargetConfig>()
        val insertionOrder = mutableListOf<String>()

        for (entry in existingEntries) {
            val sanitized = sanitize(entry) ?: continue
            val key = sanitized.name.lowercase()
            if (mergedEntries.putIfAbsent(key, sanitized) == null) {
                insertionOrder += key
            }
        }

        var added = 0
        var replaced = 0
        val skipped = mutableListOf<String>()

        for (entry in imported.entries) {
            val sanitized = sanitize(entry)
            if (sanitized == null) {
                skipped += entry.name.ifBlank { "<unnamed>" }
                continue
            }

            val key = sanitized.name.lowercase()
            if (mergedEntries.containsKey(key)) {
                replaced++
            } else {
                added++
                insertionOrder += key
            }
            mergedEntries[key] = sanitized
        }

        val finalEntries = insertionOrder.mapNotNull { mergedEntries[it] }
        objectMapper.writeValue(destinationFile.toFile(), YamlProxyTargetConfigList(finalEntries))

        return ProxyTargetImportResult(added, replaced, skipped, destinationFile)
    }

    private fun sanitize(entry: YamlProxyTargetConfig): YamlProxyTargetConfig? {
        val name = entry.name.trim()
        val javConfigUrl = entry.javConfigUrl.trim()
        if (name.isEmpty() || javConfigUrl.isEmpty()) {
            return null
        }
        return entry.copy(
            name = name,
            javConfigUrl = javConfigUrl,
            modulus = entry.modulus?.trim()?.ifEmpty { null },
            revision = entry.revision?.trim()?.ifEmpty { null },
            runeliteBootstrapCommitHash = entry.runeliteBootstrapCommitHash?.trim()?.ifEmpty { null },
            runeliteGamepackUrl = entry.runeliteGamepackUrl?.trim()?.ifEmpty { null },
            binaryFolder = entry.binaryFolder?.trim()?.ifEmpty { null },
        )
    }

    private fun determineDestinationFile(): Path {
        return when {
            PROXY_TARGETS_FILE.exists() -> PROXY_TARGETS_FILE
            ALT_PROXY_TARGETS_FILE.exists() -> ALT_PROXY_TARGETS_FILE
            else -> PROXY_TARGETS_FILE
        }
    }
}
