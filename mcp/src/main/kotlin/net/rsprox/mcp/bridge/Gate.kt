package net.rsprox.mcp.bridge

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Decides whether a request may still act on a client: fork one, kill one or send one a request.
 *
 * A request can run for minutes, so closing the endpoint cannot wait for it, and its thread cannot be
 * stopped from outside. Each act therefore passes the gate at the moment it happens, and [close] waits
 * only for the acts that are passing, which are short. Once [close] has returned, no act happens until
 * [open]. The standalone server never closes its gate.
 */
public class Gate(
    open: Boolean = true,
) {
    /** The lock that the acts share and that opening and closing take alone. */
    private val lock = ReentrantReadWriteLock()

    /** Whether acts pass. Guarded by [lock]. */
    private var open = open

    /** Whether acts pass at this moment, which says nothing of the next. */
    public val isOpen: Boolean
        get() = lock.read { open }

    /** Run [act] if the gate is open, and keep the gate from closing until it returns. Returns whether it ran. */
    public fun ifOpen(act: () -> Unit): Boolean =
        lock.read {
            if (open) act()

            open
        }

    /** Let acts pass. Idempotent. */
    public fun open() {
        lock.write { open = true }
    }

    /** Refuse every act from now on, once the acts that are passing have returned. Idempotent. */
    public fun close() {
        lock.write { open = false }
    }
}
