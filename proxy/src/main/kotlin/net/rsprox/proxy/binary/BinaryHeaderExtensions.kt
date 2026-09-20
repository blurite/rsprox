package net.rsprox.proxy.binary

import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import java.nio.file.Files
import java.nio.file.Path

public fun BinaryHeader.isRuneScape3(): Boolean =
    when (clientName) {
        // Recordings made before renderer metadata was added remain supported.
        "RS3 Native", "RS3 Native (OpenGL)", "RS3 Native (Vulkan)" -> true
        else -> false
    }

/** Inspect the header without loading the entire packet stream for a GUI file choice. */
public fun readBinaryHeader(path: Path): BinaryHeader {
    val bytes = Files.newInputStream(path).use { it.readNBytes(1024 * 1024) }
    val buffer = Unpooled.wrappedBuffer(bytes)
    return try {
        BinaryHeader.decode(buffer.toJagByteBuf())
    } finally {
        buffer.release()
    }
}

internal fun BinaryHeader.isOldSchoolRuneScape(): Boolean = !isRuneScape3() && worldHost.endsWith(".runescape.com")
