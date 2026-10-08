package net.rsprox.proxy

import com.github.michaelbull.logging.InlineLogger
import java.util.ServiceConfigurationError
import java.util.ServiceLoader

/**
 * A part of rsprox that lives in another module and runs inside the process of the GUI.
 * The GUI finds it through [ServiceLoader], so it does not depend on that module.
 */
public interface ProxyExtension {
    /** Called once, after [service] has started. */
    public fun start(service: ProxyService)

    public companion object {
        private val logger = InlineLogger()

        /** Start every extension on the classpath. One that fails is logged and skipped. */
        public fun startAll(service: ProxyService) {
            try {
                for (extension in ServiceLoader.load(ProxyExtension::class.java)) {
                    try {
                        extension.start(service)
                    } catch (t: Throwable) {
                        logger.error(t) { "Unable to start ${extension.javaClass.name}" }
                    }
                }
            } catch (e: ServiceConfigurationError) {
                logger.error(e) { "Unable to load the proxy extensions" }
            }
        }
    }
}
