package net.rsprox.cache.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.rsprox.cache.dictionary.openrs2.CacheScope
import java.net.URI

@PublishedApi
internal const val CONNECT_TIMEOUT_MS: Int = 8_000

@PublishedApi
internal val ROOT_URI: URI = URI("https://archive.openrs2.org/")

@PublishedApi
internal const val CACHES_ENDPOINT: String = "caches.json"

@PublishedApi
internal val mapper: ObjectMapper = jacksonObjectMapper()

public inline fun <reified T> downloadCacheListings(): T {
    val connection =
        ROOT_URI
            .resolve(CACHES_ENDPOINT)
            .toURL()
            .openConnection()
    connection.connectTimeout = CONNECT_TIMEOUT_MS
    return connection.getInputStream().use { stream ->
        mapper.readValue<T>(stream.readBytes().toString(Charsets.UTF_8))
    }
}

public fun downloadOpenRs2Group(
    scope: CacheScope,
    cacheId: Int,
    archive: Int,
    group: Int,
): ByteArray {
    val connection =
        ROOT_URI
            .resolve("caches/${scope.label}/$cacheId/archives/$archive/groups/$group.dat")
            .toURL()
            .openConnection()
    connection.connectTimeout = CONNECT_TIMEOUT_MS
    return connection.getInputStream().use { stream ->
        stream.readBytes()
    }
}
