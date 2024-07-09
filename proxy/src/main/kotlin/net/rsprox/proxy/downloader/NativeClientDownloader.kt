package net.rsprox.proxy.downloader

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.proxy.config.CLIENTS_DIRECTORY
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import java.util.zip.GZIPInputStream
import kotlin.io.path.exists
import kotlin.io.path.readText

public data object NativeClientDownloader {
    private val logger = InlineLogger()

    @OptIn(ExperimentalStdlibApi::class)
    public fun download(type: NativeClientType): Path {
        val repository = buildRepositoryInfo(type.systemShortName)
        val versionData = repository.getVersionData()
        val version =
            versionData.environments
                .entries
                .firstOrNull { it.key == "production" }
                ?: error("Unable to locate latest production version of native client!")
        val id = version.value.id
        val expectedClientName =
            if (type == NativeClientType.WIN) {
                "osclient.exe"
            } else {
                "osclient.app/Contents/MacOS/osclient"
            }
        val metafileCache = CLIENTS_DIRECTORY.resolve("${type.systemShortName}-cached-version.txt")
        if (metafileCache.exists()) {
            val text = metafileCache.readText(Charsets.UTF_8)
            if (text == id) {
                val client = CLIENTS_DIRECTORY.resolve(expectedClientName)
                if (client.exists()) {
                    logger.debug { "Cached native client up to date." }
                    return client
                }
            }
        }
        // Update the metadata file
        metafileCache.toFile().writeText(id)
        logger.debug { "Downloading version ${type.systemShortName}/${version.value.version}-production" }
        val catalog = repository.getCatalog(id)
        val remote = catalog.config.remote
        val baseUrl = remote.baseUrl

        if (remote.pieceFormat != "pieces/{SubString:0,2,{TargetDigest}}/{TargetDigest}.solidpiece") {
            throw IllegalStateException(
                "piece format has changed, " +
                    "format is currently hardcoded in this program: ${remote.pieceFormat}",
            )
        }
        val metafile = catalog.getMetafile()
        var totalSize = 0L
        for (file in metafile.files) {
            totalSize += file.size
        }
        val buffer = ByteBuffer.allocate(totalSize.toInt())
        val digests = metafile.pieces.digests
        for ((i, digest) in digests.withIndex()) {
            logger.debug { "Downloading piece $i/${digests.size}" }
            val hexDigest = Base64.getDecoder().decode(digest).toHexString(HexFormat.Default)
            val url = "${baseUrl}pieces/${hexDigest.substring(0, 2)}/$hexDigest.solidpiece"
            val data = RepositoryDownloader.getData(url)

            @Suppress("UNUSED_VARIABLE")
            val unknownData = data.copyOfRange(0, 6)
            val gzipData = data.copyOfRange(6, data.size)
            val decompressedData = GZIPInputStream(gzipData.inputStream()).readAllBytes()
            buffer.put(decompressedData)
        }
        buffer.flip()
        for (file in metafile.files) {
            logger.debug { "Saving output file ${file.name}" }
            val filePath = CLIENTS_DIRECTORY.resolve(file.name)
            val data = ByteArray(file.size.toInt())
            buffer.get(data)
            Files.createDirectories(filePath.parent)
            Files.write(filePath, data)
        }
        val osclient = metafile.files.first { it.name == expectedClientName }
        return CLIENTS_DIRECTORY.resolve(osclient.name)
    }

    private fun buildRepositoryInfo(systemShortName: String): Repository {
        return Repository(
            "osrs-$systemShortName",
            "https://jagex.akamaized.net/direct6/osrs-$systemShortName/osrs-$systemShortName.json",
            "https://jagex.akamaized.net/direct6/osrs-$systemShortName/catalog/",
            "https://jagex.akamaized.net/direct6/osrs-$systemShortName/alias.json",
        )
    }
}
