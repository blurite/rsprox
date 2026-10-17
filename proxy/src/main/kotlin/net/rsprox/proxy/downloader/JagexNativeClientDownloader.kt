package net.rsprox.proxy.downloader

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.NativeClientType
import net.rsprox.proxy.config.CLIENTS_DIRECTORY
import net.rsprox.proxy.downloader.cpp.Repository
import net.rsprox.proxy.downloader.cpp.RepositoryDownloader
import net.rsprox.proxy.rs3.Rs3LaunchProgress
import net.rsprox.proxy.rs3.config.Rs3JavConfig
import java.net.URI
import java.net.URL
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import java.util.zip.GZIPInputStream
import kotlin.io.path.exists
import kotlin.io.path.readText

public data object JagexNativeClientDownloader {
    private val logger = InlineLogger()
    public const val DEFAULT_RS3_JAV_CONFIG_URL: String =
        "https://world5.runescape.com/jav_config.ws?binaryType=2"
    public const val VULKAN_RS3_JAV_CONFIG_URL: String =
        "https://world5.runescape.com/jav_config.ws?binaryType=10"

    // Use the current beta world. Keep HTTP so resource URLs do not require beta-host TLS support.
    public const val BETA_RS3_JAV_CONFIG_URL: String =
        "http://world205.runescape.com/jav_config_beta.ws?binaryType=2"
    public const val BETA_VULKAN_RS3_JAV_CONFIG_URL: String =
        "http://world205.runescape.com/jav_config_beta.ws?binaryType=10"

    @OptIn(ExperimentalStdlibApi::class)
    public fun download(
        type: NativeClientType,
        rs3JavConfigUrl: String = DEFAULT_RS3_JAV_CONFIG_URL,
        onProgress: (Rs3LaunchProgress) -> Unit = {},
    ): Path {
        if (type == NativeClientType.RS3_WIN) {
            return RSProxArchiveRs3ClientDownloader.live(
                Rs3JavConfig(URL(rs3JavConfigUrl)),
                rs3BinaryType(rs3JavConfigUrl),
                URI(rs3JavConfigUrl).path.endsWith("/jav_config_beta.ws"),
                onProgress,
            )
        }

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
            val filePath = CLIENTS_DIRECTORY.resolve(file.name)
            logger.debug { "Saving output file $filePath" }
            val data = ByteArray(file.size.toInt())
            buffer.get(data)
            Files.createDirectories(filePath.parent)
            Files.write(filePath, data)
        }
        val osclient = metafile.files.first { it.name == expectedClientName }
        return CLIENTS_DIRECTORY.resolve(osclient.name)
    }

    internal fun rs3BinaryType(javConfigUrl: String): Int {
        val values =
            URI(javConfigUrl)
                .query
                .orEmpty()
                .split('&')
                .filter { it.substringBefore('=') == "binaryType" }
                .map { it.substringAfter('=', "").toIntOrNull() }
        val binaryType = values.singleOrNull()
        require(binaryType == 2 || binaryType == 10) {
            "RS3 javconfig must specify one binaryType: 2 (Windows OpenGL) or 10 (Windows Vulkan)"
        }
        return binaryType
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
