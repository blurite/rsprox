package net.rsprox.proxy.downloader

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.NativeClientType
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeBytes

public object RuneWikiNativeClientDownloader {
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
        val file = folder.resolve(filePath)
        file.writeBytes(bytes)
        return file
    }
}
