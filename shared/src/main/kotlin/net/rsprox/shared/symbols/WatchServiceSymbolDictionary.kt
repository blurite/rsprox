package net.rsprox.shared.symbols

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.shared.property.SymbolType
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchService
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

public class WatchServiceSymbolDictionary(
    private val typeEntries: Map<SymbolType, SymbolTypeEntry>,
) : SymbolDictionary {
    private val symbols =
        typeEntries.entries.associateTo(ConcurrentHashMap()) { (k, v) ->
            k to v.read()
        }
    private lateinit var service: WatchService
    private lateinit var executor: ScheduledExecutorService

    override fun start() {
        service = FileSystems.getDefault().newWatchService()
        executor = Executors.newSingleThreadScheduledExecutor()
        val directories =
            typeEntries.values.mapNotNull { entry ->
                if (!entry.path.exists()) return@mapNotNull null
                if (entry.path.isDirectory()) return@mapNotNull entry.path
                val parent = entry.path.parent
                if (!parent.exists() || !parent.isDirectory()) return@mapNotNull null
                return@mapNotNull parent
            }
        val distinctDirectories = directories.distinct()
        for (directory in distinctDirectories) {
            directory.register(service, StandardWatchEventKinds.ENTRY_MODIFY)
        }
        executor.scheduleWithFixedDelay({
            while (true) {
                val key = service.poll() ?: break
                loop@for (event in key.pollEvents()) {
                    if (event.kind() != StandardWatchEventKinds.ENTRY_MODIFY) continue
                    val context = event.context() ?: continue
                    val str = context.toString()
                    for ((type, v) in typeEntries) {
                        if (!v.path.exists()) continue
                        val file = v.path.toFile()
                        if (str == file.name) {
                            val decoded =
                                try {
                                    v.read()
                                } catch (e: Exception) {
                                    // Skip any parsing exceptions
                                    continue@loop
                                }
                            logger.debug { "Reloaded ${file.name} symbols" }
                            symbols[type] = decoded
                            continue@loop
                        }
                    }
                }
            }
        }, 5L, 5L, TimeUnit.SECONDS)
    }

    override fun stop() {
        service.close()
        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.SECONDS)
    }

    override fun getScriptVarTypeName(
        id: Int,
        type: ScriptVarType,
    ): String? {
        val symbolMap = symbols[SymbolType.ScriptVarTypeSymbol(type)] ?: return null
        return symbolMap[id]
    }

    override fun getVarpName(id: Int): String? {
        val symbolMap = symbols[SymbolType.VarpSymbol] ?: return null
        return symbolMap[id]
    }

    override fun getVarbitName(id: Int): String? {
        val symbolMap = symbols[SymbolType.VarbitSymbol] ?: return null
        return symbolMap[id]
    }

    override fun getScriptName(id: Int): String? {
        val symbolMap = symbols[SymbolType.ScriptSymbol] ?: return null
        return symbolMap[id]
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
