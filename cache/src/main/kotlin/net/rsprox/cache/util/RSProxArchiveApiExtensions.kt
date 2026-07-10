package net.rsprox.cache.util

import com.fasterxml.jackson.module.kotlin.readValue
import net.rsprox.cache.dictionary.RSProxArchiveCacheIndex
import java.io.InputStream
import java.net.URI

private val RSPROX_ARCHIVE_INDEX_URI: URI =
    URI("https://archive.rsprox.net/caches/index.json")

internal fun downloadRSProxArchiveCacheIndex(): RSProxArchiveCacheIndex {
    val connection = RSPROX_ARCHIVE_INDEX_URI.toURL().openConnection()
    connection.connectTimeout = CONNECT_TIMEOUT_MS
    connection.readTimeout = READ_TIMEOUT_MS
    return connection.getInputStream().use(mapper::readValue)
}

internal fun openRSProxArchiveCache(url: String): InputStream {
    val uri = URI(url)
    require(uri.scheme.equals("https", ignoreCase = true)) {
        "RSProx Archive cache URL must use HTTPS"
    }
    require(uri.host.equals(RSPROX_ARCHIVE_HOST, ignoreCase = true)) {
        "RSProx Archive cache URL must use $RSPROX_ARCHIVE_HOST"
    }
    require(uri.path.startsWith("/caches/")) {
        "RSProx Archive cache URL must be under /caches/"
    }
    val connection = uri.toURL().openConnection()
    connection.connectTimeout = CONNECT_TIMEOUT_MS
    connection.readTimeout = CACHE_READ_TIMEOUT_MS
    return connection.getInputStream()
}

private const val RSPROX_ARCHIVE_HOST: String = "archive.rsprox.net"
private const val READ_TIMEOUT_MS: Int = 8_000
private const val CACHE_READ_TIMEOUT_MS: Int = 30_000
