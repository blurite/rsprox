package net.rsprox.shared.symbols

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.SymbolType
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.Path

public class SymbolProperties(
    private val rootPath: Path,
    private val rootRegex: Regex,
    private val rootExtension: String,
    private val symbols: List<SymbolPointer>,
) {
    public fun buildSymbolTypeEntries(): Map<SymbolType, SymbolTypeEntry> {
        val map = buildDefaultSymbolTypeEntries()
        for (symbol in this.symbols) {
            val entry =
                SymbolTypeEntry(
                    symbol.path,
                    symbol.regex,
                )
            when (symbol.name) {
                "varp" -> map[SymbolType.VarpSymbol] = entry
                "varbit" -> map[SymbolType.VarbitSymbol] = entry
                "clientscript", "script", "proc" -> map[SymbolType.ScriptSymbol] = entry
                else -> {
                    val scriptVarType =
                        ScriptVarType.entries
                            .find { it.name.lowercase() == symbol.name }
                    if (scriptVarType != null) {
                        map[SymbolType.ScriptVarTypeSymbol(scriptVarType)] = entry
                    }
                }
            }
        }
        return map
    }

    private fun buildDefaultSymbolTypeEntries(): MutableMap<SymbolType, SymbolTypeEntry> {
        val map = mutableMapOf<SymbolType, SymbolTypeEntry>()
        for (entry in ScriptVarType.entries) {
            map[SymbolType.ScriptVarTypeSymbol(entry)] =
                SymbolTypeEntry(
                    rootPath.resolve("${entry.name.lowercase()}.$rootExtension"),
                    rootRegex,
                )
        }
        map[SymbolType.VarpSymbol] =
            SymbolTypeEntry(
                rootPath.resolve("varp.$rootExtension"),
                rootRegex,
            )
        map[SymbolType.VarbitSymbol] =
            SymbolTypeEntry(
                rootPath.resolve("varbit.$rootExtension"),
                rootRegex,
            )
        map[SymbolType.ScriptSymbol] =
            SymbolTypeEntry(
                rootPath.resolve("clientscript.$rootExtension"),
                rootRegex,
            )
        return map
    }

    public companion object {
        private val logger = InlineLogger()
        private val SYMBOL_REGEX: Regex = Regex("""^(\w+)\.(\w+)$""")
        private const val ROOT_PATH: String = "symbol.root.path"
        private const val ROOT_REGEX: String = "symbol.root.regex"
        private const val ROOT_EXTENSION: String = "symbol.root.extension"
        private const val TYPE_PREFIX = "symbol.type."

        public fun load(properties: Properties): SymbolProperties {
            val rootPathString: String =
                properties.getProperty(ROOT_PATH)
                    ?: throw IllegalArgumentException("Root path is not defined.")
            val rootRegexString: String =
                properties.getProperty(ROOT_REGEX)
                    ?: throw IllegalArgumentException("Default regex is not defined")
            val rootExtension: String =
                properties.getProperty(ROOT_EXTENSION)
                    ?: throw IllegalArgumentException("Extension is not defined")
            val rootPath = Path(rootPathString)
            val rootRegex = Regex(rootRegexString)
            val symbolBuilders = mutableListOf<SymbolPointer.Builder>()
            for ((k, v) in properties) {
                val key = k.toString()
                if (!key.startsWith(TYPE_PREFIX)) {
                    continue
                }
                val symbolType = key.substringAfter("symbol.type.")
                val match = SYMBOL_REGEX.find(symbolType) ?: continue
                val (type, op) = match.destructured
                var entry = symbolBuilders.firstOrNull { it.name == type }
                if (entry == null) {
                    entry = SymbolPointer.Builder(type)
                    symbolBuilders += entry
                }
                val value = v.toString()
                when (op) {
                    "path" -> {
                        entry.path = rootPath.resolve("$value.$rootExtension")
                    }
                    "regex" -> {
                        entry.regex = Regex(value)
                    }
                    "extension" -> {
                        entry.extension = value
                    }
                    else -> {
                        logger.warn {
                            "Unknown symbol op: $op, $value"
                        }
                    }
                }
            }
            val symbols = symbolBuilders.map { it.build(rootPath, rootExtension, rootRegex) }
            return SymbolProperties(rootPath, rootRegex, rootExtension, symbols)
        }
    }
}

public class SymbolPointer(
    public val name: String,
    public var path: Path,
    public var regex: Regex,
) {
    public class Builder(
        public val name: String,
    ) {
        public var path: Path? = null
        public var regex: Regex? = null
        public var extension: String? = null

        public fun build(
            rootPath: Path,
            rootExtension: String,
            rootRegex: Regex,
        ): SymbolPointer {
            val path = this.path
            val regex = this.regex
            val ext = this.extension
            val extensionToUse = ext ?: rootExtension
            val symbolPath = path ?: rootPath.resolve("$name.$extensionToUse")
            val symbolRegex = regex ?: rootRegex
            return SymbolPointer(
                name,
                symbolPath,
                symbolRegex,
            )
        }
    }
}
