package net.rsprox.proxy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.binary.StreamDirection
import net.rsprox.proxy.config.BINARY_PATH
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.proxy.plugin.PluginLoader
import net.rsprox.transcriber.MessageConsumer
import net.rsprox.transcriber.MessageConsumerContainer
import java.nio.file.Path
import kotlin.io.path.exists

@Suppress("DuplicatedCode")
public class TranscribeCommand : CliktCommand(name = "tostring") {
    private val name by option("-name")

    override fun run() {
        val pluginLoader = PluginLoader()
        HuffmanProvider.load()
        pluginLoader.loadTranscriberPlugins("osrs")
        val fileName = this.name
        if (fileName != null) {
            val binaryName = if (fileName.endsWith(".bin")) fileName else "$fileName.bin"
            val file = BINARY_PATH.resolve(binaryName)
            if (!file.exists()) {
                echo("Unable to locate file $fileName in $BINARY_PATH")
                return
            }
            stdoutTranscribe(file, pluginLoader)
        } else {
            val fileTreeWalk =
                BINARY_PATH
                    .toFile()
                    .walkTopDown()
                    .filter { it.extension == "bin" }
            for (bin in fileTreeWalk) {
                stdoutTranscribe(bin.toPath(), pluginLoader)
            }
        }
    }

    private fun stdoutTranscribe(
        binaryPath: Path,
        pluginLoader: PluginLoader,
    ) {
        val binary = BinaryBlob.decode(binaryPath)
        val latestPlugin = pluginLoader.getPlugin(binary.header.revision)
        val transcriberProvider = pluginLoader.getTranscriberProvider(binary.header.revision)
        val session = DecodingSession(binary, latestPlugin)
        val consumers = MessageConsumerContainer(listOf(createStdOutConsumer()))
        val runner = transcriberProvider.provide(consumers)

        println("------------------")
        println("Header information")
        println("version: ${binary.header.revision}.${binary.header.subRevision}")
        println("client type: ${binary.header.clientType}")
        println("platform type: ${binary.header.platformType}")
        println(
            "world: ${binary.header.worldId}, host: ${binary.header.worldHost}, " +
                "flags: ${binary.header.worldFlags}, location: ${binary.header.worldLocation}, " +
                "activity: ${binary.header.worldActivity}",
        )
        println("local player index: ${binary.header.localPlayerIndex}")
        println("-------------------")

        for ((direction, prot, packet) in session.sequence()) {
            try {
                when (direction) {
                    StreamDirection.CLIENT_TO_SERVER -> {
                        runner.onClientProt(prot, packet)
                    }
                    StreamDirection.SERVER_TO_CLIENT -> {
                        runner.onServerPacket(prot, packet)
                    }
                }
            } catch (t: NotImplementedError) {
                continue
            }
        }
        consumers.close()
    }

    private fun createStdOutConsumer(): MessageConsumer {
        return object : MessageConsumer {
            override fun consume(message: List<String>) {
                for (line in message) {
                    println(line)
                }
            }

            override fun close() {
            }
        }
    }
}

public fun main(args: Array<String>) {
    TranscribeCommand().main(args)
}
