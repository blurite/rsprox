package net.rsprox.proxy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.NativeClientType
import net.rsprox.patch.PatchResult
import net.rsprox.patch.native.NativePatchCriteria
import net.rsprox.patch.native.NativePatcher
import net.rsprox.patch.runelite.RuneLitePatchCriteria
import net.rsprox.patch.runelite.RuneLitePatcher
import net.rsprox.proxy.config.*
import net.rsprox.proxy.downloader.JagexNativeClientDownloader
import net.rsprox.proxy.downloader.RuneLiteClientDownloader
import net.rsprox.proxy.downloader.RuneWikiNativeClientDownloader
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.writeBytes
import kotlin.system.exitProcess

@Suppress("DuplicatedCode")
public class ClientPatcherCommand : CliktCommand(name = "patch") {
    private val version by option("-version").default("latest")
    private val type by option("-type").default("runelite").help("Valid options include 'win', 'mac' and 'runelite'")
    private val modulus by option("-modulus")
    private val acceptAllHosts by option("-acceptallhosts").default("true")
    private val javconfig by option("-javconfig")
    private val worldlist by option("-worldlist")
    private val varpcount by option("-varpcount")
    private val siteurl by option("-siteurl")
    private val name by option("-name")
    private val port by option("-port").default("43594").help("The port that the patched client will connect to. Default is 43594.")

    override fun run() {
        createConfigurationDirectories(CONFIGURATION_PATH)
        createConfigurationDirectories(CLIENTS_DIRECTORY)
        createConfigurationDirectories(SOCKETS_DIRECTORY)
        createConfigurationDirectories(SIGN_KEY_DIRECTORY)
        createConfigurationDirectories(RUNELITE_LAUNCHER_REPO_DIRECTORY)

        Locale.setDefault(Locale.US)
        val type =
            when (this.type) {
                "win" -> NativeClientType.WIN
                "mac" -> NativeClientType.MAC
                "runelite" -> NativeClientType.RUNELITE_JAR
                else -> error("Invalid client type: ${this.type}")
            }

        if (type == NativeClientType.RUNELITE_JAR) {
            deleteTemporaryRuneLiteJars()
            transferFakeCertificate()
        }

        val folder = CLIENTS_DIRECTORY.resolve(version)
        val outputPath: Path = when {
            type == NativeClientType.RUNELITE_JAR -> {
                RuneLiteClientDownloader.cleanupAndDownloadArtifacts(RUNELITE_LAUNCHER_REPO_DIRECTORY)
                folder
            }
            version == "latest" -> JagexNativeClientDownloader.download(type)
            else -> RuneWikiNativeClientDownloader.download(folder, type, version)
        }

        val builder = when {
            type == NativeClientType.RUNELITE_JAR -> RuneLitePatchCriteria.Builder()
                .setArtifactsDir(RUNELITE_LAUNCHER_REPO_DIRECTORY)
                .setBootstrap(RuneLiteClientDownloader.bootstrap)

            else -> NativePatchCriteria.Builder(type)
        }

        val revisionNum =
            if (version == "latest") {
                CURRENT_REVISION
            } else {
                this.version.substringBefore(".").toInt()
            }

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
        val port = this.port
        if (port != "43594") {
            builder.port(port.toInt())
        }

        val result: PatchResult = when (val criteria = builder.build()) {
            is NativePatchCriteria -> NativePatcher().patch(outputPath, criteria)
            is RuneLitePatchCriteria -> RuneLitePatcher().patch(outputPath, criteria)
            else -> error("Invalid criteria type")
        }
        check(result is PatchResult.Success) {
            "Failed to patch"
        }
        logger.info { "$version ${type.systemShortName} patched client written to ${result.outputPath.absolutePathString()}" }
    }

    private fun createConfigurationDirectories(path: Path) {
        runCatching("Unable to create configuration directory: $path") {
            Files.createDirectories(path)
        }
    }

    private inline fun <T> runCatching(
        errorMessage: String,
        block: () -> T,
    ): T {
        try {
            return block()
        } catch (t: Throwable) {
            logger.error(t) {
                errorMessage
            }
            exitProcess(-1)
        }
    }

    public companion object {
        private val logger = InlineLogger()

        public fun deleteTemporaryRuneLiteJars() {
            val path = Path(System.getProperty("user.home"), ".runelite", "repository2")
            if (!path.exists(LinkOption.NOFOLLOW_LINKS)) return
            val files = path.toFile().walkTopDown()
            val namesToDelete =
                listOf(
                    "client",
                    "injected-client",
                    "runelite-api",
                )
            for (file in files) {
                if (!file.isFile) {
                    continue
                }
                val match = namesToDelete.any { file.name.startsWith(it) }
                if (!match) continue
                if (!file.name.endsWith("-patched.jar")) continue
                file.delete()
            }
        }

        public fun transferFakeCertificate() {
            if (FAKE_CERTIFICATE_FILE.exists(LinkOption.NOFOLLOW_LINKS)) {
                return
            }

            logger.debug { "Copying fake certificate" }
            val resource =
                RuneLitePatcher::class.java
                    .getResourceAsStream("fake-cert.jks")
                    ?.readAllBytes()
                    ?: throw IllegalStateException("Unable to find fake-cert.jks")
            FAKE_CERTIFICATE_FILE.writeBytes(resource)
        }
    }
}

public fun main(args: Array<String>) {
    ClientPatcherCommand().main(args)
}
