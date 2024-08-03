package net.rsprox.proxy

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.Channel
import net.rsprox.patch.NativeClientType
import net.rsprox.patch.PatchResult
import net.rsprox.patch.native.NativePatcher
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.bootstrap.BootstrapFactory
import net.rsprox.proxy.config.*
import net.rsprox.proxy.config.ProxyProperty.Companion.APP_THEME
import net.rsprox.proxy.config.ProxyProperty.Companion.APP_VERSION
import net.rsprox.proxy.config.ProxyProperty.Companion.BINARY_WRITE_INTERVAL_SECONDS
import net.rsprox.proxy.config.ProxyProperty.Companion.BIND_TIMEOUT_SECONDS
import net.rsprox.proxy.config.ProxyProperty.Companion.JAV_CONFIG_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_PORT_HTTP
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_PORT_MIN
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_REFRESH_SECONDS
import net.rsprox.proxy.connection.ProxyConnectionContainer
import net.rsprox.proxy.downloader.NativeClientDownloader
import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.proxy.futures.asCompletableFuture
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.plugin.PluginLoader
import net.rsprox.proxy.progressbar.ProgressBarNotifier
import net.rsprox.proxy.rsa.publicKey
import net.rsprox.proxy.rsa.readOrGenerateRsaKey
import net.rsprox.proxy.util.ClientType
import net.rsprox.proxy.util.ConnectionInfo
import net.rsprox.proxy.util.OperatingSystem
import net.rsprox.proxy.util.getOperatingSystem
import net.rsprox.proxy.worlds.DynamicWorldListProvider
import net.rsprox.proxy.worlds.World
import net.rsprox.proxy.worlds.WorldListProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.filters.PropertyFilterSetStore
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.properties.Delegates
import kotlin.system.exitProcess

public class ProxyService(
    private val allocator: ByteBufAllocator,
) {
    private val pluginLoader: PluginLoader = PluginLoader()
    private lateinit var bootstrapFactory: BootstrapFactory
    private lateinit var serverBootstrap: ServerBootstrap
    private lateinit var httpServerBootstrap: ServerBootstrap
    private lateinit var worldListProvider: WorldListProvider
    private lateinit var operatingSystem: OperatingSystem
    private lateinit var rsa: RSAPrivateCrtKeyParameters
    public lateinit var filterSetStore: PropertyFilterSetStore
        private set
    private var properties: ProxyProperties by Delegates.notNull()
    private var availablePort: Int = -1
    private val processes: MutableMap<Int, Process> = mutableMapOf()
    private val connections: ProxyConnectionContainer = ProxyConnectionContainer()

    public fun start() {
        logger.info { "Starting proxy service" }
        createConfigurationDirectories(CONFIGURATION_PATH)
        createConfigurationDirectories(BINARY_PATH)
        createConfigurationDirectories(CLIENTS_DIRECTORY)
        createConfigurationDirectories(TEMP_CLIENTS_DIRECTORY)
        createConfigurationDirectories(CACHES_DIRECTORY)
        createConfigurationDirectories(FILTERS_DIRECTORY)
        loadProperties()
        HuffmanProvider.load()
        this.rsa = loadRsa()
        this.filterSetStore = DefaultPropertyFilterSetStore.load(FILTERS_DIRECTORY)
        this.availablePort = properties.getProperty(PROXY_PORT_MIN)
        this.bootstrapFactory = BootstrapFactory(allocator, properties)
        val javConfig = loadJavConfig()
        this.worldListProvider = loadWorldListProvider(javConfig.getWorldListUrl())
        val replacementWorld = findCodebaseReplacementWorld(javConfig, worldListProvider)
        val updatedJavConfig = rebuildJavConfig(javConfig, replacementWorld)

        this.operatingSystem = getOperatingSystem()
        logger.debug { "Proxy launched on $operatingSystem" }
        if (operatingSystem == OperatingSystem.SOLARIS) {
            throw IllegalStateException("Operating system not supported for native: $operatingSystem")
        }

        launchHttpServer(this.bootstrapFactory, worldListProvider, updatedJavConfig)
        deleteTemporaryClients()
        setShutdownHook()
    }

    public fun getAppVersion(): String {
        return properties.getProperty(APP_VERSION);
    }

    public fun getAppTheme(): String {
        return properties.getProperty(APP_THEME);
    }

    public fun setAppTheme(theme: String) {
        properties.setProperty(APP_THEME, theme)
        properties.saveProperties(PROPERTIES_FILE)
    }

    private fun setShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                if (hasAliveProcesses()) {
                    logger.debug {
                        "Unsafe shutdown detected - attempting to shut down gracefully"
                    }
                    try {
                        killAliveProcesses()
                    } finally {
                        safeShutdown()
                    }
                }
            },
        )
    }

    public fun hasAliveProcesses(): Boolean {
        return processes.values.any { process ->
            process.isAlive
        }
    }

    public fun killAliveProcess(port: Int) {
        val process = processes.remove(port) ?: return
        if (process.isAlive) {
            try {
                process.destroyForcibly().waitFor(5, TimeUnit.SECONDS)
            } catch (t: Throwable) {
                logger.error(t) {
                    "Unable to destroy process on port $port: ${process.info()}"
                }
                return
            }
        }
        logger.info {
            "Destroyed process on port $port: ${process.info()}"
        }
    }

    public fun killAliveProcesses() {
        for (port in processes.keys.toSet()) {
            killAliveProcess(port)
        }
    }

    public fun safeShutdown() {
        for (connection in connections.listConnections()) {
            closeActiveChannel(connection.clientChannel)
            closeActiveChannel(connection.serverChannel)
            try {
                connection.blob.close()
            } catch (t: Throwable) {
                logger.error(t) {
                    "Unable to close blob ${connection.blob}"
                }
            }
        }
    }

    private fun closeActiveChannel(channel: Channel) {
        try {
            if (channel.isActive) {
                channel.close()
            }
        } catch (t: Throwable) {
            logger.error(t) {
                "Unable to close channel $channel"
            }
        }
    }

    private fun deleteTemporaryClients() {
        val files =
            TEMP_CLIENTS_DIRECTORY
                .toFile()
                .walkTopDown()
                .filter { it.isFile }
        for (file in files) {
            try {
                file.delete()
            } catch (t: Throwable) {
                // Doesn't really matter, we're just deleting to avoid growing infinitely
                continue
            }
        }
    }

    public fun launchNativeClient(
        progressBarNotifier: ProgressBarNotifier,
        sessionMonitor: SessionMonitor<BinaryHeader>,
    ): Int {
        return launchNativeClient(
            operatingSystem,
            rsa,
            progressBarNotifier,
            sessionMonitor,
        )
    }

    private fun launchNativeClient(
        os: OperatingSystem,
        rsa: RSAPrivateCrtKeyParameters,
        progressBarNotifier: ProgressBarNotifier,
        sessionMonitor: SessionMonitor<BinaryHeader>,
    ): Int {
        val port = this.availablePort++
        progressBarNotifier.update(0, "Binding port $port")
        try {
            launchProxyServer(this.bootstrapFactory, this.worldListProvider, rsa, port)
        } catch (t: Throwable) {
            logger.error { "Unable to bind network port $port for native client." }
            return - 1
        }
        progressBarNotifier.update(5, "Checking native client updates")
        val webPort = properties.getProperty(PROXY_PORT_HTTP)
        val javConfigEndpoint = properties.getProperty(JAV_CONFIG_ENDPOINT)
        val worldlistEndpoint = properties.getProperty(WORLDLIST_ENDPOINT)
        val nativeClientType =
            when (os) {
                OperatingSystem.WINDOWS, OperatingSystem.UNIX -> NativeClientType.WIN
                OperatingSystem.MAC -> NativeClientType.MAC
                else -> throw IllegalStateException()
            }
        val binary = NativeClientDownloader.download(nativeClientType, progressBarNotifier)
        val extension = if (binary.extension.isNotEmpty()) ".${binary.extension}" else ""
        val stamp = System.currentTimeMillis()
        val patched = TEMP_CLIENTS_DIRECTORY.resolve("${binary.nameWithoutExtension}-$stamp$extension")
        progressBarNotifier.update(90, "Cloning native client")
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
        registerConnection(
            ConnectionInfo(
                ClientType.Native,
                os,
                port,
                BigInteger(result.oldModulus, 16),
            ),
        )
        progressBarNotifier.update(100, "Launching native client")
        launchExecutable(port, result.outputPath, os)
        this.connections.addSessionMonitor(port, sessionMonitor)
        return port
    }

    private fun launchExecutable(
        port: Int,
        path: Path,
        operatingSystem: OperatingSystem,
    ) {
        when (operatingSystem) {
            OperatingSystem.WINDOWS -> {
                val directory = path.parent.toFile()
                val absolutePath = path.absolutePathString()
                createProcess(listOf(absolutePath), directory, path, port)
            }
            OperatingSystem.MAC -> {
                // The patched file is at /.rsprox/clients/osclient.app/Contents/MacOS/osclient-patched
                // We need to however execute the /.rsprox/clients/osclient.app "file"
                val rootDirection = path.parent.parent.parent
                val absolutePath = "${File.separator}${rootDirection.absolutePathString()}"
                createProcess(listOf("open", absolutePath), null, path, port)
            }
            OperatingSystem.UNIX -> {
                try {
                    val directory = path.parent.toFile()
                    val absolutePath = path.absolutePathString()
                    createProcess(listOf("wine", absolutePath), directory, path, port)
                } catch (e: IOException) {
                    throw RuntimeException("wine is required to run the enhanced client on unix", e)
                }
            }
            OperatingSystem.SOLARIS -> throw IllegalStateException("Solaris not supported yet.")
        }
    }

    private fun createProcess(
        command: List<String>,
        directory: File?,
        path: Path,
        port: Int,
    ) {
        logger.debug { "Attempting to create process $command" }
        val builder =
            ProcessBuilder()
                .command(command)
        if (directory != null) {
            builder.directory(directory)
        }
        builder.environment().putAll(Properties().let { props ->
            val runeliteCreds = File(System.getProperty("user.home"), ".runelite")
                .resolve("credentials.properties")
            if (!runeliteCreds.exists()) {
                logger.info { "Unable to find RuneLite credentials file: $runeliteCreds" }
                emptyMap()
            } else {
                runeliteCreds.inputStream().use {
                    props.load(it)
                }
                props.stringPropertyNames().associateWith { props.getProperty(it) }
            }
        })
        val process = builder.start()
        if (process.isAlive) {
            logger.debug { "Successfully launched $path" }
            processes[port] = process
        } else {
            logger.warn { "Unable to successfully launch $path" }
        }
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

    private fun loadJavConfig(): JavConfig {
        val url = "https://oldschool.runescape.com/jav_config.ws"
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
            val changedWorldListUrl = "http://127.0.0.1:${properties.getProperty(PROXY_PORT_HTTP)}/worldlist.ws"
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
            val port = properties.getProperty(PROXY_PORT_HTTP)
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
        port: Int,
    ) {
        val serverBootstrap =
            factory.createServerBootStrap(
                worldListProvider,
                rsa,
                pluginLoader,
                properties.getProperty(BINARY_WRITE_INTERVAL_SECONDS),
                connections,
                filterSetStore,
            )
        val timeoutSeconds = properties.getProperty(BIND_TIMEOUT_SECONDS).toLong()
        serverBootstrap
            .bind(port)
            .asCompletableFuture()
            .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .join()
        this.serverBootstrap = serverBootstrap
        logger.debug { "Proxy server bound to port $port" }
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
