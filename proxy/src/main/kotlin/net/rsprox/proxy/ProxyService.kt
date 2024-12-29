package net.rsprox.proxy

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.Channel
import net.rsprox.patch.NativeClientType
import net.rsprox.patch.PatchResult
import net.rsprox.patch.native.NativePatchCriteria
import net.rsprox.patch.native.NativePatcher
import net.rsprox.proxy.accounts.DefaultJagexAccountStore
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.binary.credentials.BinaryCredentials
import net.rsprox.proxy.binary.credentials.BinaryCredentialsStore
import net.rsprox.proxy.bootstrap.BootstrapFactory
import net.rsprox.proxy.config.*
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
import net.rsprox.proxy.config.ProxyProperty.Companion.SELECTED_CLIENT
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_REFRESH_SECONDS
import net.rsprox.proxy.connection.ClientTypeDictionary
import net.rsprox.proxy.connection.ProxyConnectionContainer
import net.rsprox.proxy.downloader.JagexNativeClientDownloader
import net.rsprox.proxy.exceptions.MissingLibraryException
import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.proxy.futures.asCompletableFuture
import net.rsprox.proxy.http.GamePackProvider
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.plugin.DecoderLoader
import net.rsprox.proxy.rsa.publicKey
import net.rsprox.proxy.rsa.readOrGenerateRsaKey
import net.rsprox.proxy.runelite.RuneliteLauncher
import net.rsprox.proxy.settings.DefaultSettingSetStore
import net.rsprox.proxy.util.*
import net.rsprox.proxy.worlds.DynamicWorldListProvider
import net.rsprox.proxy.worlds.World
import net.rsprox.proxy.worlds.WorldListProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.account.JagexAccountStore
import net.rsprox.shared.account.JagexCharacter
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
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.concurrent.thread
import kotlin.io.path.*
import kotlin.properties.Delegates
import kotlin.system.exitProcess

@Suppress("DuplicatedCode")
public class ProxyService(
    private val allocator: ByteBufAllocator,
) {
    private val decoderLoader: DecoderLoader = DecoderLoader()
    private lateinit var bootstrapFactory: BootstrapFactory
    private lateinit var serverBootstrap: ServerBootstrap
    private lateinit var httpServerBootstrap: ServerBootstrap
    private lateinit var worldListProvider: WorldListProvider
    public lateinit var operatingSystem: OperatingSystem
        private set
    private lateinit var rsa: RSAPrivateCrtKeyParameters
    public lateinit var jagexAccountStore: JagexAccountStore
        private set
    public lateinit var filterSetStore: PropertyFilterSetStore
        private set
    public lateinit var settingsStore: SettingSetStore
        private set
    private var properties: ProxyProperties by Delegates.notNull()
    private var availablePort: Int = -1
    private val processes: MutableMap<Int, List<ProcessHandle>> = mutableMapOf()
    private val connections: ProxyConnectionContainer = ProxyConnectionContainer()
    private lateinit var credentials: BinaryCredentialsStore
    private val gamePackProvider: GamePackProvider = GamePackProvider()
    private var rspsModulus: String? = null

    public fun start(
        rspsJavConfigUrl: String?,
        rspsModulus: String?,
        progressCallback: ProgressCallback,
    ) {
        this.rspsModulus = rspsModulus
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
        createConfigurationDirectories(RUNELITE_LAUNCHER_REPO_DIRECTORY)
        progressCallback.update(0.10, "Proxy", "Loading properties")
        loadProperties()
        progressCallback.update(0.15, "Proxy", "Loading Huffman")
        HuffmanProvider.load()
        progressCallback.update(0.20, "Proxy", "Loading RSA")
        this.rsa = loadRsa()
        progressCallback.update(0.25, "Proxy", "Loading jagex accounts")
        this.jagexAccountStore = DefaultJagexAccountStore.load(JAGEX_ACCOUNTS_FILE)
        progressCallback.update(0.30, "Proxy", "Loading property filters")
        this.filterSetStore = DefaultPropertyFilterSetStore.load(FILTERS_DIRECTORY)
        this.settingsStore = DefaultSettingSetStore.load(SETTINGS_DIRECTORY)
        this.availablePort = properties.getProperty(PROXY_PORT_MIN)
        this.bootstrapFactory = BootstrapFactory(allocator, properties)
        progressCallback.update(0.35, "Proxy", "Loading jav config")
        val javConfig = loadJavConfig(rspsJavConfigUrl)
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

    public fun setSelectedClient(index: Int) {
        properties.setProperty(SELECTED_CLIENT, index)
        properties.saveProperties(PROPERTIES_FILE)
    }

    public fun getSelectedClient(): Int {
        return properties.getPropertyOrNull(SELECTED_CLIENT) ?: 0
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
        return processes.values.any { processList ->
            processList.any { it.isAlive }
        }
    }

    public fun killAliveProcess(port: Int) {
        val processList = processes.remove(port) ?: return
        for (process in processList) {
            try {
                kill(process)
            } catch (t: Throwable) {
                logger.error(t) {
                    "Unable to destroy process on port $port: ${process.info()}"
                }
                continue
            }
            logger.info {
                "Destroyed process on port $port: ${process.info()}"
            }
        }
    }

    private fun kill(process: ProcessHandle) {
        for (descendant in process.descendants()) {
            kill(descendant)
        }
        process.destroyForcibly()
    }

    @Suppress("SameParameterValue")
    private fun launchDaemonWatcherThread(
        timeout: Long,
        unit: TimeUnit,
    ) {
        thread(isDaemon = true) {
            Thread.sleep(unit.toMillis(timeout))
            // Print to system.err as logger will not necessarily flush it due to caching
            System.err.println("Process is still alive after $timeout ${unit.name.lowercase()} - forcibly killing it.")
            exitProcess(-1)
        }
    }

    private fun killAliveProcesses() {
        if (processes.isNotEmpty()) {
            // It is possible for the below process to get stuck in a weird state
            // which requires taskkill to be performed. This is not particularly user-friendly,
            // so we shall launch a separate daemon thread to forcibly exit the application after
            // a long enough time period
            launchDaemonWatcherThread(5, TimeUnit.SECONDS)
        }
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
        killAliveProcesses()
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

    public fun launchRuneLiteClient(
        sessionMonitor: SessionMonitor<BinaryHeader>,
        character: JagexCharacter?,
        port: Int,
    ) {
        try {
            launchProxyServer(this.bootstrapFactory, this.worldListProvider, rsa, port)
        } catch (t: Throwable) {
            logger.error(t) { "Unable to bind network port $port for native client." }
            return
        }
        this.connections.addSessionMonitor(port, sessionMonitor)
        ClientTypeDictionary[port] = "RuneLite (${operatingSystem.shortName})"
        launchJavaProcess(
            port,
            operatingSystem,
            character,
        )
    }

    public fun allocatePort(): Int {
        return this.availablePort++
    }

    public fun launchNativeClient(
        sessionMonitor: SessionMonitor<BinaryHeader>,
        character: JagexCharacter?,
        port: Int,
    ) {
        launchNativeClient(
            operatingSystem,
            rsa,
            sessionMonitor,
            character,
            port,
        )
    }

    private fun launchNativeClient(
        os: OperatingSystem,
        rsa: RSAPrivateCrtKeyParameters,
        sessionMonitor: SessionMonitor<BinaryHeader>,
        character: JagexCharacter?,
        port: Int,
    ) {
        try {
            launchProxyServer(this.bootstrapFactory, this.worldListProvider, rsa, port)
        } catch (t: Throwable) {
            logger.error(t) { "Unable to bind network port $port for native client." }
            return
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
                BigInteger(rspsModulus ?: result.oldModulus, 16),
            ),
        )
        ClientTypeDictionary[port] = "Native (${os.shortName})"
        this.connections.addSessionMonitor(port, sessionMonitor)
        launchExecutable(port, result.outputPath, os, character)
    }

    private fun launchJavaProcess(
        port: Int,
        operatingSystem: OperatingSystem,
        character: JagexCharacter?,
    ) {
        val timestamp = System.currentTimeMillis()
        val socketFile = SOCKETS_DIRECTORY.resolve("$timestamp.socket").toFile()
        val socket = AFUNIXServerSocket.newInstance()
        logger.debug {
            "Binding an AF UNIX Socket to ${socketFile.name}"
        }
        socket.bind(AFUNIXSocketAddress.of(socketFile))
        try {
            val javConfigEndpoint = properties.getProperty(JAV_CONFIG_ENDPOINT)
            val launcher = RuneliteLauncher()
            val args = launcher.getLaunchArgs(
                port,
                rsa.publicKey.modulus.toString(16),
                javConfig = "http://127.0.0.1:$HTTP_SERVER_PORT/$javConfigEndpoint",
                socket = timestamp.toString(),
            )

            createProcess(
                args,
                directory = null,
                path = null,
                port = port,
                character,
                operatingSystem,
                ClientType.RuneLite,
            )
            logger.debug { "Waiting for client to connect to the server socket..." }
            gamePackProvider.prefetch()
            val channel = socket.accept()
            logger.debug { "Client connected to server socket successfully." }
            logger.debug { "Requesting old rsa modulus from the client..." }
            val output = channel.outputStream
            output.write("old-rsa-modulus:".encodeToByteArray())
            output.flush()
            val input = channel.inputStream

            val buf = ByteArray(socket.receiveBufferSize)
            val read = input.read(buf)
            val oldModulus = String(buf, 0, read)
            logger.debug { "Old RSA modulus received from the client: ${oldModulus.substring(0, 16)}..." }
            registerConnection(
                ConnectionInfo(
                    ClientType.RuneLite,
                    operatingSystem,
                    port,
                    BigInteger(rspsModulus ?: oldModulus, 16),
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
        character: JagexCharacter?,
    ) {
        when (operatingSystem) {
            OperatingSystem.WINDOWS -> {
                val directory = path.parent.toFile()
                val absolutePath = path.absolutePathString()
                createProcess(
                    listOf(absolutePath),
                    directory,
                    path,
                    port,
                    character,
                    operatingSystem,
                    ClientType.Native,
                )
            }

            OperatingSystem.MAC -> {
                // The patched file is at /.rsprox/clients/osclient.app/Contents/MacOS/osclient-patched
                // We need to however execute the /.rsprox/clients/osclient.app "file"
                val rootDirection = path.parent.parent.parent
                val absolutePath = "${File.separator}${rootDirection.absolutePathString()}"
                createProcess(
                    listOf("open", absolutePath),
                    null,
                    path,
                    port,
                    character,
                    operatingSystem,
                    ClientType.Native,
                )
            }

            OperatingSystem.UNIX -> {
                try {
                    val directory = path.parent.toFile()
                    val absolutePath = path.absolutePathString()
                    createProcess(
                        listOf("wine", absolutePath),
                        directory,
                        path,
                        port,
                        character,
                        operatingSystem,
                        ClientType.Native,
                    )
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
        path: Path?,
        port: Int,
        character: JagexCharacter?,
        operatingSystem: OperatingSystem,
        clientType: ClientType,
    ) {
        logger.debug { "Attempting to create process $command" }
        val builder =
            ProcessBuilder()
                .inheritIO()
                .command(command)
        if (directory != null) {
            builder.directory(directory)
        }
        if (character != null) {
            val account = jagexAccountStore.accounts.firstOrNull { it.characters.contains(character) }
            if (account != null) {
                builder.environment()["JX_CHARACTER_ID"] = character.accountId.toString()
                builder.environment()["JX_SESSION_ID"] = account.sessionId
                builder.environment()["JX_REFRESH_TOKEN"] = ""
                builder.environment()["JX_DISPLAY_NAME"] = character.displayName ?: ""
                builder.environment()["JX_ACCESS_TOKEN"] = ""
            }
        } else {
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
        }
        val process = builder.start()
        // Wait for up to half a second for the process to launch, after which we can determine if it's still alive
        process.waitFor(500, TimeUnit.MILLISECONDS)
        // If the process encountered an error during the launching (e.g. exe couldn't be launched), the failure
        // case will be hit here. The 500 millisecond wait time is a requirement to hit it, otherwise it'll still
        // be alive by the time it hits that.
        if (!process.isAlive) {
            if (operatingSystem == OperatingSystem.WINDOWS && clientType == ClientType.Native) {
                checkVisualCPlusPlusRedistributable()
            }
            throw IllegalStateException("Unable to launch process: $path, error code: ${process.waitFor()}")
        }
        if (path != null) logger.debug { "Successfully launched $path" }
        processes[port] = process.children().collect(Collectors.toList()) + process.toHandle()
    }

    private fun checkVisualCPlusPlusRedistributable() {
        val rootPath = Path(System.getenv("SYSTEMROOT") ?: return)
        val vcomp140 = rootPath.resolve("System32").resolve("vcomp140.dll")
        if (vcomp140.notExists()) {
            throw MissingLibraryException(
                "VCOMP140.dll is missing. " +
                    "Install Visual C++ Redistributable to obtain the necessary libraries via " +
                    "https://www.microsoft.com/en-ca/download/details.aspx?id=48145",
            )
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

    private fun loadJavConfig(customUrl: String?): JavConfig {
        val url = customUrl ?: "http://oldschool.runescape.com/jav_config.ws"
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
                    gamePackProvider,
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
                decoderLoader,
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
