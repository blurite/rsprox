package net.rsprox.proxy.binary

import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.resolver.HistoricCacheResolver
import net.rsprox.proxy.cache.StatefulCacheProvider
import net.rsprox.proxy.config.FILTERS_DIRECTORY
import net.rsprox.proxy.config.SETTINGS_DIRECTORY
import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.proxy.plugin.PluginLoader
import net.rsprox.proxy.settings.DefaultSettingSetStore
import net.rsprox.proxy.util.NopSessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.indexing.IndexedKey
import net.rsprox.shared.indexing.IndexedType
import net.rsprox.shared.indexing.MultiMapBinaryIndex
import net.rsprox.transcriber.BaseMessageConsumerContainer
import net.rsprox.transcriber.indexer.IndexerTranscriberProvider
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

@Suppress("DuplicatedCode")
public class BinaryIndexer {
    private val pluginLoader: PluginLoader = PluginLoader()
    private val statefulCacheProvider: StatefulCacheProvider = StatefulCacheProvider(HistoricCacheResolver())

    public fun initialize() {
        Locale.setDefault(Locale.US)
        HuffmanProvider.load()
    }

    public fun index(binaryPath: Path): Map<IndexedType, Map<IndexedKey, Int>> {
        val filters = DefaultPropertyFilterSetStore.load(FILTERS_DIRECTORY)
        val settings = DefaultSettingSetStore.load(SETTINGS_DIRECTORY)
        val binary = BinaryBlob.decode(binaryPath, filters, settings)
        statefulCacheProvider.update(
            Js5MasterIndex.trimmed(
                binary.header.revision,
                binary.header.js5MasterIndex,
            ),
        )
        pluginLoader.load(binary.header.revision, statefulCacheProvider)
        val latestPlugin = pluginLoader.getPlugin(binary.header.revision)
        val transcriberProvider = IndexerTranscriberProvider()
        val session = DecodingSession(binary, latestPlugin)
        val folder = binaryPath.parent.resolve("indexed")
        Files.createDirectories(folder)
        val consumers = BaseMessageConsumerContainer(emptyList())
        val index = MultiMapBinaryIndex()
        val runner =
            transcriberProvider.provide(
                consumers,
                statefulCacheProvider,
                NopSessionMonitor,
                filters,
                settings,
                index,
            )
        val revision = binary.header.revision
        for ((direction, prot, packet) in session.sequence()) {
            try {
                when (direction) {
                    StreamDirection.CLIENT_TO_SERVER -> {
                        runner.onClientProt(prot, packet, revision)
                    }
                    StreamDirection.SERVER_TO_CLIENT -> {
                        runner.onServerPacket(prot, packet, revision)
                    }
                }
            } catch (t: NotImplementedError) {
                continue
            }
        }
        consumers.close()
        return index.results()
    }
}
