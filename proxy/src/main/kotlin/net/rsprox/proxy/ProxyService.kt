package net.rsprox.proxy

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.Channel
import net.rsprox.patch.NativeClientType
import net.rsprox.patch.PatchResult
import net.rsprox.patch.native.NativePatchCriteria
import net.rsprox.patch.native.NativePatcher
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.binary.credentials.BinaryCredentials
import net.rsprox.proxy.binary.credentials.BinaryCredentialsStore
import net.rsprox.proxy.bootstrap.BootstrapFactory
import net.rsprox.proxy.config.BINARY_CREDENTIALS_FOLDER
import net.rsprox.proxy.config.BINARY_PATH
import net.rsprox.proxy.config.CACHES_DIRECTORY
import net.rsprox.proxy.config.CLIENTS_DIRECTORY
import net.rsprox.proxy.config.CONFIGURATION_PATH
import net.rsprox.proxy.config.FAKE_CERTIFICATE_FILE
import net.rsprox.proxy.config.FILTERS_DIRECTORY
import net.rsprox.proxy.config.HTTP_SERVER_PORT
import net.rsprox.proxy.config.JavConfig
import net.rsprox.proxy.config.ProxyProperties
import net.rsprox.proxy.config.ProxyProperty.Companion.APP_HEIGHT
import net.rsprox.proxy.config.ProxyProperty.Companion.APP_MAXIMIZED
import net.rsprox.proxy.config.ProxyProperty.Companion.APP_POSITION_X
import net.rsprox.proxy.config.ProxyProperty.Companion.APP_POSITION_Y
import net.rsprox.proxy.config.ProxyProperty.Companion.APP_THEME
import net.rsprox.proxy.config.ProxyProperty.Companion.APP_WIDTH
import net.rsprox.proxy.config.ProxyProperty.Companion.BINARY_WRITE_INTERVAL_SECONDS
import net.rsprox.proxy.config.ProxyProperty.Companion.BIND_TIMEOUT_SECONDS
import net.rsprox.proxy.config.ProxyProperty.Companion.FILTERS_STATUS
import net.rsprox.proxy.config.ProxyProperty.Companion.JAV_CONFIG_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_PORT_MIN
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_REFRESH_SECONDS
import net.rsprox.proxy.config.RUNELITE_LAUNCHER
import net.rsprox.proxy.config.SETTINGS_DIRECTORY
import net.rsprox.proxy.config.SIGN_KEY_DIRECTORY
import net.rsprox.proxy.config.SOCKETS_DIRECTORY
import net.rsprox.proxy.config.TEMP_CLIENTS_DIRECTORY
import net.rsprox.proxy.config.registerConnection
import net.rsprox.proxy.connection.ClientTypeDictionary
import net.rsprox.proxy.connection.ProxyConnectionContainer
import net.rsprox.proxy.downloader.JagexNativeClientDownloader
import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.proxy.futures.asCompletableFuture
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.plugin.PluginLoader
import net.rsprox.proxy.rsa.publicKey
import net.rsprox.proxy.rsa.readOrGenerateRsaKey
import net.rsprox.proxy.settings.DefaultSettingSetStore
import net.rsprox.proxy.util.ClientType
import net.rsprox.proxy.util.ConnectionInfo
import net.rsprox.proxy.util.OperatingSystem
import net.rsprox.proxy.util.ProgressCallback
import net.rsprox.proxy.util.getOperatingSystem
import net.rsprox.proxy.worlds.DynamicWorldListProvider
import net.rsprox.proxy.worlds.World
import net.rsprox.proxy.worlds.WorldListProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.settings.SettingSetStore
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import org.newsclub.net.unix.AFUNIXServerSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.net.URL
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.util.Properties
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.writeBytes
import kotlin.properties.Delegates
import kotlin.system.exitProcess

@Suppress("DuplicatedCode")
public class ProxyService(
    private val allocator: ByteBufAllocator,
) {
    private val pluginLoader: PluginLoader = PluginLoader()
    private lateinit var bootstrapFactory: BootstrapFactory
    private lateinit var serverBootstrap: ServerBootstrap
    private lateinit var httpServerBootstrap: ServerBootstrap
    private lateinit var worldListProvider: WorldListProvider
    public lateinit var operatingSystem: OperatingSystem
        private set
    private lateinit var rsa: RSAPrivateCrtKeyParameters
    public lateinit var filterSetStore: PropertyFilterSetStore
        private set
    public lateinit var settingsStore: SettingSetStore
        private set
    private var properties: ProxyProperties by Delegates.notNull()
    private var availablePort: Int = -1
    private val processes: MutableMap<Int, Process> = mutableMapOf()
    private val connections: ProxyConnectionContainer = ProxyConnectionContainer()
    private lateinit var credentials: BinaryCredentialsStore

    public fun start(progressCallback: ProgressCallback) {
        logger.info { "Starting proxy service" }
        progressCallback.update(0.05, "Proxy", "Creating directories")
        createConfigurationDirectories(CONFIGURATION_PATH)
        createConfigurationDirectories(BINARY_PATH)
        createConfigurationDirectories(CLIENTS_DIRECTORY)
        createConfigurationDirectories(TEMP_CLIENTS_DIRECTORY)
        createConfigurationDirectories(CACHES_DIRECTORY)
        createConfigurationDirectories(FILTERS_DIRECTORY)
        createConfigurationDirectories(SETTINGS_DIRECTORY)
        createConfigurationDirectories(SOCKETS_DIRECTORY)
        createConfigurationDirectories(SIGN_KEY_DIRECTORY)
        createConfigurationDirectories(BINARY_CREDENTIALS_FOLDER)
        progressCallback.update(0.10, "Proxy", "Loading properties")
        loadProperties()
        progressCallback.update(0.15, "Proxy", "Loading Huffman")
        HuffmanProvider.load()
        progressCallback.update(0.20, "Proxy", "Loading RSA")
        this.rsa = loadRsa()
        progressCallback.update(0.25, "Proxy", "Loading property filters")
        this.filterSetStore = DefaultPropertyFilterSetStore.load(FILTERS_DIRECTORY)
        this.settingsStore = DefaultSettingSetStore.load(SETTINGS_DIRECTORY)
        this.availablePort = properties.getProperty(PROXY_PORT_MIN)
        this.bootstrapFactory = BootstrapFactory(allocator, properties)
        progressCallback.update(0.35, "Proxy", "Loading jav config")
        val javConfig = loadJavConfig()
        progressCallback.update(0.40, "Proxy", "Loading world list")
        this.worldListProvider = loadWorldListProvider(javConfig.getWorldListUrl())
        progressCallback.update(0.50, "Proxy", "Replacing codebase")
        val replacementWorld = findCodebaseReplacementWorld(javConfig, worldListProvider)
        progressCallback.update(0.60, "Proxy", "Rebuilding jav config")
        val updatedJavConfig = rebuildJavConfig(javConfig, replacementWorld)
        progressCallback.update(0.65, "Proxy", "Reading binary credentials")
        this.credentials = BinaryCredentialsStore.read()

        this.operatingSystem = getOperatingSystem()
        logger.debug { "Proxy launched on $operatingSystem" }
        if (operatingSystem == OperatingSystem.SOLARIS) {
            throw IllegalStateException("Operating system not supported for native: $operatingSystem")
        }
        progressCallback.update(0.70, "Proxy", "Launching http server")
        launchHttpServer(this.bootstrapFactory, worldListProvider, updatedJavConfig)
        progressCallback.update(0.80, "Proxy", "Deleting temporary files")
        deleteTemporaryClients()
        deleteTemporaryRuneLiteJars()
        progressCallback.update(0.90, "Proxy", "Transferring certificate")
        transferFakeCertificate()
        progressCallback.update(0.95, "Proxy", "Setting up safe shutdown")
        setShutdownHook()
    }

    public fun updateCredentials(
        name: String,
        userId: Long,
        userHash: Long,
    ) {
        this.credentials.append(BinaryCredentials(name, userId, userHash))
    }

    private fun transferFakeCertificate() {
        if (FAKE_CERTIFICATE_FILE.exists(LinkOption.NOFOLLOW_LINKS)) {
            return
        }
        logger.debug { "Copying fake certificate" }
        val resource =
            ProxyService::class.java
                .getResourceAsStream("fake-cert.jks")
                ?.readAllBytes()
                ?: throw IllegalStateException("Unable to find fake-cert.jks")
        FAKE_CERTIFICATE_FILE.writeBytes(resource)
    }

    public fun getAppTheme(): String {
        return properties.getPropertyOrNull(APP_THEME) ?: "RuneLite"
    }

    public fun getAppWidth(): Int {
        return properties.getPropertyOrNull(APP_WIDTH) ?: 800
    }

    public fun getAppHeight(): Int {
        return properties.getPropertyOrNull(APP_HEIGHT) ?: 600
    }

    public fun setAppMaximized(maximized: Boolean) {
        properties.setProperty(APP_MAXIMIZED, maximized)
        properties.saveProperties(PROPERTIES_FILE)
    }

    public fun getAppMaximized(): Boolean? {
        return properties.getPropertyOrNull(APP_MAXIMIZED)
    }

    public fun setFiltersStatus(status: Int) {
        properties.setProperty(FILTERS_STATUS, status)
        properties.saveProperties(PROPERTIES_FILE)
    }

    public fun getFiltersStatus(): Int {
        return properties.getPropertyOrNull(FILTERS_STATUS) ?: 0
    }

    public fun setAppSize(
        width: Int,
        height: Int,
    ) {
        properties.setProperty(APP_WIDTH, width)
        properties.setProperty(APP_HEIGHT, height)
        properties.saveProperties(PROPERTIES_FILE)
    }

    public fun getAppPositionX(): Int? {
        return properties.getPropertyOrNull(APP_POSITION_X)
    }

    public fun getAppPositionY(): Int? {
        return properties.getPropertyOrNull(APP_POSITION_Y)
    }

    public fun setAppPosition(
        x: Int,
        y: Int,
    ) {
        properties.setProperty(APP_POSITION_X, x)
        properties.setProperty(APP_POSITION_Y, y)
        properties.saveProperties(PROPERTIES_FILE)
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

    private fun hasAliveProcesses(): Boolean {
        return processes.values.any { process ->
            process.isAlive
        }
    }

    public fun killAliveProcess(port: Int) {
        val process = processes.remove(port) ?: return
        if (process.isAlive) {
            try {
                for (descendant in process.descendants()) {
                    descendant.destroyForcibly()
                }
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

    private fun killAliveProcesses() {
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
                connection.blob.shutdown()
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

    private fun deleteTemporaryRuneLiteJars() {
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

    public fun launchRuneLiteClient(sessionMonitor: SessionMonitor<BinaryHeader>): Int {
        if (!RUNELITE_LAUNCHER.exists(LinkOption.NOFOLLOW_LINKS)) {
            throw IllegalStateException("RuneLite Launcher jar could not be found in $RUNELITE_LAUNCHER")
        }
        val port = this.availablePort++
        try {
            launchProxyServer(this.bootstrapFactory, this.worldListProvider, rsa, port)
        } catch (t: Throwable) {
            logger.error { "Unable to bind network port $port for native client." }
            return -1
        }
        this.connections.addSessionMonitor(port, sessionMonitor)
        ClientTypeDictionary[port] = "RuneLite (${operatingSystem.shortName})"
        launchJar(
            port,
            RUNELITE_LAUNCHER,
            operatingSystem,
        )
        return port
    }

    public fun launchNativeClient(sessionMonitor: SessionMonitor<BinaryHeader>): Int {
        return launchNativeClient(
            operatingSystem,
            rsa,
            sessionMonitor,
        )
    }

    private fun launchNativeClient(
        os: OperatingSystem,
        rsa: RSAPrivateCrtKeyParameters,
        sessionMonitor: SessionMonitor<BinaryHeader>,
    ): Int {
        val port = this.availablePort++
        try {
            launchProxyServer(this.bootstrapFactory, this.worldListProvider, rsa, port)
        } catch (t: Throwable) {
            logger.error { "Unable to bind network port $port for native client." }
            return -1
        }
        val javConfigEndpoint = properties.getProperty(JAV_CONFIG_ENDPOINT)
        val worldlistEndpoint = properties.getProperty(WORLDLIST_ENDPOINT)
        val nativeClientType =
            when (os) {
                OperatingSystem.WINDOWS, OperatingSystem.UNIX -> NativeClientType.WIN
                OperatingSystem.MAC -> NativeClientType.MAC
                else -> throw IllegalStateException()
            }
        val binary = JagexNativeClientDownloader.download(nativeClientType)
        val extension = if (binary.extension.isNotEmpty()) ".${binary.extension}" else ""
        val stamp = System.currentTimeMillis()
        val patched = TEMP_CLIENTS_DIRECTORY.resolve("${binary.nameWithoutExtension}-$stamp$extension")
        binary.copyTo(patched, overwrite = true)

        // For now, directly just download, patch and launch the C++ client
        val patcher = NativePatcher()
        val criteria =
            NativePatchCriteria
                .Builder(nativeClientType)
                .acceptAllLoopbackAddresses()
                .rsaModulus(rsa.publicKey.modulus.toString(16))
                .javConfig("http://127.0.0.1:$HTTP_SERVER_PORT/$javConfigEndpoint")
                .worldList("http://127.0.0.1:$HTTP_SERVER_PORT/$worldlistEndpoint")
                .port(port)
                .build()
        val result =
            patcher.patch(
                patched,
                criteria,
            )
        check(result is PatchResult.Success) {
            "Failed to patch"
        }
        checkNotNull(result.oldModulus)
        registerConnection(
            ConnectionInfo(
                ClientType.Native,
                os,
                port,
                BigInteger(result.oldModulus, 16),
            ),
        )
        ClientTypeDictionary[port] = "Native (${os.shortName})"
        this.connections.addSessionMonitor(port, sessionMonitor)
        launchExecutable(port, result.outputPath, os)
        return port
    }

    private fun launchJar(
        port: Int,
        path: Path,
        operatingSystem: OperatingSystem,
    ) {
        val timestamp = System.currentTimeMillis()
        val socketFile = SOCKETS_DIRECTORY.resolve("$timestamp.socket").toFile()
        val socket = AFUNIXServerSocket.newInstance()
        logger.debug {
            "Binding an AF UNIX Socket to ${socketFile.name}"
        }
        socket.bind(AFUNIXSocketAddress.of(socketFile))
        try {
            val directory = path.parent.toFile()
            val absolutePath = path.absolutePathString()
            val javConfigEndpoint = properties.getProperty(JAV_CONFIG_ENDPOINT)
            createProcess(
                listOf(
                    "java",
                    "-jar",
                    absolutePath,
                    "--port=$port",
                    "--rsa=${rsa.publicKey.modulus.toString(16)}",
                    "--jav_config=http://127.0.0.1:$HTTP_SERVER_PORT/$javConfigEndpoint",
                    "--socket_id=$timestamp",
                    "--developer-mode",
                ),
                directory,
                path,
                port,
            )
            logger.debug { "Waiting for client to connect to the server socket..." }
            val channel = socket.accept()
            logger.debug { "Client connected to server socket successfully." }
            logger.debug { "Requesting old rsa modulus from the client..." }
            val output = channel.outputStream
            output.write("old-rsa-modulus:".encodeToByteArray())
            output.flush()
            val input = channel.inputStream

            val buf = ByteArray(socket.getReceiveBufferSize())
            val read = input.read(buf)
            val oldModulus = String(buf, 0, read)
            logger.debug { "Old RSA modulus received from the client: ${oldModulus.substring(0, 16)}..." }
            registerConnection(
                ConnectionInfo(
                    ClientType.Native,
                    operatingSystem,
                    port,
                    BigInteger(oldModulus, 16),
                ),
            )
            socket.close()
        } finally {
            socket.close()
            socketFile.delete()
        }
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
                .inheritIO()
                .command(command)
        if (directory != null) {
            builder.directory(directory)
        }
        builder.environment().putAll(
            Properties().let { props ->
                val runeliteCreds =
                    File(System.getProperty("user.home"), ".runelite")
                        .resolve("credentials.properties")
                if (!runeliteCreds.exists()) {
                    logger.info { "(Jagex Account) RuneLite credentials could not be located in: $runeliteCreds" }
                    logger.info { "(Jagex Account) Using regular username/e-mail & password login box" }
                    emptyMap()
                } else {
                    runeliteCreds.inputStream().use {
                        props.load(it)
                    }
                    props.stringPropertyNames().associateWith { props.getProperty(it) }
                }
            },
        )
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
            val changedWorldListUrl = "http://127.0.0.1:$HTTP_SERVER_PORT/worldlist.ws"
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
            val timeoutSeconds = properties.getProperty(BIND_TIMEOUT_SECONDS).toLong()
            httpServerBootstrap
                .bind(HTTP_SERVER_PORT)
                .asCompletableFuture()
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .join()
            this.httpServerBootstrap = httpServerBootstrap
            logger.debug { "HTTP server bound to port $HTTP_SERVER_PORT" }
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
                settingsStore,
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

    public companion object {
        private val logger = InlineLogger()
        private val PROPERTIES_FILE = CONFIGURATION_PATH.resolve("proxy.properties")

        public fun loadJavConfig(): JavConfig {
            val url = "http://oldschool.runescape.com/jav_config.ws"
            return runCatching("Failed to load jav_config.ws from $url") {
                val config = JavConfig(URL(url))
                logger.debug { "Jav config loaded from $url" }
                config
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
    }
}
