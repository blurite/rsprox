package net.rsprox.transcriber.state

import net.rsprox.protocol.game.incoming.model.events.util.MouseMovements

/**
 * Tracks the mouse coordinate over time, applying the deltas/absolute positions as expected.
 * When the time variable is set to zero, the delta exceeded maximum possible time delta.
 * In order to fix an overflow issue within the Java clients, the timings are additionally reset
 * to zero after 145,000 milliseconds have elapsed since the last tracking was performed.
 * This is because the client will overflow the time after 163,820 milliseconds; we provide it
 * just enough time to deal with any potential lag and buffering, ensuring the timings can be trusted.
 */
public class MouseTracker {
    private var time: Int = 0
    private var x: Int = -1
    private var y: Int = -1
    private var lastTracking: Long = -1L

    /**
     * Captures the mouse movements as submitted by the client and returns absolute values in a list.
     * @param stepExcess the number of sub-client-cycle milliseconds each movement should have added to it.
     * @param endExcess the number of sub-client-cycle milliseconds that should be added at the very end
     * to keep the tracking precise.
     * @param movements the list of mouse coordinates and time deltas for each recorded movement.
     */
    public fun capture(
        stepExcess: Int,
        endExcess: Int,
        movements: MouseMovements,
    ): List<Recording> {
        val recordings = ArrayList<Recording>(movements.length)
        track(stepExcess, endExcess, movements) { x, y, time, lastMouseButton ->
            recordings += Recording(x, y, time, lastMouseButton)
        }
        return recordings
    }

    /**
     * Tracks the mouse movements as submitted by the client without returning them to the caller.
     * @param stepExcess the number of sub-client-cycle milliseconds each movement should have added to it.
     * @param endExcess the number of sub-client-cycle milliseconds that should be added at the very end
     * to keep the tracking precise.
     * @param movements the list of mouse coordinates and time deltas for each recorded movement.
     */
    public fun track(
        stepExcess: Int,
        endExcess: Int,
        movements: MouseMovements,
    ) {
        track(stepExcess, endExcess, movements) { _, _, _, _ ->
            // No-op, do nothing - just for keeping the values in sync
        }
    }

    /**
     * Captures the mouse movements as submitted by the client and yields them via the [consumer].
     * @param stepExcess the number of sub-client-cycle milliseconds each movement should have added to it.
     * @param endExcess the number of sub-client-cycle milliseconds that should be added at the very end
     * to keep the tracking precise.
     * @param movements the list of mouse coordinates and time deltas for each recorded movement.
     * @param consumer the consumer for each mouse recording.
     */
    private inline fun track(
        stepExcess: Int,
        endExcess: Int,
        movements: MouseMovements,
        consumer: (x: Int, y: Int, time: Int, mouseButton: Int) -> Unit,
    ) {
        for (i in 0..<movements.length) {
            time += stepExcess
            val movement = movements.getMousePosChange(i)
            val timeDelta = movement.timeDelta
            if (timeDelta == 0x1FFF || (i == 0 && untrustworthyTimings())) {
                time = 0
            } else {
                time += timeDelta * MILLISECONDS_PER_CLIENT_CYCLE
            }

            if (movement.delta) {
                x += movement.x
                y += movement.y
            } else {
                x = movement.x
                y = movement.y
            }
            consumer(x, y, time, movement.lastMouseButton)
        }
        time += endExcess
    }

    /**
     * Checks whether the elapsed time between two mouse recording packets has exceeded the
     * limit as described in the header, in which case the time delta should be reset to avoid
     * relying on overflowed values.
     * @return true if timings can no longer be trusted (>145 seconds has elapsed since last mouse packet).
     */
    private fun untrustworthyTimings(): Boolean {
        val elapsed = elapsedMillis()
        return elapsed > TRUSTWORTHY_PACKET_DELAY
    }

    /**
     * Gets the number of milliseconds that have elapsed since the last mouse movement packet.
     * @return number of milliseconds elapsed, or 0 if this is the first invocation/packet.
     */
    private fun elapsedMillis(): Long {
        val last = lastTracking
        val time = System.currentTimeMillis()
        lastTracking = time
        return if (last == -1L) {
            0L
        } else {
            time - last
        }
    }

    /**
     * Mouse movement recording data class.
     * @property x the absolute x coordinate where the mouse was at the time of capturing.
     * @property y the absolute y coordinate where the mouse was at the time of capturing.
     * @property time the number of milliseconds that have elapsed since the last recording.
     * This value will be 0 the time delta is too high (> 145 seconds).
     * @property lastMouseButton the last mouse button that was clicked. This is only assigned on native,
     * and remains at 0x7FFF on other clients.
     */
    public data class Recording(
        public val x: Int,
        public val y: Int,
        public val time: Int,
        public val lastMouseButton: Int = 0x7FFF,
    )

    private companion object {
        /**
         * The number of milliseconds per client cycle, used in encoding the packet.
         */
        private const val MILLISECONDS_PER_CLIENT_CYCLE: Int = 20

        /**
         * The maximum number of milliseconds that can elapse between two mouse recordings in client
         * before the value overflows when writing to the server.
         */
        private const val OVERFLOW_TIME_LIMIT: Long = 8191L * MILLISECONDS_PER_CLIENT_CYCLE

        /**
         * The number of milliseconds that can elapse at most before the game connection is killed off.
         */
        private const val GAME_TIMEOUT: Long = 15 * 1000L

        /**
         * Extra buffer for packet processing, since it is synced to server cycles.
         * Furthermore, the value is rounded off so the limit is a nice round 145 seconds.
         */
        private const val PROCESSING_BUFFER_TIME: Long = 3820L

        /**
         * The maximum number of millisecond that can elapse before the first timing in a mouse movement packet
         * is considered untrustworthy.
         */
        private const val TRUSTWORTHY_PACKET_DELAY: Long = OVERFLOW_TIME_LIMIT - GAME_TIMEOUT - PROCESSING_BUFFER_TIME
    }
}
