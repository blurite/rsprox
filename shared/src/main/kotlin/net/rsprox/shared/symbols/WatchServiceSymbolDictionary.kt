package net.rsprox.shared.symbols

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.shared.property.SymbolType
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread
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
    private lateinit var thread: Thread

    @Volatile
    private var running: Boolean = true

    override fun start() {
        service = FileSystems.getDefault().newWatchService()
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
        this.thread = launchThread()
    }

    private fun launchThread(): Thread {
        return thread(
            start = true,
            isDaemon = true,
        ) {
            while (this.running) {
                try {
                    val next =
                        try {
                            service.take()
                        } catch (_: ClosedWatchServiceException) {
                            return@thread
                        }
                    checkForReloads(next)
                } catch (t: Throwable) {
                    logger.error(t) {
                        "Error in watch service for dictionary"
                    }
                }
            }
        }
    }

    private fun checkForReloads(key: WatchKey) {
        for (event in key.pollEvents()) {
            if (event.kind() != StandardWatchEventKinds.ENTRY_MODIFY) continue
            val context = event.context() ?: continue
            reloadSymbols(context.toString())
        }
        key.reset()
    }

    private fun reloadSymbols(fileName: String) {
        for ((type, v) in typeEntries) {
            if (!v.path.exists()) continue
            val file = v.path.toFile()
            if (fileName != file.name) continue
            val decoded =
                try {
                    v.read()
                } catch (_: Exception) {
                    // Skip any parsing exceptions
                    return
                }
            logger.debug { "Reloaded ${file.name} symbols" }
            symbols[type] = decoded
            return
        }
    }

    override fun stop() {
        running = false
        if (this::service.isInitialized) {
            service.close()
        }
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
