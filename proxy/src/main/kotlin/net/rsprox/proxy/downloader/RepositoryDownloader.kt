package net.rsprox.proxy.downloader

import com.auth0.jwt.JWT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64

public data object RepositoryDownloader {
    @PublishedApi
    internal val http: HttpClient = HttpClient.newBuilder().build()

    public inline fun <reified T> getConfig(url: String): T {
        val response =
            http.send(
                HttpRequest.newBuilder(URI.create(url)).build(),
                HttpResponse.BodyHandlers.ofString(),
            )
        if (response.statusCode() != 200) {
            throw IOException("HTTP request resulted in an error: ${response.statusCode()}")
        }
        val decoded = Base64.getDecoder().decode(JWT.decode(response.body()).payload)
        return jacksonObjectMapper().readValue(decoded)
    }

    public fun getData(url: String): ByteArray {
        val response =
            http.send(
                HttpRequest.newBuilder(URI.create(url)).build(),
                HttpResponse.BodyHandlers.ofByteArray(),
            )
        if (response.statusCode() != 200) {
            throw IOException("HTTP request resulted in an error: " + response.statusCode())
        }

        return response.body()
    }
}
