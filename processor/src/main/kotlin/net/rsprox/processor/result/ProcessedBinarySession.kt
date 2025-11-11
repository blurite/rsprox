package net.rsprox.processor.result

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.processor.state.BinarySessionStateIterator
import net.rsprox.proxy.binary.BinaryHeader
import java.nio.file.Path

public class ProcessedBinarySession(
    public val file: Path,
    public val header: BinaryHeader,
    private val messages: List<IncomingMessage>,
) : List<IncomingMessage> by messages {
    public fun toTickTimestamped(): TimestampedBinarySession {
        return TimestampedBinarySession.from(this)
    }

    public fun statefulIterator(): BinarySessionStateIterator {
        return BinarySessionStateIterator(messages)
    }
}
