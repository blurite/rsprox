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

    /**
     * Serve what the extension offers to other programs, from this call on. Returns null once it is
     * served, and otherwise the reason it is not, in words for the user. Called on the Swing thread.
     */
    public fun serve(): String? = null

    /** Stop serving it, from this call on. Called on the Swing thread. */
    public fun stopServing() {
    }

    public companion object {
        private val logger = InlineLogger()

        /** Start every extension on the classpath. One that fails is logged and skipped. */
        public fun startAll(service: ProxyService): StartedExtensions {
            val started = ArrayList<ProxyExtension>()
            try {
                for (extension in ServiceLoader.load(ProxyExtension::class.java)) {
                    try {
                        extension.start(service)
                        started += extension
                    } catch (t: Throwable) {
                        logger.error(t) { "Unable to start ${extension.javaClass.name}" }
                    }
                }
            } catch (e: ServiceConfigurationError) {
                logger.error(e) { "Unable to load the proxy extensions" }
            }
            return StartedExtensions(started)
        }
    }
}

/** The extensions that started, which the GUI switches on and off while it runs. */
public class StartedExtensions internal constructor(
    private val extensions: List<ProxyExtension>,
) {
    /** Ask every extension to serve. Returns null once all do, and otherwise the reason of the first that does not. */
    public fun serve(): String? {
        if (extensions.isEmpty()) {
            return "Nothing is served, because no extension of RSProx is running. " +
                "The log names one that failed to start."
        }
        for (extension in extensions) {
            val refusal =
                try {
                    extension.serve()
                } catch (t: Throwable) {
                    logger.error(t) { "Unable to serve ${extension.javaClass.name}" }
                    "${extension.javaClass.name} failed: ${t.message}"
                }
            if (refusal != null) return refusal
        }
        return null
    }

    /** Ask every extension to stop serving. One that fails is logged and skipped. */
    public fun stopServing() {
        for (extension in extensions) {
            try {
                extension.stopServing()
            } catch (t: Throwable) {
                logger.error(t) { "Unable to stop serving ${extension.javaClass.name}" }
            }
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
