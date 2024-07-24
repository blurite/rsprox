package net.rsprox.cache.store

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toByteArray
import net.rsprox.cache.util.atomicWrite
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readBytes

public class DiskGroupStore(
    private val path: Path,
) : GroupStore {
    override fun get(
        archive: Int,
        group: Int,
    ): ByteBuf? {
        val file = path.resolve(archive.toString()).resolve("$group.dat")
        if (!file.exists(LinkOption.NOFOLLOW_LINKS)) {
            return null
        }
        return Unpooled.wrappedBuffer(file.readBytes())
    }

    public fun put(
        archive: Int,
        group: Int,
        file: ByteBuf,
    ) {
        val directory = path.resolve(archive.toString())
        Files.createDirectories(directory)
        val destination = directory.resolve("$group.dat")
        destination.atomicWrite(file.toByteArray())
    }
}
