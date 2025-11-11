package net.rsprox.processor.result

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.game.outgoing.model.misc.client.ServerTickEnd
import net.rsprox.proxy.binary.BinaryHeader
import java.nio.file.Path

/**
 * A server-tick-indexed representation of [ProcessedBinarySession].
 *
 * Each map key represents the server tick (starting at `0` and incrementing after every [ServerTickEnd] message).
 * Each map value contains all [net.rsprot.protocol.message.IncomingMessage]s sent during said tick, excluding
 * [ServerTickEnd] itself.
 */
public class TimestampedBinarySession private constructor(
    public val file: Path,
    public val header: BinaryHeader,
    private val timestamped: Map<Int, List<IncomingMessage>>,
) : Map<Int, List<IncomingMessage>> by timestamped {
    public companion object {
        public fun from(session: ProcessedBinarySession): TimestampedBinarySession {
            val timestamped = mutableMapOf<Int, MutableList<IncomingMessage>>()
            var currentTick = 0
            for (message in session) {
                if (message is ServerTickEnd) {
                    currentTick++
                    continue
                }
                val messages = timestamped.getOrPut(currentTick) { mutableListOf() }
                messages += message
            }
            return TimestampedBinarySession(session.file, session.header, timestamped)
        }
    }
}
