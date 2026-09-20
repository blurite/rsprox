package net.rsprox.proxy.rs3.launcher

import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE
import java.util.concurrent.atomic.AtomicBoolean

/** Persistent, first-free preferences with a cross-JVM lease held until both native processes stop. */
internal class Rs3LauncherSlot private constructor(
    val directory: Path,
    private val channel: FileChannel,
    private val lock: FileLock,
) : AutoCloseable {
    private val closed = AtomicBoolean()
    val name: String get() = directory.fileName.toString()
    val client: Path get() = directory.resolve("rs2client.exe")
    val launcher: Path get() = directory.resolve("RuneScape-rsprox.exe")

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        try {
            lock.release()
        } finally {
            channel.close()
        }
    }

    companion object {
        fun acquire(
            configuration: Path,
            cacheRoot: Path,
        ): Rs3LauncherSlot {
            val owner = configuration.toAbsolutePath().normalize().toString()
            val namespace = Rs3LauncherDistribution.sha256(owner.toByteArray()).take(5)
            Files.createDirectories(cacheRoot)
            for (slot in 0 until 128) {
                val name = "r$namespace${slot.toString(16).padStart(2, '0')}"
                val directory = cacheRoot.resolve(name)
                Files.createDirectories(directory)
                val channel = FileChannel.open(directory.resolve("rsprox.lock"), CREATE, WRITE)
                try {
                    val lock =
                        try {
                            channel.tryLock()
                        } catch (_: OverlappingFileLockException) {
                            null
                        }
                    if (lock != null) {
                        val lease = Rs3LauncherSlot(directory, channel, lock)
                        try {
                            val ownerFile = directory.resolve("rsprox-owner.txt")
                            if (Files.exists(ownerFile)) {
                                check(Files.readString(ownerFile) == owner) {
                                    "RS3 launcher storage belongs to another installation"
                                }
                            } else {
                                Files.list(directory).use { entries ->
                                    check(entries.allMatch { it.fileName.toString() == "rsprox.lock" }) {
                                        "Refusing to overwrite an unrecognized RuneScape launcher directory"
                                    }
                                }
                                Files.writeString(ownerFile, owner)
                            }
                            val userRoot = configuration.resolve("slots/$slot/user").toAbsolutePath()
                            Files.createDirectories(userRoot)
                            val preferences = directory.resolve("preferences.cfg")
                            if (!Files.exists(preferences)) {
                                Files.writeString(
                                    preferences,
                                    "cache_folder=$cacheRoot\nuser_folder=$userRoot\nLanguage=0\n",
                                )
                            }
                            return lease
                        } catch (e: Throwable) {
                            lease.close()
                            throw e
                        }
                    }
                } catch (e: Throwable) {
                    channel.close()
                    throw e
                }
                channel.close()
            }
            error("No free RuneScape launcher slots")
        }
    }
}
