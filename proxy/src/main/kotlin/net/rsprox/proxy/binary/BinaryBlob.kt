package net.rsprox.proxy.binary

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.proxy.config.BINARY_PATH
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.Path
import kotlin.time.TimeSource

public data class BinaryBlob(
    public val header: BinaryHeader,
    public val stream: BinaryStream,
    public val writeIntervalSeconds: Int,
) {
    private var lastWrite = TimeSource.Monotonic.markNow()
    private val closed = AtomicBoolean(false)

    public fun append(
        direction: StreamDirection,
        packet: ByteBuf,
    ) {
        if (closed.get()) {
            throw IllegalStateException("Binary stream is closed.")
        }
        stream.append(
            direction,
            packet,
        )
        val interval = writeIntervalSeconds
        if (interval != 0) {
            val elapsed = lastWrite.elapsedNow().inWholeSeconds
            if (elapsed >= interval) {
                lastWrite = TimeSource.Monotonic.markNow()
                write()
            }
        }
    }

    public fun close() {
        val isClosed = closed.getAndSet(true)
        if (isClosed) {
            return
        }
        write()
    }

    private fun write() {
        write(BINARY_PATH.resolve(header.fileName()))
    }

    private fun write(path: Path) {
        val file = path.toFile()
        if (file.isDirectory) {
            throw IllegalArgumentException("Path does not point to a file: $path")
        }
        val header = header.encode(ByteBufAllocator.DEFAULT).buffer
        val streamCopy = stream.copy()
        val headerBytes = header.readableBytes()
        val streamBytes = streamCopy.readableBytes()
        val array = ByteArray(headerBytes + streamBytes)
        header.readBytes(array, 0, headerBytes)
        streamCopy.readBytes(array, headerBytes, streamBytes)
        val parent = path.parent
        val tempFileName = ".${file.name}"
        val tempPath =
            if (parent == null) {
                val root = path.root
                if (root == null) {
                    Path(tempFileName)
                } else {
                    root.resolve(tempFileName)
                }
            } else {
                parent.resolve(tempFileName)
            }
        checkNotNull(tempPath) {
            "Temporary resolved file is null: $path @ $tempFileName"
        }
        try {
            Files.write(
                tempPath,
                array,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.SYNC,
            )
        } catch (t: Throwable) {
            logger.error(t) {
                "Unable to write buffer to file $tempPath"
            }
            return
        }
        try {
            Files.move(tempPath, path, StandardCopyOption.ATOMIC_MOVE)
        } catch (t: Throwable) {
            logger.error(t) {
                "Unable to copy temporary binary file to real file: $tempPath"
            }
        }
    }

    public companion object {
        private val logger = InlineLogger()

        public fun decode(path: Path): BinaryBlob {
            val file = path.toFile()
            if (!file.isFile) {
                throw IllegalArgumentException("Path does not point to a file: $path")
            }
            val buffer = Unpooled.wrappedBuffer(file.readBytes())
            val header = BinaryHeader.decode(buffer.toJagByteBuf())
            val stream = BinaryStream(buffer.slice())
            return BinaryBlob(header, stream, 0)
        }
    }
}
