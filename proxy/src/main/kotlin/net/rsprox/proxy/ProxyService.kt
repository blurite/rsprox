package net.rsprox.proxy

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.UnpooledByteBufAllocator
import net.rsprox.patch.NativeClientType
import net.rsprox.patch.PatchResult
import net.rsprox.patch.native.NativePatcher
import net.rsprox.proxy.bootstrap.BootstrapFactory
import net.rsprox.proxy.config.BINARY_PATH
import net.rsprox.proxy.config.CLIENTS_DIRECTORY
import net.rsprox.proxy.config.CONFIGURATION_PATH
import net.rsprox.proxy.config.JavConfig
import net.rsprox.proxy.config.ProxyProperties
import net.rsprox.proxy.config.ProxyProperty.Companion.BINARY_WRITE_INTERVAL_SECONDS
import net.rsprox.proxy.config.ProxyProperty.Companion.BIND_TIMEOUT_SECONDS
import net.rsprox.proxy.config.ProxyProperty.Companion.JAV_CONFIG_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_HTTP_PORT
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_PORT
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_REFRESH_SECONDS
import net.rsprox.proxy.config.patchedRsaModulus
import net.rsprox.proxy.downloader.NativeClientDownloader
import net.rsprox.proxy.futures.asCompletableFuture
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.plugin.PluginLoader
import net.rsprox.proxy.rsa.publicKey
import net.rsprox.proxy.rsa.readOrGenerateRsaKey
import net.rsprox.proxy.util.OperatingSystem
import net.rsprox.proxy.util.getOperatingSystem
import net.rsprox.proxy.worlds.DynamicWorldListProvider
import net.rsprox.proxy.worlds.World
import net.rsprox.proxy.worlds.WorldListProvider
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.io.File
import java.math.BigInteger
import java.net.URL
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.properties.Delegates
import kotlin.system.exitProcess
import kotlin.time.measureTime

public class ProxyService(
    private val allocator: ByteBufAllocator,
) {
    private val pluginLoader: PluginLoader = PluginLoader()
    private lateinit var serverBootstrap: ServerBootstrap
    private lateinit var httpServerBootstrap: ServerBootstrap
    private var properties: ProxyProperties by Delegates.notNull()

    public fun start() {
        logger.info { "Starting proxy service" }
        createConfigurationDirectories(CONFIGURATION_PATH)
        createConfigurationDirectories(BINARY_PATH)
        createConfigurationDirectories(CLIENTS_DIRECTORY)
        loadProperties()
        HuffmanProvider.load()
        val preferredCppWorld = parsePreferredCppWorld()
        val rsa = loadRsa()
        val factory = BootstrapFactory(allocator, properties)
        val javConfig = loadJavConfig(preferredCppWorld)
        val worldListProvider = loadWorldListProvider(javConfig.getWorldListUrl())
        val replacementWorld = findCodebaseReplacementWorld(javConfig, worldListProvider)
        val updatedJavConfig = rebuildJavConfig(javConfig, replacementWorld)

        val os = getOperatingSystem()
        logger.debug { "Proxy launched on $os" }
        if (os == OperatingSystem.UNIX || os == OperatingSystem.SOLARIS) {
            throw IllegalStateException("Operating system not supported for native: $os")
        }

        launchHttpServer(factory, worldListProvider, updatedJavConfig)
        launchProxyServer(factory, worldListProvider, rsa)

        // Only load osrs plugins for now, we don't currently intend on supporting other types
        pluginLoader.loadPlugins("osrs")

        // Immediately launch the corresponding native client
        launchNativeClient(os, rsa)
    }

    private fun launchNativeClient(
        os: OperatingSystem,
        rsa: RSAPrivateCrtKeyParameters,
    ) {
        val port = properties.getProperty(PROXY_PORT)
        val webPort = properties.getProperty(PROXY_HTTP_PORT)
        val javConfigEndpoint = properties.getProperty(JAV_CONFIG_ENDPOINT)
        val worldlistEndpoint = properties.getProperty(WORLDLIST_ENDPOINT)
        val nativeClientType =
            when (os) {
                OperatingSystem.WINDOWS -> NativeClientType.WIN
                OperatingSystem.MAC -> NativeClientType.MAC
                else -> throw IllegalStateException()
            }
        val binary = NativeClientDownloader.download(nativeClientType)
        val extension = if (binary.extension.isNotEmpty()) ".${binary.extension}" else ""
        val patched = binary.parent.resolve("${binary.nameWithoutExtension}-patched$extension")
        binary.copyTo(patched, overwrite = true)

        // For now, directly just download, patch and launch the C++ client
        val patcher = NativePatcher()
        val result =
            patcher.patch(
                patched,
                rsa.publicKey.modulus.toString(16),
                "http://127.0.0.1:$webPort/$javConfigEndpoint",
                "http://127.0.0.1:$webPort/$worldlistEndpoint",
                port,
                nativeClientType,
            )
        check(result is PatchResult.Success) {
            "Failed to patch"
        }
        val originalModulus = BigInteger(result.oldModulus, 16)
        val patchedModulus = patchedRsaModulus
        if (patchedModulus != null) {
            check(patchedModulus == originalModulus) {
                "Unable to launch client due to different modulus being used. Reboot proxy."
            }
        }
        patchedRsaModulus = originalModulus
        launchExecutable(result.outputPath, os)
    }

    private fun launchExecutable(
        path: Path,
        operatingSystem: OperatingSystem,
    ) {
        when (operatingSystem) {
            OperatingSystem.WINDOWS -> {
                val directory = path.parent.toFile()
                val absolutePath = path.absolutePathString()
                Runtime.getRuntime().exec(absolutePath, null, directory)
                logger.debug { "Launched $path" }
            }
            OperatingSystem.MAC -> {
                // The patched file is at /.rsprox/clients/osclient.app/Contents/MacOS/osclient-patched
                // We need to however execute the /.rsprox/clients/osclient.app "file"
                val rootDirection = path.parent.parent.parent
                val absolutePath = "${File.separator}${rootDirection.absolutePathString()}"
                logger.debug { "Attempting to execute command 'open $absolutePath'" }
                Runtime.getRuntime().exec("open $absolutePath")
                logger.debug { "Launched $path" }
            }
            OperatingSystem.UNIX -> throw IllegalStateException("Unix not supported yet.")
            OperatingSystem.SOLARIS -> throw IllegalStateException("Solaris not supported yet.")
        }
    }

    private fun parsePreferredCppWorld(): Int? {
        val path =
            Path(
                System.getProperty("user.home"),
                "AppData",
                "Local",
                "Jagex",
                "Old School Runescape",
                "preferences_client.dat",
            )
        if (!path.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
            return null
        }
        val text = path.readText(Charsets.UTF_8)
        val preferredWorld =
            text
                .lineSequence()
                .firstOrNull { line -> line.startsWith("LastWorldId ") }
                ?.substring(12)
                ?.toIntOrNull()
        if (preferredWorld != null) {
            logger.debug { "Loaded preferred C++ world: $preferredWorld" }
        }
        return preferredWorld
    }

    private fun createConfigurationDirectories(path: Path) {
        runCatching("Unable to create configuration directory: $path") {
            Files.createDirectories(path)
        }
    }

    private fun loadProperties() {
        runCatching("Unable to load proxy properties") {
            this.properties = ProxyProperties(PROPERTIES_FILE)
            logger.debug { "Loaded proxy properties:" }
            for ((key, value) in this.properties.entryPairList()) {
                logger.debug { "$key=$value" }
            }
        }
    }

    private fun loadRsa(): RSAPrivateCrtKeyParameters {
        return runCatching("Unable to load or generate RSA parameters") {
            readOrGenerateRsaKey()
        }
    }

    private fun loadJavConfig(preferredWorldId: Int?): JavConfig {
        val url =
            if (preferredWorldId == null) {
                "https://oldschool.runescape.com/jav_config.ws"
            } else {
                "https://oldschool${preferredWorldId - 300}.runescape.com/jav_config.ws"
            }
        return runCatching("Failed to load jav_config.ws from $url") {
            val config = JavConfig(URL(url))
            logger.debug { "Jav config loaded from $url" }
            config
        }
    }

    private fun loadWorldListProvider(url: String): WorldListProvider {
        return runCatching("Failed to instantiate world list provider") {
            val provider =
                DynamicWorldListProvider(
                    URL(url),
                    properties.getProperty(WORLDLIST_REFRESH_SECONDS),
                )
            logger.debug { "World list provider loaded from $url" }
            provider
        }
    }

    private fun findCodebaseReplacementWorld(
        javConfig: JavConfig,
        worldListProvider: WorldListProvider,
    ): World {
        val address =
            javConfig
                .getCodebase()
                .removePrefix("http://")
                .removePrefix("https://")
                .removeSuffix("/")
        return runCatching("Failed to find a linked world for codebase '$address'") {
            val world = checkNotNull(worldListProvider.get().getTargetWorld(address))
            logger.debug { "Loaded initial world ${world.localHostAddress} <-> ${world.host}" }
            world
        }
    }

    private fun rebuildJavConfig(
        javConfig: JavConfig,
        replacementWorld: World,
    ): JavConfig {
        return runCatching("Failed to rebuild jav_config.ws") {
            val oldWorldList = javConfig.getWorldListUrl()
            val oldCodebase = javConfig.getCodebase()
            val changedWorldListUrl = "http://127.0.0.1:${properties.getProperty(PROXY_HTTP_PORT)}/worldlist.ws"
            val changedCodebase = "http://${replacementWorld.localHostAddress}/"
            val updated =
                javConfig
                    .replaceWorldListUrl(changedWorldListUrl)
                    .replaceCodebase(changedCodebase)
            logger.debug { "Rebuilt jav_config.ws:" }
            logger.debug { "Codebase changed from '$oldCodebase' to '$changedCodebase'" }
            logger.debug { "Worldlist changed from '$oldWorldList' to '$changedWorldListUrl'" }
            updated
        }
    }

    private fun launchHttpServer(
        factory: BootstrapFactory,
        worldListProvider: WorldListProvider,
        javConfig: JavConfig,
    ) {
        runCatching("Failure to launch HTTP server") {
            val httpServerBootstrap =
                factory.createWorldListHttpServer(
                    worldListProvider,
                    javConfig,
                )
            val port = properties.getProperty(PROXY_HTTP_PORT)
            val timeoutSeconds = properties.getProperty(BIND_TIMEOUT_SECONDS).toLong()
            httpServerBootstrap
                .bind(port)
                .asCompletableFuture()
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .join()
            this.httpServerBootstrap = httpServerBootstrap
            logger.debug { "HTTP server bound to port $port" }
        }
    }

    private fun launchProxyServer(
        factory: BootstrapFactory,
        worldListProvider: WorldListProvider,
        rsa: RSAPrivateCrtKeyParameters,
    ) {
        runCatching("Failure to launch HTTP server") {
            val serverBootstrap =
                factory.createServerBootStrap(
                    worldListProvider,
                    rsa,
                    properties.getProperty(BINARY_WRITE_INTERVAL_SECONDS),
                )
            val port = properties.getProperty(PROXY_PORT)
            val timeoutSeconds = properties.getProperty(BIND_TIMEOUT_SECONDS).toLong()
            serverBootstrap
                .bind(port)
                .asCompletableFuture()
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .join()
            this.serverBootstrap = serverBootstrap
            logger.debug { "Proxy server bound to port $port" }
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
        private val PROPERTIES_FILE = CONFIGURATION_PATH.resolve("proxy.properties")
    }
}

public fun main() {
    val logger = InlineLogger("ProxyService")
    val service = ProxyService(UnpooledByteBufAllocator.DEFAULT)
    val time = measureTime(service::start)
    logger.info { "Proxy service started in $time" }
}
