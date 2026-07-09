@file:Suppress("DuplicatedCode")

package net.rsprox.proxy.target

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate
import net.rsprox.proxy.bootstrap.BootstrapFactory
import net.rsprox.proxy.channel.getPort
import net.rsprox.proxy.config.CURRENT_REVISION
import net.rsprox.proxy.config.HTTP_SERVER_PORT
import net.rsprox.proxy.config.JavConfig
import net.rsprox.proxy.config.ProxyProperties
import net.rsprox.proxy.config.ProxyProperty
import net.rsprox.proxy.config.SIGN_KEY_DIRECTORY
import net.rsprox.proxy.connection.ClientTypeDictionary
import net.rsprox.proxy.futures.asCompletableFuture
import net.rsprox.proxy.http.GamePackProvider
import net.rsprox.proxy.http.REPLAY_WORLDLIST_ENDPOINT
import net.rsprox.proxy.worlds.DynamicWorldListProvider
import net.rsprox.proxy.worlds.StaticWorldListProvider
import net.rsprox.proxy.worlds.World
import net.rsprox.proxy.worlds.WorldList
import net.rsprox.proxy.worlds.WorldListProvider
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

public class ProxyTarget(
    public val config: ProxyTargetConfig,
    public val gamePackProvider: GamePackProvider,
    public val sessionId: Int,
    private val rewriteJagexAuth: Boolean = false,
) {
    public val httpPort: Int
        get() = HTTP_SERVER_PORT + sessionId
    public val replayAuthPort: Int
        get() = HTTP_SERVER_PORT + REPLAY_AUTH_PORT_OFFSET + sessionId
    public var replayAuthTrustStore: Path? = null
        private set
    public var replayAuthTrustStorePassword: String? = null
        private set
    private val name: String
        get() = config.name
    private lateinit var httpServerBootstrap: ServerBootstrap
    private lateinit var replayAuthServerBootstrap: ServerBootstrap
    private var replayAuthCertificate: SelfSignedCertificate? = null
    public lateinit var worldListProvider: WorldListProvider
        private set

    public fun isSuccessfullyLoaded(): Boolean {
        return this::httpServerBootstrap.isInitialized
    }

    public fun revisionNum(channel: Channel): Int {
        if (config.revision == "latest_supported") {
            return CURRENT_REVISION
        }
        val overriddenRevision =
            config.revision
                ?.split(".")
                ?.firstOrNull()
                ?.toIntOrNull()
        if (overriddenRevision != null) return overriddenRevision
        // RuneLite is detached from jav config and needs hard-coding
        val runelite = ClientTypeDictionary[channel.getPort()].startsWith("RuneLite")
        if (runelite) return CURRENT_REVISION
        return javConfigVersion()
    }

    public fun javConfigVersion(): Int {
        return loadJavConfig(config.javConfigUrl).getRevision()
    }

    public fun load(
        properties: ProxyProperties,
        bootstrapFactory: BootstrapFactory,
    ) {
        val javConfig = loadJavConfig(config.javConfigUrl)
        this.worldListProvider =
            if (rewriteJagexAuth) {
                loadReplayWorldListProvider()
            } else {
                loadWorldListProvider(properties, javConfig.getWorldListUrl())
            }
        val replacementWorld =
            if (rewriteJagexAuth) {
                worldListProvider.get().single()
            } else {
                findCodebaseReplacementWorld(javConfig, worldListProvider)
            }
        val updatedJavConfig = rebuildJavConfig(javConfig, replacementWorld)
        launchHttpServer(
            properties,
            bootstrapFactory,
            worldListProvider,
            updatedJavConfig,
            gamePackProvider,
        )
        if (rewriteJagexAuth) {
            launchReplayAuthHttpsServer(
                properties,
                bootstrapFactory,
                worldListProvider,
                updatedJavConfig,
                gamePackProvider,
            )
        }
    }

    private fun loadJavConfig(url: String): JavConfig {
        val config = JavConfig(URL(url))
        logger.debug { "Jav config loaded from $url for target '$name'" }
        return config
    }

    private fun loadWorldListProvider(
        properties: ProxyProperties,
        url: String,
    ): WorldListProvider {
        val provider =
            DynamicWorldListProvider(
                config,
                URL(url),
                properties.getProperty(ProxyProperty.WORLDLIST_REFRESH_SECONDS),
            )
        logger.debug { "World list provider loaded from $url for target '$name'" }
        return provider
    }

    private fun loadReplayWorldListProvider(): WorldListProvider {
        val world =
            World(
                proxyTargetConfig = config,
                id = REPLAY_WORLD_ID,
                properties = 0,
                population = 0,
                location = 0,
                host = REPLAY_WORLD_HOST,
                activity = REPLAY_WORLD_ACTIVITY,
            )
        logger.debug { "Replay world list provider loaded with world ${world.localHostAddress} for target '$name'" }
        return StaticWorldListProvider(WorldList(listOf(world)))
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
        val world = checkNotNull(worldListProvider.get().getTargetWorld(address))
        logger.debug { "Loaded initial world ${world.localHostAddress} <-> ${world.host} for target '$name'" }
        return world
    }

    private fun rebuildJavConfig(
        javConfig: JavConfig,
        replacementWorld: World,
    ): JavConfig {
        val oldWorldList = javConfig.getWorldListUrl()
        val oldCodebase = javConfig.getCodebase()
        val worldListEndpoint =
            if (rewriteJagexAuth) {
                REPLAY_WORLDLIST_ENDPOINT
            } else {
                "worldlist.ws"
            }
        val changedWorldListUrl = "http://127.0.0.1:$httpPort/$worldListEndpoint"
        val changedCodebase = "http://${replacementWorld.localHostAddress}/"
        var updated =
            javConfig
                .replaceWorldListUrl(changedWorldListUrl)
                .replaceCodebase(changedCodebase)
        if (rewriteJagexAuth) {
            val changedAuth = "https://localhost:$replayAuthPort/"
            updated =
                updated
                    .replaceInitialWorld(REPLAY_WORLD_ID)
                    .replaceJagexAuthUrl(changedAuth)
                    .replaceRuneScapeAuthUrl(changedAuth)
                    .replaceModeWhat(MODEWHAT_LOCAL)
            logger.debug { "Auth endpoints changed to '$changedAuth' for replay target '$name'" }
        }
        logger.debug { "Rebuilt jav_config.ws for target '$name':" }
        logger.debug { "Codebase changed from '$oldCodebase' to '$changedCodebase'" }
        logger.debug { "Worldlist changed from '$oldWorldList' to '$changedWorldListUrl'" }
        return updated
    }

    private fun launchHttpServer(
        properties: ProxyProperties,
        factory: BootstrapFactory,
        worldListProvider: WorldListProvider,
        javConfig: JavConfig,
        gamePackProvider: GamePackProvider,
    ) {
        val httpServerBootstrap =
            factory.createWorldListHttpServer(
                worldListProvider,
                javConfig,
                gamePackProvider,
            )
        val timeoutSeconds = properties.getProperty(ProxyProperty.BIND_TIMEOUT_SECONDS).toLong()
        if (rewriteJagexAuth) {
            closeReplayHttpChannel()
        }
        val channel =
            httpServerBootstrap
                .bind(httpPort)
                .also { future ->
                    future
                        .asCompletableFuture()
                        .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                        .join()
                }.channel()
        if (rewriteJagexAuth) {
            replayHttpServerChannel = channel
        }
        this.httpServerBootstrap = httpServerBootstrap
        logger.debug { "HTTP server bound to port $httpPort for target '$name'" }
    }

    private fun launchReplayAuthHttpsServer(
        properties: ProxyProperties,
        factory: BootstrapFactory,
        worldListProvider: WorldListProvider,
        javConfig: JavConfig,
        gamePackProvider: GamePackProvider,
    ) {
        val certificate = SelfSignedCertificate(REPLAY_AUTH_HOST)
        replayAuthCertificate = certificate
        replayAuthTrustStore = writeReplayAuthTrustStore(certificate.cert())
        replayAuthTrustStorePassword = REPLAY_AUTH_TRUSTSTORE_PASSWORD
        val sslContext =
            SslContextBuilder
                .forServer(certificate.certificate(), certificate.privateKey())
                .build()
        val authServerBootstrap =
            factory.createReplayAuthHttpsServer(
                sslContext,
                worldListProvider,
                javConfig,
                gamePackProvider,
            )
        val timeoutSeconds = properties.getProperty(ProxyProperty.BIND_TIMEOUT_SECONDS).toLong()
        closeReplayAuthChannel()
        replayAuthServerChannel =
            authServerBootstrap
                .bind(replayAuthPort)
                .also { future ->
                    future
                        .asCompletableFuture()
                        .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                        .join()
                }.channel()

        this.replayAuthServerBootstrap = authServerBootstrap
        logger.debug { "Replay auth HTTPS server bound to port $replayAuthPort for target '$name'" }
    }

    private fun writeReplayAuthTrustStore(certificate: X509Certificate): Path {
        Files.createDirectories(SIGN_KEY_DIRECTORY)
        val path = SIGN_KEY_DIRECTORY.resolve(REPLAY_AUTH_TRUSTSTORE_FILE)
        val password = REPLAY_AUTH_TRUSTSTORE_PASSWORD.toCharArray()
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        val defaultTrustStore =
            Path.of(
                System.getProperty("java.home"),
                "lib",
                "security",
                "cacerts",
            )
        if (Files.exists(defaultTrustStore)) {
            Files.newInputStream(defaultTrustStore).use { input ->
                keyStore.load(input, DEFAULT_TRUSTSTORE_PASSWORD.toCharArray())
            }
        } else {
            keyStore.load(null, password)
        }
        keyStore.setCertificateEntry(REPLAY_AUTH_TRUSTSTORE_ALIAS, certificate)
        Files.newOutputStream(path).use { output ->
            keyStore.store(output, password)
        }
        return path
    }

    public companion object {
        private val logger = InlineLogger()
        private const val REPLAY_AUTH_HOST = "localhost"
        private const val REPLAY_AUTH_PORT_OFFSET = 1_000
        public const val REPLAY_WORLD_ID: Int = 1_000
        private const val MODEWHAT_LOCAL: Int = 4
        private const val REPLAY_WORLD_HOST = "replay.rsprox.local"
        private const val REPLAY_WORLD_ACTIVITY = "Replay"
        private const val REPLAY_AUTH_TRUSTSTORE_FILE = "replay-auth-truststore.jks"
        private const val REPLAY_AUTH_TRUSTSTORE_ALIAS = "rsprox-replay-auth"
        private const val REPLAY_AUTH_TRUSTSTORE_PASSWORD = "changeit"
        private const val DEFAULT_TRUSTSTORE_PASSWORD = "changeit"

        private lateinit var replayHttpServerChannel: Channel
        private lateinit var replayAuthServerChannel: Channel

        private fun closeReplayHttpChannel() {
            if (::replayHttpServerChannel.isInitialized) {
                replayHttpServerChannel
                    .close()
                    .asCompletableFuture()
                    .join()
            }
        }

        private fun closeReplayAuthChannel() {
            if (::replayAuthServerChannel.isInitialized) {
                replayAuthServerChannel
                    .close()
                    .asCompletableFuture()
                    .join()
            }
        }
    }
}
