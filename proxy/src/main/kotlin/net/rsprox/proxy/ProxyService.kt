package net.rsprox.proxy

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.UnpooledByteBufAllocator
import net.rsprox.proxy.bootstrap.BootstrapFactory
import net.rsprox.proxy.config.CONFIGURATION_PATH
import net.rsprox.proxy.config.JavConfig
import net.rsprox.proxy.config.ProxyProperties
import net.rsprox.proxy.config.ProxyProperty.Companion.BIND_TIMEOUT_SECONDS
import net.rsprox.proxy.config.ProxyProperty.Companion.JAV_CONFIG_URL
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_HTTP_PORT
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_PORT
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_REFRESH_SECONDS
import net.rsprox.proxy.futures.asCompletableFuture
import net.rsprox.proxy.rsa.readOrGenerateRsaKey
import net.rsprox.proxy.worlds.DynamicWorldListProvider
import net.rsprox.proxy.worlds.World
import net.rsprox.proxy.worlds.WorldListProvider
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.math.BigInteger
import java.net.URL
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.io.path.readText
import kotlin.properties.Delegates
import kotlin.system.exitProcess
import kotlin.time.measureTime

public class ProxyService(
    private val allocator: ByteBufAllocator,
) {
    private lateinit var serverBootstrap: ServerBootstrap
    private lateinit var httpServerBootstrap: ServerBootstrap
    private var properties: ProxyProperties by Delegates.notNull()

    public fun start() {
        logger.info { "Starting proxy service" }
        createConfigurationDirectories()
        loadProperties()
        val rsa = loadRsa()
        val factory = BootstrapFactory(allocator)
        val javConfig = loadJavConfig()
        val worldListProvider = loadWorldListProvider(javConfig.getWorldListUrl())
        val replacementWorld = findCodebaseReplacementWorld(javConfig, worldListProvider)
        val updatedJavConfig = rebuildJavConfig(javConfig, replacementWorld)
        // The original modulus should be obtained during the patching process in the future
        // For testing purposes, since we lack a patcher, we just manually load it from
        // a file for the time being.
        val originalModulus = loadOriginalModulus()
        launchHttpServer(factory, worldListProvider, updatedJavConfig)
        launchProxyServer(factory, worldListProvider, rsa, originalModulus)
    }

    private fun createConfigurationDirectories() {
        runCatching("Unable to create configuration directory: $CONFIGURATION_PATH") {
            Files.createDirectories(CONFIGURATION_PATH)
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
        val javConfigUrl = properties.getProperty(JAV_CONFIG_URL)
        return runCatching("Failed to load jav_config.ws from $javConfigUrl") {
            val config = JavConfig(URL(javConfigUrl))
            logger.debug { "Jav config loaded from $javConfigUrl" }
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
        originalModulus: BigInteger,
    ) {
        runCatching("Failure to launch HTTP server") {
            val serverBootstrap =
                factory.createServerBootStrap(
                    worldListProvider,
                    rsa,
                    originalModulus,
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

    private fun loadOriginalModulus(): BigInteger {
        return runCatching("Unable to locate base original modulus file") {
            val text = ORIGINAL_MODULUS_FILE.readText(Charsets.UTF_8)
            BigInteger(text, 16)
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
        private val ORIGINAL_MODULUS_FILE = CONFIGURATION_PATH.resolve("original_modulus.txt")
    }
}

public fun main() {
    val logger = InlineLogger("ProxyService")
    val service = ProxyService(UnpooledByteBufAllocator.DEFAULT)
    val time = measureTime(service::start)
    logger.info { "Proxy service started in $time" }
}
