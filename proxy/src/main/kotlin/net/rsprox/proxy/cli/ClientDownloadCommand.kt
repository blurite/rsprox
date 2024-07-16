package net.rsprox.proxy.cli

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import net.rsprox.patch.NativeClientType
import net.rsprox.proxy.downloader.NativeClientDownloader

public class ClientDownloadCommand : CliktCommand(name = "download") {
    private val type by argument(
        name = "type",
        help = "The type of the client to download.",
        completionCandidates =
            CompletionCandidates.Fixed(
                "native-win",
                "native-mac",
            ),
    )

    override fun run() {
        when (type) {
            "native-win" -> {
                NativeClientDownloader.download(NativeClientType.WIN)
            }
            "native-mac" -> {
                NativeClientDownloader.download(NativeClientType.MAC)
            }
            else -> {
                echo("Invalid type provided: $type")
            }
        }
    }
}

public fun main(args: Array<String>) {
    ClientDownloadCommand().main(args)
}
