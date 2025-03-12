package net.rsprox.proxy.downloader

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.NativeClientType
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeBytes

public object LostCityNativeClientDownloader {
    private val logger = InlineLogger()

    public fun download(
        folder: Path,
        type: NativeClientType,
        version: String,
    ): Path {
        val prefix = "https://archive.lostcity.rs/oldschool.runescape.com/native/"
        val typePath =
            when (type) {
                NativeClientType.WIN -> "osrs-win/"
                NativeClientType.MAC -> "osrs-mac/"
            }
        val versionPath = "$version/"
        val filePath =
            when (type) {
                NativeClientType.WIN -> "osclient.exe"
                NativeClientType.MAC -> "osclient.app/Contents/MacOS/osclient"
            }
        val filePathWithVersion =
            when (type) {
                NativeClientType.WIN -> "osclient-$version.exe"
                NativeClientType.MAC -> "osclient.app/Contents/MacOS/osclient-$version"
            }
        val file = folder.resolve(filePathWithVersion)
        // Return the old file if it already exists, assume it is unchanged
        if (file.exists()) {
            return file
        }
        val url = URL(prefix + typePath + versionPath + filePath)
        val bytes =
            try {
                url.readBytes()
            } catch (t: Throwable) {
                logger.error(t) {
                    "Unable to download client $type/$version"
                }
                throw t
            }
        Files.createDirectories(folder)
        file.writeBytes(bytes)
        return file
    }
}
