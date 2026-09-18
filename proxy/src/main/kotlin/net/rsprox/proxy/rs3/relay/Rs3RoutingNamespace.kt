package net.rsprox.proxy.rs3.relay

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/** Shared address namespace; each launch leases only its own consecutive port pair. */
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
        @Synchronized
        public fun acquire(
            path: Path,
            ports: Rs3RelayPorts,
        ): Rs3RoutingNamespace {
            val channel =
                FileChannel.open(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE,
                )
            try {
                // Serialize metadata migration, but do not hold this lock for the client's lifetime.
                val metadataLock = checkNotNull(channel.tryLock(0, 8, false)) { "RS3 namespace is being initialized" }
                val addresses = metadataLock.use { readAddresses(channel) }
                // Byte ranges beyond the small header reserve ports without growing the file.
                val lock =
                    checkNotNull(channel.tryLock(ports.primary.toLong(), 2, false)) {
                        "RS3 relay ports ${ports.primary}/${ports.alternate} are already leased"
                    }
                return Rs3RoutingNamespace(addresses, channel, lock)
            } catch (error: Throwable) {
                channel.close()
                if (error is OverlappingFileLockException) {
                    throw IllegalStateException("RS3 namespace or relay port pair is already leased", error)
                }
                throw error
            }
        }

        private fun readAddresses(channel: FileChannel): Rs3LocalAddressSpace {
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
            return addresses
        }
    }
}
