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
import net.rsprox.proxy.config.ProxyProperty.Companion.SELECTED_PROXY_TARGET
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_ENDPOINT
import net.rsprox.proxy.connection.ClientTypeDictionary
import net.rsprox.proxy.connection.ProxyConnectionContainer
import net.rsprox.proxy.downloader.JagexNativeClientDownloader
import net.rsprox.proxy.downloader.LostCityNativeClientDownloader
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
import net.rsprox.proxy.target.ProxyTarget
import net.rsprox.proxy.target.ProxyTargetConfig
import net.rsprox.proxy.target.YamlProxyTargetConfig
import net.rsprox.proxy.util.*
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.account.JagexAccountStore
import net.rsprox.shared.account.JagexCharacter
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.shared.symbols.SymbolDictionaryProvider
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
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import kotlin.concurrent.thread
import kotlin.io.path.*
import kotlin.properties.Delegates
import kotlin.system.exitProcess

@Suppress("DuplicatedCode")
public class ProxyService(
    private val allocator: ByteBufAllocator,
) {
    public val decoderLoader: DecoderLoader = DecoderLoader()
    private lateinit var bootstrapFactory: BootstrapFactory
    private lateinit var serverBootstrap: ServerBootstrap
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
    private var rspsModulus: String? = null
    public lateinit var proxyTargets: List<ProxyTarget>
        private set
    private val currentProxyTarget: ProxyTarget
        get() = proxyTargets[getSelectedProxyTarget()]

    public fun start(
        rspsJavConfigUrl: String?,
        rspsModulus: String?,
        progressCallback: ProgressCallback,
    ) {
        this.rspsModulus = rspsModulus
        logger.info { "Starting proxy service" }
        progressCallback.update(0.05, "Proxy", "Loading RSProx (1/15)")
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
        progressCallback.update(0.10, "Proxy", "Loading RSProx (2/15)")
        loadProperties()
        this.availablePort = properties.getProperty(PROXY_PORT_MIN)
        this.bootstrapFactory = BootstrapFactory(allocator, properties)
        progressCallback.update(0.15, "Proxy", "Loading RSProx (3/15)")
        val proxyTargetConfigs = loadProxyTargetConfigs(rspsJavConfigUrl)
        val jobs = mutableListOf<Callable<Boolean>>()
        jobs += createJob(progressCallback) { HuffmanProvider.load() }
        jobs += createJob(progressCallback) { this.rsa = loadRsa() }
        jobs +=
            createJob(progressCallback) { this.jagexAccountStore = DefaultJagexAccountStore.load(JAGEX_ACCOUNTS_FILE) }
        jobs +=
            createJob(progressCallback) { this.filterSetStore = DefaultPropertyFilterSetStore.load(FILTERS_DIRECTORY) }
        jobs += createJob(progressCallback) { this.settingsStore = DefaultSettingSetStore.load(SETTINGS_DIRECTORY) }

        jobs += loadProxyTargets(progressCallback, proxyTargetConfigs)

        jobs += createJob(progressCallback) { this.credentials = BinaryCredentialsStore.read() }

        this.operatingSystem = getOperatingSystem()
        logger.debug { "Proxy launched on $operatingSystem" }
        if (operatingSystem == OperatingSystem.SOLARIS) {
            throw IllegalStateException("Operating system not supported for native: $operatingSystem")
        }
        jobs += createJob(progressCallback) { deleteTemporaryClients() }
        jobs += createJob(progressCallback) { deleteTemporaryRuneLiteJars() }
        jobs += createJob(progressCallback) { transferFakeCertificate() }
        jobs += createJob(progressCallback) { setShutdownHook() }
        totalJobs.set(jobs.size + 3)
        val results = ForkJoinPool.commonPool().invokeAll(jobs)
        check(results.all { it.get() }) {
            "Unable to boot RSProx"
        }
        this.proxyTargets = this.proxyTargets.filter(ProxyTarget::isSuccessfullyLoaded)
    }

    private val completedJobs = AtomicInteger(0)
    private val totalJobs = AtomicInteger(0)

    private inline fun createJob(
        progressCallback: ProgressCallback,
        crossinline block: () -> Unit,
    ): Callable<Boolean> {
        return Callable {
            try {
                block()
                val num = completedJobs.incrementAndGet()
                val percentage = num.toDouble() / totalJobs.get()
                progressCallback.update(
                    0.10 + percentage,
                    "Proxy",
                    "Loading RSProx ($num/${totalJobs.get()})",
                )
                return@Callable true
            } catch (t: Throwable) {
                logger.error(t) {
                    "Unable to load RSProx"
                }
                return@Callable false
            }
        }
    }

    private fun loadProxyTargetConfigs(overriddenJavConfig: String?): List<ProxyTargetConfig> {
        val oldschool =
            ProxyTargetConfig(
                id = 0,
                name = YamlProxyTargetConfig.DEFAULT_NAME,
                javConfigUrl = overriddenJavConfig ?: "http://oldschool.runescape.com/jav_config.ws",
                modulus = null,
                varpCount = YamlProxyTargetConfig.DEFAULT_VARP_COUNT,
                revision = null,
                runeliteBootstrapCommitHash = null,
                runeliteGamepackUrl = null,
            )
        try {
            val path = if (PROXY_TARGETS_FILE.exists()) PROXY_TARGETS_FILE else ALT_PROXY_TARGETS_FILE
            val yamlTargets = YamlProxyTargetConfig.load(path)
            val customTargets =
                yamlTargets.entries.mapIndexedNotNull { index, yaml ->
                    yaml.mapToProxyTargetConfig(index)
                }
            return listOf(oldschool) + customTargets
        } catch (e: Exception) {
            logger.error(e) {
                "Unable to load proxy target configs"
            }
            return listOf(oldschool)
        }
    }

    private fun YamlProxyTargetConfig.mapToProxyTargetConfig(index: Int): ProxyTargetConfig? {
        val config =
            try {
                JavConfig(URL(this.javConfigUrl))
            } catch (e: Exception) {
                logger.error(e) {
                    "Unable to load proxy target ${this.name}"
                }
                return null
            }
        val revision = config.getRevision()
        val commitHash =
            this.runeliteBootstrapCommitHash
                ?: getBootstrapCommitHash(revision)
        val gamepackUrl = this.runeliteGamepackUrl ?: getGamepackUrl(revision)
        val revisionString = this.revision?.substringBefore('.')
        if (revisionString != null) {
            check(revisionString.toInt() == revision) {
                "Revision in jav-config mismatches with supplied revision: $revision vs $revisionString"
            }
        }
        return ProxyTargetConfig(
            id = index + 1,
            name = this.name,
            javConfigUrl = this.javConfigUrl,
            modulus = this.modulus,
            varpCount = this.varpCount,
            revision = this.revision,
            runeliteBootstrapCommitHash = commitHash,
            runeliteGamepackUrl = gamepackUrl,
        )
    }

    private fun getGamepackUrl(revision: Int): String? {
        if (revision > 228) {
            return null
        }
        return "https://github.com/runetech/osrs-gamepacks/raw/refs/heads/master/gamepacks/osrs-$revision.jar"
    }

    private fun getBootstrapCommitHash(revision: Int): String? {
        return when (revision) {
            223 -> "b7c08f2a08be75cfbdb3a11b870b5a82c480267f"
            224 -> "94578497efe13939b032f161d4a0d146b2123d01"
            225 -> "84c5b3531c55657fdb66a90da7c6a723236cf32e"
            226 -> "73cc7fff3224e5abdba9f3594f39899fdfdff4b2"
            227 -> "96ae421d77c3e967faf5758b446d274770a9b453"
            228 -> "dc197f1c305c712fcf496d8a2c3c0d02f3824d18"
            229 -> "793a9df1ed8cdef5d6a324aeec0629fa0346d32b"
            230 -> "34a480a260a68aaeb8d505b8c2cf17d8fbed9c30"
            231 -> "8d2e0c60ecec85cffd7a84196aabf4effde55132"
            else -> null
        }
    }

    private fun loadProxyTargets(
        progressCallback: ProgressCallback,
        configs: List<ProxyTargetConfig>,
    ): List<Callable<Boolean>> {
        this.proxyTargets =
            configs.map {
                ProxyTarget(
                    it,
                    GamePackProvider(it.runeliteGamepackUrl),
                )
            }
        return this.proxyTargets.map { target ->
            createJob(progressCallback) {
                try {
                    target.load(properties, bootstrapFactory)
                } catch (e: Exception) {
                    logger.error(e) {
                        "Unable to load proxy target ${target.config.name}."
                    }
                }
            }
        }
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

    public fun getSelectedProxyTarget(): Int {
        val lastSelected = properties.getPropertyOrNull(SELECTED_PROXY_TARGET) ?: 0
        if (lastSelected in proxyTargets.indices) {
            return lastSelected
        }
        return 0
    }

    public fun setSelectedProxyTarget(index: Int) {
        properties.setProperty(SELECTED_PROXY_TARGET, index)
        properties.saveProperties(PROPERTIES_FILE)
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
        SymbolDictionaryProvider.stop()
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
        val target = this.currentProxyTarget
        try {
            launchProxyServer(this.bootstrapFactory, target, rsa, port)
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
            target,
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
        val target = this.currentProxyTarget
        try {
            launchProxyServer(this.bootstrapFactory, target, rsa, port)
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
        val targetRev = target.config.revision
        val binary =
            if (targetRev == null) {
                JagexNativeClientDownloader.download(nativeClientType)
            } else {
                getHistoricNativeClient(targetRev, nativeClientType)
            }
        val extension = if (binary.extension.isNotEmpty()) ".${binary.extension}" else ""
        val stamp = System.currentTimeMillis()
        val patched = TEMP_CLIENTS_DIRECTORY.resolve("${binary.nameWithoutExtension}-$stamp$extension")
        binary.copyTo(patched, overwrite = true)

        // For now, directly just download, patch and launch the C++ client
        val patcher = NativePatcher()
        val criteriaBuilder =
            NativePatchCriteria
                .Builder(nativeClientType)
                .acceptAllLoopbackAddresses()
                .rsaModulus(rsa.publicKey.modulus.toString(16))
                .javConfig("http://127.0.0.1:${target.config.httpPort}/$javConfigEndpoint")
                .worldList("http://127.0.0.1:${target.config.httpPort}/$worldlistEndpoint")
                .port(port)
        if (target.config.varpCount != YamlProxyTargetConfig.DEFAULT_VARP_COUNT) {
            criteriaBuilder.varpCount(YamlProxyTargetConfig.DEFAULT_VARP_COUNT, target.config.varpCount)
        }
        if (target.config.name != YamlProxyTargetConfig.DEFAULT_NAME) {
            criteriaBuilder.name(target.config.name)
        }
        val criteria = criteriaBuilder.build()
        val result =
            patcher.patch(
                patched,
                criteria,
            )
        check(result is PatchResult.Success) {
            "Failed to patch"
        }
        checkNotNull(result.oldModulus)
        val targetModulus =
            target.config.modulus
                ?: rspsModulus
                ?: result.oldModulus
        registerConnection(
            ConnectionInfo(
                ClientType.Native,
                os,
                port,
                BigInteger(targetModulus, 16),
            ),
        )
        ClientTypeDictionary[port] = "Native (${os.shortName})"
        this.connections.addSessionMonitor(port, sessionMonitor)
        launchExecutable(port, result.outputPath, os, character)
    }

    private fun getHistoricNativeClient(
        version: String,
        type: NativeClientType,
    ): Path {
        return LostCityNativeClientDownloader.download(
            CLIENTS_DIRECTORY,
            type,
            version,
        )
    }

    private fun launchJavaProcess(
        port: Int,
        operatingSystem: OperatingSystem,
        character: JagexCharacter?,
        target: ProxyTarget,
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
            val args =
                launcher.getLaunchArgs(
                    port,
                    rsa.publicKey.modulus.toString(16),
                    javConfig = "http://127.0.0.1:${target.config.httpPort}/$javConfigEndpoint",
                    socket = timestamp.toString(),
                    target = target,
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
            if (target.gamePackProvider.gamepackUrl != null) {
                logger.debug { "Prefetching gamepack from ${target.gamePackProvider.gamepackUrl}" }
                target.gamePackProvider.prefetch()
            } else {
                logger.debug { "Skipping gamepack prefetching" }
            }
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
            val targetModulus =
                target.config.modulus
                    ?: rspsModulus
                    ?: oldModulus
            registerConnection(
                ConnectionInfo(
                    ClientType.RuneLite,
                    operatingSystem,
                    port,
                    BigInteger(targetModulus, 16),
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

                    val protonFilePath = CONFIGURATION_PATH.absolutePathString() + "/protonpath"
                    val protonFile = Path(protonFilePath)

                    if (protonFile.exists()) {
                        createProcess(
                            listOf(protonFile.readText().trim(), "run", absolutePath),
                            directory,
                            path,
                            port,
                            character,
                            operatingSystem,
                            ClientType.Native,
                            true,
                        )
                    } else {
                        createProcess(
                            listOf("wine", absolutePath),
                            directory,
                            path,
                            port,
                            character,
                            operatingSystem,
                            ClientType.Native,
                        )
                    }
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
        proton: Boolean = false,
    ) {
        logger.debug { "Attempting to create process $command" }
        val builder =
            ProcessBuilder()
                .inheritIO()
                .command(command)
        if (directory != null) {
            builder.directory(directory)
        }
        if (proton) {
            val pfxFolder = CONFIGURATION_PATH.absolutePathString() + "/proton_pfx"
            val pfxPath = Path(pfxFolder)
            if (pfxPath.notExists()) pfxPath.createDirectory()
            // half sure steam doesn't even work properly if in any other loc so kind of safe to hardcode
            builder.environment()["STEAM_COMPAT_CLIENT_INSTALL_PATH"] =
                System.getProperty("user.home ") + "/.steam/steam"
            builder.environment()["STEAM_COMPAT_DATA_PATH"] = pfxFolder
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

    private fun launchProxyServer(
        factory: BootstrapFactory,
        target: ProxyTarget,
        rsa: RSAPrivateCrtKeyParameters,
        port: Int,
    ) {
        val serverBootstrap =
            factory.createServerBootStrap(
                target,
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
        private val PROXY_TARGETS_FILE = CONFIGURATION_PATH.resolve("proxy-targets.yaml")
        private val ALT_PROXY_TARGETS_FILE = CONFIGURATION_PATH.resolve("proxy-targets.yml")
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
