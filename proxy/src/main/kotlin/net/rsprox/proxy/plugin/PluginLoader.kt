package net.rsprox.proxy.plugin

import com.github.michaelbull.logging.InlineLogger
import io.github.classgraph.ClassGraph
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.ClientPacketDecoder
import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.ServerPacketDecoder
import net.rsprox.proxy.config.LATEST_SUPPORTED_PLUGIN
import net.rsprox.proxy.config.PLUGINS_DIRECTORY
import net.rsprox.proxy.config.TRANSCRIBERS_DIRECTORY
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.transcriber.TranscriberProvider
import java.io.File
import java.io.InputStream
import java.net.URLClassLoader

public class PluginLoader {
    private val plugins: MutableMap<Int, DecoderPlugin> = mutableMapOf()
    private val transcribers: MutableMap<String?, MutableMap<Int, TranscriberProvider>> = mutableMapOf()
    private val classloaders: MutableList<URLClassLoader> = mutableListOf()

    public fun load(
        type: String,
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
        loadTranscriberPlugins(type, revision)
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

    @Suppress("UNUSED_VARIABLE")
    private fun loadTranscriberPlugins(
        @Suppress("UNUSED_PARAMETER") type: String,
        revision: Int,
    ) {
        val plugins =
            TRANSCRIBERS_DIRECTORY
                .toFile()
                .walkTopDown()
                .filter { it.isFile }
        for (file in plugins) {
            try {
                val match = transcriberRegex.find(file.name) ?: continue
                val (revisionString, name) = match.destructured
                val rev = revisionString.toInt()
                if (rev != revision) continue
                if (rev > LATEST_SUPPORTED_PLUGIN) continue
                loadTranscriberPlugin(file, rev)
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error loading transcriber $file"
                }
            }
        }
    }

    private fun loadTranscriberPlugin(
        file: File,
        revision: Int,
    ) {
        logger.debug { "Attempting to load ${file.nameWithoutExtension}." }
        // Decoder is always loaded first so we add it as a "dependency"
        val decoderClassLoader = classloaders.first()
        val loader =
            URLClassLoader(
                arrayOf(file.toURI().toURL()),
                decoderClassLoader,
            )
        classloaders += loader
        val scanner = ClassGraph()
        val result =
            scanner
                .ignoreParentClassLoaders()
                .overrideClassLoaders(loader)
                .enableClassInfo()
                .scan()
        val plugin =
            result
                .getClassesImplementing("net.rsprox.transcriber.TranscriberProvider")
        if (plugin.isEmpty()) {
            logger.warn { "No provider implementation found for ${file.nameWithoutExtension}." }
            return
        }
        if (plugin.size > 1) {
            logger.warn { "More than one provider implementation found for ${file.nameWithoutExtension}" }
        }
        // For now only accept first. In the future, perhaps allow loading multiple from a single jar?
        val clazz = plugin.first().loadClass()
        val instance = clazz.getDeclaredConstructor().newInstance() as TranscriberProvider
        val metainf = loader.getResourceAsStream("TRANSCRIBER-METAINF")
        transcribers.getOrPut(parseType(metainf), ::mutableMapOf)[revision] = instance
    }

    private fun parseType(inputStream: InputStream?): String? {
        if (inputStream == null) {
            return null
        }
        val lines = inputStream.reader(Charsets.UTF_8).readLines()
        val type = lines.firstOrNull { it.startsWith("type=") } ?: return null
        return type.substringAfter("type=")
    }

    public fun getTranscriberProvider(revision: Int): TranscriberProvider {
        val customTranscriber = transcribers[null]?.get(revision)
        if (customTranscriber != null) {
            return customTranscriber
        }
        return transcribers
            .getValue("base")
            .getValue(revision)
    }

    public fun getIndexerProvider(revision: Int): TranscriberProvider {
        return transcribers
            .getValue("indexer")
            .getValue(revision)
    }

    private companion object {
        private val logger = InlineLogger()
        private val pluginRegex = Regex("""^(osrs)-(\d+)\.jar$""")
        private val transcriberRegex = Regex("""transcriber-(\d+)-(.+)""")
    }
}
