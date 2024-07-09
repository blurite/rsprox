package net.rsprox.proxy.plugin

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ServerProt
import net.rsprox.protocol.ClientPacketDecoder
import net.rsprox.protocol.ProtProvider
import net.rsprox.proxy.config.PLUGINS_DIRECTORY
import java.io.File
import java.net.URLClassLoader

public class PluginLoader {
    private val plugins: MutableMap<Int, DecoderPlugin> = mutableMapOf()

    public fun loadPlugins(type: String) {
        val plugins =
            PLUGINS_DIRECTORY
                .toFile()
                .walkTopDown()
                .filter { it.isFile }
        for (file in plugins) {
            try {
                val match = pluginRegex.find(file.name) ?: continue
                val (name, revisionString) = match.destructured
                if (name != type) {
                    continue
                }
                loadPlugin(file, revisionString.toInt())
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error loading plugin $file"
                }
            }
        }
    }

    private fun loadPlugin(
        file: File,
        revision: Int,
    ) {
        logger.debug { "Attempting to load ${file.nameWithoutExtension} plugin." }
        val loader =
            URLClassLoader(
                arrayOf(file.toURI().toURL()),
                this::class.java.classLoader,
            )
        val classToLoad =
            Class.forName(
                "net.rsprox.protocol.ClientPacketDecoderService",
                true,
                loader,
            )
        val clientPacketDecoder =
            classToLoad
                .getDeclaredConstructor()
                .newInstance() as ClientPacketDecoder

        @Suppress("UNCHECKED_CAST")
        val clientProtProvider =
            Class
                .forName(
                    "net.rsprox.protocol.GameClientProtProvider",
                    true,
                    loader,
                ).kotlin.objectInstance as ProtProvider<ClientProt>

        @Suppress("UNCHECKED_CAST")
        val serverProtProvider =
            Class
                .forName(
                    "net.rsprox.protocol.GameServerProtProvider",
                    true,
                    loader,
                ).kotlin.objectInstance as ProtProvider<ServerProt>
        val decoderPlugin =
            DecoderPlugin(
                clientPacketDecoder,
                clientProtProvider,
                serverProtProvider,
            )
        val old = this.plugins.put(revision, decoderPlugin)
        if (old != null) {
            throw IllegalStateException("Multiple $revision plugins detected.")
        }
        logger.debug { "Loaded ${file.nameWithoutExtension} plugin." }
    }

    public fun getPlugin(revision: Int): DecoderPlugin {
        return plugins.getValue(revision)
    }

    private companion object {
        private val logger = InlineLogger()
        private val pluginRegex = Regex("""^(osrs)-(\d+)\.jar$""")
    }
}
