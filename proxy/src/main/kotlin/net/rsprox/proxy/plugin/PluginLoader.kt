package net.rsprox.proxy.plugin

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.ClientPacketDecoder
import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.ServerPacketDecoder
import net.rsprox.proxy.config.LATEST_SUPPORTED_PLUGIN
import net.rsprox.proxy.config.PLUGINS_DIRECTORY
import net.rsprox.proxy.huffman.HuffmanProvider
import java.io.File
import java.net.URLClassLoader

public class PluginLoader {
    private val plugins: MutableMap<Int, DecoderPlugin> = mutableMapOf()
    private val classloaders: MutableList<URLClassLoader> = mutableListOf()

    public fun load(
        revision: Int,
        cache: CacheProvider,
    ) {
        if (plugins.containsKey(revision)) {
            return
        }
        for (loader in classloaders) {
            loader.close()
        }
        classloaders.clear()
        loadDecoderPlugin(revision, cache)
    }

    private fun loadDecoderPlugin(
        revision: Int,
        cache: CacheProvider,
    ) {
        val plugins =
            PLUGINS_DIRECTORY
                .toFile()
                .walkTopDown()
                .filter { it.isFile }
        for (file in plugins) {
            try {
                val match = pluginRegex.find(file.name) ?: continue
                val (_, revisionString) = match.destructured
                val rev = revisionString.toInt()
                if (rev != revision) continue
                if (rev > LATEST_SUPPORTED_PLUGIN) continue
                loadDecoderPlugin(cache, file, rev)
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error loading plugin $file"
                }
            }
        }
    }

    private fun loadDecoderPlugin(
        cache: CacheProvider,
        file: File,
        revision: Int,
    ) {
        logger.debug { "Attempting to load ${file.nameWithoutExtension} plugin." }
        val jarLoader =
            URLClassLoader(
                arrayOf(file.toURI().toURL()),
                this::class.java.classLoader,
            )
        classloaders += jarLoader
        loadDecoderPlugin(cache, jarLoader, revision)
        logger.debug { "Loaded ${file.nameWithoutExtension} plugin." }
    }

    private fun loadDecoderPlugin(
        cache: CacheProvider,
        classLoader: ClassLoader,
        revision: Int,
    ) {
        val clientPacketDecoderService =
            Class.forName(
                "net.rsprox.protocol.ClientPacketDecoderService",
                true,
                classLoader,
            )
        val huffmanCodec = HuffmanProvider.get()
        val clientPacketDecoder =
            clientPacketDecoderService
                .getDeclaredConstructor(HuffmanCodec::class.java)
                .newInstance(huffmanCodec) as ClientPacketDecoder

        val serverPacketDecoderService =
            Class.forName(
                "net.rsprox.protocol.ServerPacketDecoderService",
                true,
                classLoader,
            )
        val serverPacketDecoder =
            serverPacketDecoderService
                .getDeclaredConstructor(HuffmanCodec::class.java, CacheProvider::class.java)
                .newInstance(huffmanCodec, cache) as ServerPacketDecoder

        @Suppress("UNCHECKED_CAST")
        val clientProtProvider =
            Class
                .forName(
                    "net.rsprox.protocol.GameClientProtProvider",
                    true,
                    classLoader,
                ).kotlin.objectInstance as ProtProvider<ClientProt>

        @Suppress("UNCHECKED_CAST")
        val serverProtProvider =
            Class
                .forName(
                    "net.rsprox.protocol.GameServerProtProvider",
                    true,
                    classLoader,
                ).kotlin.objectInstance as ProtProvider<ClientProt>
        val decoderPlugin =
            DecoderPlugin(
                clientPacketDecoder,
                serverPacketDecoder,
                clientProtProvider,
                serverProtProvider,
            )
        val old = this.plugins.put(revision, decoderPlugin)
        if (old != null) {
            throw IllegalStateException("Multiple $revision plugins detected.")
        }
    }

    public fun getPlugin(revision: Int): DecoderPlugin {
        return plugins.getValue(revision)
    }

    public fun getPluginOrNull(revision: Int): DecoderPlugin? {
        return plugins[revision]
    }

    private companion object {
        private val logger = InlineLogger()
        private val pluginRegex = Regex("""^(osrs)-(\d+)\.jar$""")
    }
}
