package net.rsprox.proxy.rs3.relay

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/** Persistent namespace for the mapped live route. The lease prevents two relays claiming its listeners. */
public class Rs3RoutingNamespace private constructor(
    public val addresses: Rs3LocalAddressSpace,
    private val channel: FileChannel,
    private val lock: FileLock,
) : AutoCloseable {
    override fun close() {
        try {
            lock.release()
        } finally {
            channel.close()
        }
    }

    public companion object {
        public fun acquire(path: Path): Rs3RoutingNamespace {
            val channel =
                FileChannel.open(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE,
                )
            try {
                val lock = checkNotNull(channel.tryLock()) { "A mapped RS3 launch is already active" }
                require(channel.size() <= 8) { "Invalid saved RS3 routing namespace" }
                val bytes = ByteBuffer.allocate(8)
                while (channel.read(bytes) > 0) { /* Read the small, locked namespace file. */ }
                val saved = String(bytes.array(), 0, bytes.position(), Charsets.US_ASCII).trim()
                // Old numeric IDs shared the OSRS suffix space. Migrate under the same
                // file lock, so an active old relay cannot have its namespace reassigned.
                val id =
                    if (saved.startsWith("v2:")) {
                        checkNotNull(saved.removePrefix("v2:").toIntOrNull()) { "Invalid saved RS3 routing namespace" }
                    } else {
                        require(
                            saved.isEmpty() || saved.toIntOrNull() in 0..252,
                        ) { "Invalid saved RS3 routing namespace" }
                        0
                    }
                val addresses = Rs3LocalAddressSpace(id)
                if (!saved.startsWith("v2:")) {
                    channel.truncate(0)
                    channel.position(0)
                    val value = ByteBuffer.wrap("v2:$id".toByteArray(Charsets.US_ASCII))
                    while (value.hasRemaining()) channel.write(value)
                    channel.force(true)
                }
                return Rs3RoutingNamespace(addresses, channel, lock)
            } catch (error: Throwable) {
                channel.close()
                throw error
            }
        }
    }
}
