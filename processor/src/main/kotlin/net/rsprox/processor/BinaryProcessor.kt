package net.rsprox.processor

import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.resolver.HistoricCacheResolver
import net.rsprox.processor.filters.ProcessorPropertyFilterSetStore
import net.rsprox.processor.result.ProcessedBinarySession
import net.rsprox.processor.settings.ProcessorSettingSetStore
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.cache.CachedCaches
import net.rsprox.proxy.cache.StatefulCacheProvider
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.plugin.DecoderLoader
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.settings.SettingSetStore
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

public sealed class BinaryProcessor(private val files: Collection<Path>) {
    protected fun collectAll(): List<ProcessedBinarySession> {
        val decoderLoader = DecoderLoader()
        HuffmanProvider.load()
        val cacheProvider = StatefulCacheProvider(CachedCaches(HistoricCacheResolver()))
        return collectAll(ProcessorPropertyFilterSetStore, ProcessorSettingSetStore, decoderLoader, cacheProvider)
    }

    private fun collectAll(
        filters: PropertyFilterSetStore,
        settings: SettingSetStore,
        decoderLoader: DecoderLoader,
        cacheProvider: StatefulCacheProvider
    ): List<ProcessedBinarySession> {
        val fileTreeWalk =
            files.map { it to BinaryBlob.decode(it, filters, settings) }.sortedBy { it.second.header.revision }
        return fileTreeWalk.map { (path, binary) -> process(path, binary, decoderLoader, cacheProvider) }
    }

    private fun process(
        binaryPath: Path,
        binary: BinaryBlob,
        decoderLoader: DecoderLoader,
        cacheProvider: StatefulCacheProvider,
    ): ProcessedBinarySession {
        cacheProvider.update(
            Js5MasterIndex.trimmed(
                binary.header.revision,
                binary.header.js5MasterIndex,
            ),
        )
        decoderLoader.load(cacheProvider)
        val latestPlugin = decoderLoader.getDecoder(binary.header.revision, cacheProvider)
        val session = DecodingSession(binary, latestPlugin)
        val messages = session.sequence().map { it.message }.toList()
        return ProcessedBinarySession(binaryPath, binary.header, messages)
    }

    public class SingleBinaryProcessor(file: Path) : BinaryProcessor(listOf(file)) {
        public fun collect(): ProcessedBinarySession {
            val sessions = collectAll()
            return sessions.single()
        }
    }

    public class MultiBinaryProcessor(files: Collection<Path>) : BinaryProcessor(files) {
        public fun collect(): List<ProcessedBinarySession> {
            return collectAll()
        }
    }

    public companion object {
        public fun fromFolder(path: Path): MultiBinaryProcessor {
            require(path.isDirectory()) { "Path must be a directory: ${path.absolutePathString()}" }
            val fileTreeWalk = path.toFile().walkTopDown().filter { it.extension == "bin" }.map { it.toPath() }
            return MultiBinaryProcessor(fileTreeWalk.toList())
        }

        public fun fromFile(file: Path): SingleBinaryProcessor {
            require(file.isRegularFile()) { "Binary file could not be found: ${file.absolutePathString()}" }
            check(file.extension == "bin") { "Binary file must have a .bin extension: ${file.absolutePathString()}" }
            return SingleBinaryProcessor(file)
        }
    }
}
