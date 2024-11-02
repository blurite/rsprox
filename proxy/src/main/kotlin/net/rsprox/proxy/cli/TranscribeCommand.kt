package net.rsprox.proxy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.resolver.HistoricCacheResolver
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.cache.StatefulCacheProvider
import net.rsprox.proxy.config.BINARY_PATH
import net.rsprox.proxy.config.FILTERS_DIRECTORY
import net.rsprox.proxy.config.SETTINGS_DIRECTORY
import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.proxy.plugin.PluginLoader
import net.rsprox.proxy.settings.DefaultSettingSetStore
import net.rsprox.proxy.util.NopSessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.indexing.NopBinaryIndex
import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.MessageConsumer
import net.rsprox.transcriber.state.SessionState
import net.rsprox.transcriber.state.SessionTracker
import net.rsprox.transcriber.text.TextMessageConsumerContainer
import net.rsprox.transcriber.text.TextTranscriberProvider
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.time.measureTime

@Suppress("DuplicatedCode")
public class TranscribeCommand : CliktCommand(name = "transcribe") {
    private val name by option("-name")

    override fun run() {
        Locale.setDefault(Locale.US)
        val pluginLoader = PluginLoader()
        HuffmanProvider.load()
        val provider = StatefulCacheProvider(HistoricCacheResolver())
        val filters = DefaultPropertyFilterSetStore.load(FILTERS_DIRECTORY)
        val settings = DefaultSettingSetStore.load(SETTINGS_DIRECTORY)
        val fileName = this.name
        if (fileName != null) {
            val binaryName = if (fileName.endsWith(".bin")) fileName else "$fileName.bin"
            val file = BINARY_PATH.resolve(binaryName)
            if (!file.exists()) {
                echo("Unable to locate file $fileName in $BINARY_PATH")
                return
            }
            val binary = BinaryBlob.decode(file, filters, settings)
            val time =
                measureTime {
                    fileTranscribe(file, binary, pluginLoader, provider, filters, settings)
                }
            logger.debug { "$file took $time to transcribe." }
        } else {
            // Sort all the binaries according to revision, so we don't end up loading and unloading plugins
            // repeatedly for the same things, as we can only have one plugin available at a time
            // to avoid classloading problems
            val fileTreeWalk =
                BINARY_PATH
                    .toFile()
                    .walkTopDown()
                    .filter { it.extension == "bin" }
                    .map { it.toPath() }
                    .map { it to BinaryBlob.decode(it, filters, settings) }
                    .sortedBy { it.second.header.revision }
            for ((path, blob) in fileTreeWalk) {
                val time =
                    measureTime {
                        fileTranscribe(path, blob, pluginLoader, provider, filters, settings)
                    }
                logger.debug { "${path.name} took $time to transcribe." }
            }
        }
    }

    private fun fileTranscribe(
        binaryPath: Path,
        binary: BinaryBlob,
        pluginLoader: PluginLoader,
        statefulCacheProvider: StatefulCacheProvider,
        filters: PropertyFilterSetStore,
        settings: SettingSetStore,
    ) {
        statefulCacheProvider.update(
            Js5MasterIndex.trimmed(
                binary.header.revision,
                binary.header.js5MasterIndex,
            ),
        )
        pluginLoader.load(binary.header.revision, statefulCacheProvider)
        val latestPlugin = pluginLoader.getPlugin(binary.header.revision)
        val transcriberProvider = TextTranscriberProvider()
        val session = DecodingSession(binary, latestPlugin)
        val textPath = binaryPath.parent.resolve(binaryPath.nameWithoutExtension + ".txt")
        val writer = textPath.bufferedWriter()
        val consumers = TextMessageConsumerContainer(listOf(createBufferedWriterConsumer(writer)))
        val sessionState = SessionState(settings)
        val runner =
            transcriberProvider.provide(
                consumers,
                statefulCacheProvider,
                NopSessionMonitor,
                filters,
                settings,
                NopBinaryIndex,
                sessionState,
            )
        val sessionTracker =
            SessionTracker(
                sessionState,
                statefulCacheProvider.get(),
                NopSessionMonitor,
            )

        writer.appendLine("------------------")
        writer.appendLine("Header information")
        writer.appendLine("version: ${binary.header.revision}.${binary.header.subRevision}")
        writer.appendLine("client type: ${binary.header.clientType}")
        writer.appendLine("platform type: ${binary.header.platformType}")
        writer.appendLine(
            "world: ${binary.header.worldId}, host: ${binary.header.worldHost}, " +
                "flags: ${binary.header.worldFlags}, location: ${binary.header.worldLocation}, " +
                "activity: ${binary.header.worldActivity}",
        )
        writer.appendLine("local player index: ${binary.header.localPlayerIndex}")
        writer.appendLine("-------------------")
        val revision = binary.header.revision
        for ((direction, prot, packet) in session.sequence()) {
            try {
                when (direction) {
                    StreamDirection.CLIENT_TO_SERVER -> {
                        sessionTracker.onClientPacket(packet, prot)
                        sessionTracker.beforeTranscribe(packet)
                        runner.onClientProt(prot, packet, revision)
                        sessionTracker.afterTranscribe(packet)
                    }
                    StreamDirection.SERVER_TO_CLIENT -> {
                        sessionTracker.onServerPacket(packet, prot)
                        sessionTracker.beforeTranscribe(packet)
                        runner.onServerPacket(prot, packet, revision)
                        sessionTracker.afterTranscribe(packet)
                    }
                }
            } catch (t: NotImplementedError) {
                continue
            }
        }
        consumers.close()
        // Set the last modified date to match up with the .bin file, so it's easier to find and link files
        // in particular when re-ordering files in descending order
        Files.setLastModifiedTime(textPath, Files.getLastModifiedTime(binaryPath))
    }

    private fun createBufferedWriterConsumer(writer: BufferedWriter): MessageConsumer {
        return object : MessageConsumer {
            var lastCycle = -1

            override fun consume(
                formatter: PropertyTreeFormatter,
                cycle: Int,
                property: RootProperty,
            ) {
                if (cycle != lastCycle) {
                    if (lastCycle != -1) {
                        writer.newLine()
                    }
                    lastCycle = cycle
                    writer.write("[$cycle]")
                    writer.newLine()
                }
                val result = formatter.format(property)
                for (line in result) {
                    // Add four space indentation due to the cycle header
                    writer.write("    ")
                    writer.write(line)
                    writer.newLine()
                }
            }

            override fun close() {
                writer.flush()
                writer.close()
            }
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}

public fun main(args: Array<String>) {
    TranscribeCommand().main(args)
}
