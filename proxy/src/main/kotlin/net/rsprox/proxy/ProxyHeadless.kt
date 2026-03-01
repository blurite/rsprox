package net.rsprox.proxy

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import net.rsprox.proxy.config.HTTP_SERVER_PORT
import net.rsprox.proxy.util.NopSessionMonitor
import java.util.Locale
import kotlin.system.exitProcess

/**
 * Headless (CLI) entrypoint for RSProx that starts the proxy server
 * without the GUI, suitable for automated/scripted environments.
 *
 * Usage:
 *   ./gradlew proxyHeadless -PappArgs="--client <client> --target <target>"
 *
 * Arguments:
 *   --client <name>   Client type (none, runelite, native)
 *   --target <name>   Proxy target name from proxy-targets.yaml
 *
 * Environment variables (optional):
 *   RSPS_JAVCONFIG_URL   Override jav_config URL for custom RSPS
 *   RSPS_RSA             RSA modulus for custom RSPS (requires RSPS_JAVCONFIG_URL)
 */
public fun main(args: Array<String>) {
    val logger = InlineLogger()

    // Parse CLI arguments
    val parsedArgs = parseArguments(args)
    val clientMode = parsedArgs["client"] ?: "none"
    val targetName = parsedArgs["target"]

    // Check environment variables for RSPS configuration
    val rspsJavConfigUrl = System.getenv("RSPS_JAVCONFIG_URL")
    val rspsModulus = System.getenv("RSPS_RSA")

    when {
        rspsModulus == null && rspsJavConfigUrl != null -> throw IllegalArgumentException(
            "Missing ENV variable: RSPS_RSA (required when RSPS_JAVCONFIG_URL is set)",
        )
        rspsModulus != null && rspsJavConfigUrl == null -> throw IllegalArgumentException(
            "Missing ENV variable: RSPS_JAVCONFIG_URL (required when RSPS_RSA is set)",
        )
    }

    logger.info { "Starting RSProx in headless mode" }
    logger.info { "Client mode: $clientMode" }
    if (targetName != null) {
        logger.info { "Target: $targetName" }
    }

    // Set locale
    Locale.setDefault(Locale.US)

    // Initialize ProxyService
    val allocator = ByteBufAllocator.DEFAULT
    val service = ProxyService(allocator)

    // Start the service (loads configurations, RSA keys, etc.)
    service.start(rspsJavConfigUrl, rspsModulus) { percentage, actionText, subActionText, progressText ->
        logger.info { "$actionText - $subActionText ${if (progressText != null) "($progressText)" else ""} [${(percentage * 100).toInt()}%]" }
    }

    // Find and select the target
    val targetIndex = if (targetName != null) {
        val index = service.proxyTargets.indexOfFirst { it.name.equals(targetName, ignoreCase = true) }
        if (index == -1) {
            logger.error { "Target '$targetName' not found in proxy-targets.yaml" }
            logger.error { "Available targets: ${service.proxyTargets.map { it.name }}" }
            exitProcess(1)
        }
        service.setSelectedProxyTarget(index)
        index
    } else {
        val defaultIndex = service.getSelectedProxyTarget()
        logger.info { "Using default target: ${service.proxyTargets[defaultIndex].name}" }
        defaultIndex
    }

    val target = service.proxyTargets[targetIndex]
    logger.info { "Selected target: ${target.name} (revision: ${target.revision})" }

    // Allocate a single port for both HTTP and game proxy servers
    // The HTTP server will use HTTP_SERVER_PORT + sessionId (typically 43600)
    // The game proxy will use the allocated port (typically 43701)
    val gameProxyPort = service.allocatePort()
    val proxyTarget = service.initializeHttpServer(gameProxyPort)

    val httpPort = proxyTarget.httpPort
    logger.info { "HTTP server started on port $httpPort" }
    logger.info { "jav_config endpoint: http://127.0.0.1:$httpPort/javconfig.ws" }
    logger.info { "Alternative endpoint: http://127.0.0.1:$httpPort/jav_config.ws" }
    logger.info { "Worldlist endpoint: http://127.0.0.1:$httpPort/worldlist.ws" }

    // Launch game proxy server on the same allocated port
    try {
        service.launchHeadlessProxy(
            sessionMonitor = NopSessionMonitor,
            port = gameProxyPort,
            proxyTarget = proxyTarget,
            clientTypeName = "VitaLite (Headless)"
        )
        logger.info { "Game proxy server started on port $gameProxyPort" }
        logger.info { "SOCKS proxy available at: 127.0.0.1:$gameProxyPort" }
    } catch (t: Throwable) {
        logger.error(t) { "Failed to start game proxy server" }
        exitProcess(1)
    }

    // Keep the process running
    logger.info { "RSProx headless mode is running. Press Ctrl+C to stop." }

    // Register shutdown hook
    Runtime.getRuntime().addShutdownHook(
        Thread {
            logger.info { "Shutting down RSProx headless mode..." }
            try {
                service.safeShutdown()
            } catch (t: Throwable) {
                logger.error(t) { "Error during shutdown" }
            }
            logger.info { "Shutdown complete" }
        }
    )

    // Block forever until interrupted
    Thread.currentThread().join()
}

/**
 * Parse command-line arguments in the form: --key value
 */
private fun parseArguments(args: Array<String>): Map<String, String> {
    val result = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
        val arg = args[i]
        if (arg.startsWith("--") && i + 1 < args.size) {
            val key = arg.removePrefix("--")
            val value = args[i + 1]
            result[key] = value
            i += 2
        } else {
            i++
        }
    }
    return result
}
