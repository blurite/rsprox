package net.rsprox.proxy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.NativeClientType
import net.rsprox.patch.PatchResult
import net.rsprox.patch.native.NativePatchCriteria
import net.rsprox.patch.native.NativePatcher
import net.rsprox.proxy.config.CLIENTS_DIRECTORY
import net.rsprox.proxy.config.CURRENT_REVISION
import net.rsprox.proxy.downloader.JagexNativeClientDownloader
import net.rsprox.proxy.downloader.RuneWikiNativeClientDownloader
import java.util.Locale
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.name

@Suppress("DuplicatedCode")
public class ClientPatcherCommand : CliktCommand(name = "patch") {
    private val version by option("-version").required()
    private val type by option("-type").required().help("Valid options include 'win' and 'mac'")
    private val modulus by option("-modulus")
    private val acceptAllHosts by option("-acceptallhosts").default("true")
    private val javconfig by option("-javconfig")
    private val worldlist by option("-worldlist")
    private val varpcount by option("-varpcount")
    private val siteurl by option("-siteurl")
    private val name by option("-name")

    override fun run() {
        Locale.setDefault(Locale.US)
        val type =
            when (this.type) {
                "win" -> NativeClientType.WIN
                "mac" -> NativeClientType.MAC
                else -> error("Invalid client type: ${this.type}")
            }
        val folder = CLIENTS_DIRECTORY.resolve(version)
        val path =
            if (version == "latest") {
                JagexNativeClientDownloader.download(type)
            } else {
                RuneWikiNativeClientDownloader.download(folder, type, version)
            }

        val outputFile = Path(path.name)
        path.copyTo(outputFile, overwrite = true)
        val revisionNum =
            if (version == "latest") {
                CURRENT_REVISION
            } else {
                this.version.substringBefore(".").toInt()
            }
        val patcher = NativePatcher()
        val builder = NativePatchCriteria.Builder(type)
        val modulus = this.modulus
        if (modulus != null) {
            builder.rsaModulus(modulus)
        }
        if (acceptAllHosts == "true") {
            builder.acceptAllHosts()
        }
        val javConfig = this.javconfig
        if (javConfig != null) {
            builder.javConfig(javConfig)
        }
        val worldList = this.worldlist
        if (worldList != null) {
            builder.worldList(worldList)
        }
        val varpCount = this.varpcount
        if (varpCount != null) {
            builder.varpCount(if (revisionNum <= 216) 4000 else 5000, varpCount.toInt())
        }
        val siteUrl = this.siteurl
        if (siteUrl != null) {
            builder.siteUrl(siteUrl)
        }
        val name = this.name
        if (name != null) {
            builder.name(name)
        }
        val criteria = builder.build()
        val result =
            patcher.patch(
                outputFile,
                criteria,
            )
        check(result is PatchResult.Success) {
            "Failed to patch"
        }
        logger.info { "$version ${type.systemShortName} patched client written to ${outputFile.absolutePathString()}" }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}

public fun main(args: Array<String>) {
    ClientPatcherCommand().main(args)
}
