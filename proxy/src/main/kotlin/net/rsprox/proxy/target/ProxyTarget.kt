package net.rsprox.proxy.target

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import net.rsprox.proxy.bootstrap.BootstrapFactory
import net.rsprox.proxy.channel.getPort
import net.rsprox.proxy.config.CURRENT_REVISION
import net.rsprox.proxy.config.HTTP_SERVER_PORT
import net.rsprox.proxy.config.JavConfig
import net.rsprox.proxy.config.ProxyProperties
import net.rsprox.proxy.config.ProxyProperty
import net.rsprox.proxy.connection.ClientTypeDictionary
import net.rsprox.proxy.futures.asCompletableFuture
import net.rsprox.proxy.http.GamePackProvider
import net.rsprox.proxy.worlds.DynamicWorldListProvider
import net.rsprox.proxy.worlds.World
import net.rsprox.proxy.worlds.WorldListProvider
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

public class ProxyTarget(
    public val config: ProxyTargetConfig,
    public val gamePackProvider: GamePackProvider,
    public val sessionId: Int,
) {
    public val httpPort: Int
        get() = HTTP_SERVER_PORT + sessionId
    private val name: String
        get() = config.name
    private lateinit var httpServerBootstrap: ServerBootstrap
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
        this.worldListProvider = loadWorldListProvider(properties, javConfig.getWorldListUrl())
        val replacementWorld = findCodebaseReplacementWorld(javConfig, worldListProvider)
        val updatedJavConfig = rebuildJavConfig(javConfig, replacementWorld)
        launchHttpServer(
            properties,
            bootstrapFactory,
            worldListProvider,
            updatedJavConfig,
            gamePackProvider,
        )
    }

    private fun loadJavConfig(url: String): JavConfig {
        return runCatching("Failed to load jav_config.ws from $url for target '$name'") {
            val config = JavConfig(URL(url))
            logger.debug { "Jav config loaded from $url for target '$name'" }
            config
        }
    }

    private fun loadWorldListProvider(
        properties: ProxyProperties,
        url: String,
    ): WorldListProvider {
        return runCatching("Failed to instantiate world list provider for target '$name'") {
            val provider =
                DynamicWorldListProvider(
                    config,
                    URL(url),
                    properties.getProperty(ProxyProperty.WORLDLIST_REFRESH_SECONDS),
                )
            logger.debug { "World list provider loaded from $url for target '$name'" }
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
        val world = checkNotNull(worldListProvider.get().getTargetWorld(address))
        logger.debug { "Loaded initial world ${world.localHostAddress} <-> ${world.host} for target '$name'" }
        return world
    }

    private fun rebuildJavConfig(
        javConfig: JavConfig,
        replacementWorld: World,
    ): JavConfig {
        return runCatching("Failed to rebuild jav_config.ws for target '$name'") {
            val oldWorldList = javConfig.getWorldListUrl()
            val oldCodebase = javConfig.getCodebase()
            val changedWorldListUrl = "http://127.0.0.1:$httpPort/worldlist.ws"
            val changedCodebase = "http://${replacementWorld.localHostAddress}/"
            val updated =
                javConfig
                    .replaceWorldListUrl(changedWorldListUrl)
                    .replaceCodebase(changedCodebase)
            logger.debug { "Rebuilt jav_config.ws for target '$name':" }
            logger.debug { "Codebase changed from '$oldCodebase' to '$changedCodebase'" }
            logger.debug { "Worldlist changed from '$oldWorldList' to '$changedWorldListUrl'" }
            updated
        }
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
        httpServerBootstrap
            .bind(httpPort)
            .asCompletableFuture()
            .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .join()
        this.httpServerBootstrap = httpServerBootstrap
        logger.debug { "HTTP server bound to port $httpPort for target '$name'" }
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

    private companion object {
        private val logger = InlineLogger()
    }
}
