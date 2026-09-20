package net.rsprox.proxy.rs3.launcher

import net.rsprox.proxy.rs3.Rs3LaunchProgress
import org.tukaani.xz.LZMA2InputStream
import java.net.URI
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.security.MessageDigest

/** A pinned bootstrap, extracted as data: never execute an installer or update the official installation. */
internal object Rs3LauncherDistribution {
    const val VERSION = 224
    const val SUB_VERSION = 1
    const val LAUNCHER_SHA256 = "f91142aaa6396795da9df71ff9c8a84b51307fb0d6d9086c5e5a874d6947dd9d"
    private const val INSTALLER_SHA256 = "4d0bbde794c725d97af7ffcd12dee8fab5573dbfe8ce15b3ee5c9fc6f4b66f2d"
    private const val INSTALLER_URL = "https://content.runescape.com/downloads/windows/RuneScape-Setup.exe"

    @Synchronized
    fun load(
        directory: Path,
        onProgress: (Rs3LaunchProgress) -> Unit,
    ): ByteArray {
        Files.createDirectories(directory)
        val cached = directory.resolve("RuneScape-$VERSION.$SUB_VERSION.exe")
        if (Files.isRegularFile(cached) && Files.size(cached) == 5_921_904L) {
            Files.readAllBytes(cached).let { if (sha256(it) == LAUNCHER_SHA256) return it }
        }
        onProgress(Rs3LaunchProgress("Downloading RuneScape launcher"))
        val connection = URI(INSTALLER_URL).toURL().openConnection()
        connection.connectTimeout = 10_000
        connection.readTimeout = 30_000
        val installer = connection.getInputStream().use { it.readNBytes(4 * 1024 * 1024) }
        val launcher = extract(installer)
        val temporary = Files.createTempFile(directory, "launcher-", ".tmp")
        try {
            Files.write(temporary, launcher)
            try {
                Files.move(temporary, cached, ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, cached, REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
        return launcher
    }

    fun extract(installer: ByteArray): ByteArray {
        // Note: launcher 224.1 / Inno 5.5.7 distribution profile. Update both hashes and extraction
        // boundaries together when the official installer changes. Never guess offsets on a new binary.
        check(sha256(installer) == INSTALLER_SHA256) {
            "Unsupported RuneScape launcher installer; RSProx's verified launcher profile needs updating"
        }
        val stream = installer.inputStream(0x3D005, 2_393_033)
        val solid = LZMA2InputStream(stream, 8 * 1024 * 1024).use { it.readNBytes(6_246_424) }
        check(solid.size == 6_246_423) { "Unexpected RuneScape launcher archive length" }
        val launcher = solid.copyOf(5_921_904)
        InnoExecutableFilter.decode(launcher)
        check(sha256(launcher) == LAUNCHER_SHA256) { "RuneScape launcher verification failed" }
        return launcher
    }

    fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
