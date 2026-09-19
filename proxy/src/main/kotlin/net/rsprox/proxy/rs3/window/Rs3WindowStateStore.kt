package net.rsprox.proxy.rs3.window

import com.github.michaelbull.logging.InlineLogger
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE
import java.util.Properties

/** Windows workspace coordinates, not screen/client-area coordinates. */
internal data class Rs3WindowPlacement(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val maximized: Boolean,
) {
    init {
        require(left in -131072..131072 && top in -131072..131072)
        require(right in -131072..131072 && bottom in -131072..131072)
        require(right.toLong() - left in 1..65536 && bottom.toLong() - top in 1..65536)
    }
}

/** First-free persistent slot, independent of the incrementing relay ports and executable filenames. */
internal class Rs3WindowStateStore private constructor(
    private val path: Path,
    private val channel: FileChannel,
    private val lock: FileLock,
) : AutoCloseable {
    fun read(): Rs3WindowPlacement? {
        if (!Files.exists(path)) return null
        return try {
            require(Files.size(path) <= 4096) { "Oversized window-state file" }
            val properties = Properties()
            Files.newBufferedReader(path).use(properties::load)
            require(properties.getProperty("version") == "1") { "Unsupported window-state version" }
            Rs3WindowPlacement(
                properties.getProperty("left").toInt(),
                properties.getProperty("top").toInt(),
                properties.getProperty("right").toInt(),
                properties.getProperty("bottom").toInt(),
                properties.getProperty("maximized").toBooleanStrict(),
            )
        } catch (e: Exception) {
            logger.warn(e) { "Ignoring invalid RS3 window state at $path" }
            null
        }
    }

    fun write(placement: Rs3WindowPlacement) {
        val properties = Properties()
        properties.setProperty("version", "1")
        properties.setProperty("left", placement.left.toString())
        properties.setProperty("top", placement.top.toString())
        properties.setProperty("right", placement.right.toString())
        properties.setProperty("bottom", placement.bottom.toString())
        properties.setProperty("maximized", placement.maximized.toString())
        val temporary = Files.createTempFile(path.parent, "window-", ".tmp")
        try {
            Files.newBufferedWriter(temporary).use { properties.store(it, "RSProx RS3 window placement") }
            try {
                Files.move(temporary, path, ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, path, REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    override fun close() {
        try {
            lock.release()
        } finally {
            channel.close()
        }
    }

    companion object {
        private val logger = InlineLogger()

        fun acquire(directory: Path): Rs3WindowStateStore {
            Files.createDirectories(directory)
            // Separate lock files also keep independent proxy JVMs from overwriting each other's placement.
            for (slot in 0 until 128) {
                val channel = FileChannel.open(directory.resolve("window-$slot.lock"), CREATE, WRITE)
                try {
                    val lock =
                        try {
                            channel.tryLock()
                        } catch (_: OverlappingFileLockException) {
                            null
                        }
                    if (lock != null) {
                        return Rs3WindowStateStore(directory.resolve("window-$slot.properties"), channel, lock)
                    }
                } catch (e: Throwable) {
                    channel.close()
                    throw e
                }
                channel.close()
            }
            error("No free RS3 window-state slots")
        }
    }
}
