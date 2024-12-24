package net.rsprox.proxy.downloader

import com.github.michaelbull.logging.InlineLogger
import com.google.gson.Gson
import net.rsprox.patch.runelite.Artifact
import net.rsprox.patch.runelite.Bootstrap
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

public object RuneLiteClientDownloader {
    private const val BOOTSTRAP_URL = "https://cdn.rsprox.net/runelite/launcher/bootstrap.json"
    private const val BOOTSTRAP_SIG_URL = "https://cdn.rsprox.net/runelite/launcher/bootstrap.json.sha256"
    private val logger = InlineLogger()
    private val httpClient = OkHttpClient()
    private val gson = Gson()
    public val bootstrap: Bootstrap by lazy { downloadBootstrap() }

    private fun downloadBootstrap(): Bootstrap {
        logger.info { "Downloading RuneLite Launcher bootstrap" }
        val bootstrapRequest =
            Request
                .Builder()
                .url(BOOTSTRAP_URL)
                .get()
                .build()

        val bootstrapBytes =
            httpClient.newCall(bootstrapRequest).execute().use { response ->
                val body = response.body?.bytes() ?: error("Bootstrap request was null")
                if (!response.isSuccessful) {
                    logger.error { "Failed to retrieve bootstrap: $body" }
                    return@use null
                }
                body
            }

        val signatureRequest =
            Request
                .Builder()
                .url(BOOTSTRAP_SIG_URL)
                .get()
                .build()

        val signatureBytes =
            httpClient.newCall(signatureRequest).execute().use { response ->
                val body = response.body?.bytes() ?: error("Bootstrap signature request was null")
                if (!response.isSuccessful) {
                    logger.error { "Failed to retrieve bootstrap signature: $body" }
                    return@use null
                }
                body
            }

        val sig =
            Signature.getInstance("SHA256withRSA").apply {
                initVerify(getCertificate())
                update(bootstrapBytes)
            }

        if (!sig.verify(signatureBytes)) {
            throw RuntimeException("Failed to validate bootstrap")
        }

        return gson.fromJson(InputStreamReader(ByteArrayInputStream(bootstrapBytes)), Bootstrap::class.java)
    }

    private fun getCertificate(): Certificate {
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificate(RuneLiteClientDownloader::class.java.getResourceAsStream("launcher.crt"))
    }

    public fun cleanupAndDownloadArtifacts(path: Path): Path {
        Files.createDirectories(path.parent)

        cleanup(path)
        download(path)

        return path
    }

    private fun cleanup(target: Path) {
        val existingFiles = target.toFile().listFiles() ?: emptyArray()
        logger.debug { "Cleaning up old RuneLite launcher artifacts" }
        val artifactNames = bootstrap.artifacts.map { it.name }.toSet()
        for (file in existingFiles) {
            if (file.isFile && file.name !in artifactNames) {
                file.delete()
            }
        }
    }

    private fun download(target: Path) {
        logger.info { "Downloading RuneLite launcher artifacts" }
        for (artifact in bootstrap.artifacts) {
            downloadArtifact(artifact, target.resolve(artifact.name))
        }
    }

    private fun downloadArtifact(artifact: Artifact, dest: Path) {
        val hash =
            try {
                hash(dest.toFile())
            } catch (_: FileNotFoundException) {
                null
            } catch (_: IOException) {
                dest.toFile().delete()
                null
            }
        if (artifact.hash == hash) {
            logger.debug { "Hash for ${artifact.name} up to date" }
            return
        }
        logger.info { "Downloading artifact ${artifact.name} hash=${artifact.hash}" }

        Files.newOutputStream(dest).use { out ->
            val request =
                Request
                    .Builder()
                    .url(artifact.path)
                    .get()
                    .build()
            val artifactBytes =
                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.bytes() ?: error("Artifact request was null")
                    if (!response.isSuccessful) {
                        error("Failed to retrieve artifact: $body")
                    }
                    body
                }
            out.write(artifactBytes)
        }

        val newHash = hash(dest.toFile())
        if (artifact.hash != newHash) {
            error("Unable to verify resource ${artifact.name} - expected ${artifact.hash}, got $newHash")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun hash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(file.readBytes())
        return hashBytes.toHexString()
    }
}
