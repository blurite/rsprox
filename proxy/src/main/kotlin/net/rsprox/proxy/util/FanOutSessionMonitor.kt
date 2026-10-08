package net.rsprox.proxy.util

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.property.RootProperty
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Passes every callback to [primary] and then to each of [observers].
 * [primary] behaves as if it were the only monitor: what it throws is passed on. An observer that
 * throws is logged once and otherwise ignored, so it can never keep a callback from another monitor.
 */
public class FanOutSessionMonitor<T>(
    private val primary: SessionMonitor<T>,
    private val observers: List<SessionMonitor<T>>,
) : SessionMonitor<T> {
    private val failureLogged = AtomicBoolean()

    override fun forSession(header: T): SessionMonitor<T> {
        val primary = this.primary.forSession(header)
        val observers = this.observers.mapNotNull { observer -> guarded { observer.forSession(header) } }
        if (observers.isEmpty()) return primary
        return FanOutSessionMonitor(primary, observers)
    }

    override fun onPacketDirection(direction: StreamDirection) {
        each { it.onPacketDirection(direction) }
    }

    override fun onLogin(header: T) {
        each { it.onLogin(header) }
    }

    override fun onLogout(header: T) {
        each { it.onLogout(header) }
    }

    override fun onCacheUpdate(cacheProvider: CacheProvider) {
        each { it.onCacheUpdate(cacheProvider) }
    }

    override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        each { it.onIncomingBytesPerSecondUpdate(bytesPerLastSecond) }
    }

    override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        each { it.onOutgoingBytesPerSecondUpdate(bytesPerLastSecond) }
    }

    override fun onNameUpdate(name: String) {
        each { it.onNameUpdate(name) }
    }

    override fun onUserInformationUpdate(
        userId: Long,
        userHash: Long,
    ) {
        each { it.onUserInformationUpdate(userId, userHash) }
    }

    override fun onTranscribe(
        cycle: Int,
        property: RootProperty,
    ) {
        each { it.onTranscribe(cycle, property) }
    }

    private inline fun each(call: (SessionMonitor<T>) -> Unit) {
        try {
            call(primary)
        } finally {
            for (observer in observers) {
                guarded { call(observer) }
            }
        }
    }

    private inline fun <R> guarded(call: () -> R): R? {
        return try {
            call()
        } catch (t: Throwable) {
            if (failureLogged.compareAndSet(false, true)) {
                logger.error(t) { "A session observer failed. Its further failures are not logged." }
            }
            null
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
