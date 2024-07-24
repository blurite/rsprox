package net.rsprox.cache.util

import com.github.michaelbull.logging.InlineLogger
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.nameWithoutExtension

private val logger = InlineLogger()

public fun File.atomicWrite(string: String) {
    toPath().atomicWrite(string)
}

public fun Path.atomicWrite(string: String) {
    atomicWrite(string.toByteArray(Charsets.UTF_8))
}

public fun File.atomicWrite(array: ByteArray) {
    toPath().atomicWrite(array)
}

public fun Path.atomicWrite(array: ByteArray) {
    val name = this.nameWithoutExtension
    val tempFileName = ".$name"
    val parent = this.parent
    val tempPath =
        if (parent == null) {
            val root = root
            if (root == null) {
                Path(tempFileName)
            } else {
                root.resolve(tempFileName)
            }
        } else {
            parent.resolve(tempFileName)
        }
    if (tempPath.exists(LinkOption.NOFOLLOW_LINKS)) {
        try {
            tempPath.deleteExisting()
        } catch (e: Exception) {
            logger.error(e) {
                "Unable to delete existing temporary file."
            }
        }
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
            "Unable to write array to file $tempPath"
        }
        return
    }
    try {
        Files.move(tempPath, this, StandardCopyOption.ATOMIC_MOVE)
    } catch (t: Throwable) {
        logger.error(t) {
            "Unable to copy temporary file to real file: $tempPath"
        }
    }
}
