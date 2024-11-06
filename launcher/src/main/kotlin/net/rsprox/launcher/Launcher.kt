package net.rsprox.launcher

import com.github.michaelbull.logging.InlineLogger
import com.google.gson.Gson
import net.rsprox.gui.SplashScreen
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.Locale
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

public fun main() {
    Locale.setDefault(Locale.US)
    SplashScreen.init()
    SplashScreen.stage(0.0, "Preparing", "Setting up environment")
    val launcher = Launcher()

    val builder =
        ProcessBuilder()
            .inheritIO()
            .command(launcher.getLaunchArgs())

    SplashScreen.stop()
    builder.start()
}

public data class Artifact(
    val name: String,
    val path: String,
    val hash: String,
    val size: Int,
)

public data class Proxy(
    val mainClass: String,
    val version: String,
)

public data class Bootstrap(
    val artifacts: List<Artifact>,
    val proxy: Proxy,
)

public class Launcher {
    private val bootstrap = getBootstrap()

    init {
        logger.info { "Initialising RSProx launcher ${bootstrap.proxy.version}" }
        SplashScreen.stage(0.20, "Preparing", "Creating directories")
        Files.createDirectories(artifactRepo)
    }

    public fun getLaunchArgs(): List<String> {
        clean()
        download()

        val classpath = StringBuilder()
        for (artifact in bootstrap.artifacts) {
            if (classpath.isNotEmpty()) {
                classpath.append(File.pathSeparator)
            }
            classpath.append(artifactRepo.resolve(artifact.name).absolutePathString())
        }

        return listOf(
            "java",
            "-cp",
            classpath.toString(),
            bootstrap.proxy.mainClass,
        )
    }

    private fun download() {
        SplashScreen.stage(0.4, "Downloading", "Downloading artifacts")
        logger.info { "Downloading proxy-tool artifacts" }
        for (artifact in bootstrap.artifacts) {
            val dest = artifactRepo.resolve(artifact.name)
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
                continue
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
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun hash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(file.readBytes())
        return hashBytes.toHexString()
    }

    private fun clean() {
        SplashScreen.stage(0.30, "Downloading", "Cleaning up old artifacts")
        val existingFiles = artifactRepo.toFile().listFiles() ?: return
        logger.debug { "Cleaning up old artifacts" }
        val artifactNames = bootstrap.artifacts.map { it.name }.toSet()
        for (file in existingFiles) {
            if (file.isFile && file.name !in artifactNames) {
                file.delete()
            }
        }
    }

    private companion object {
        private const val BOOTSTRAP_URL = "https://cdn.rsprox.net/launcher/bootstrap.json"
        private const val BOOTSTRAP_SIG_URL = "https://cdn.rsprox.net/launcher/bootstrap.json.sha256"

        private val configurationPath: Path = Path(System.getProperty("user.home"), ".rsprox", "launcher")
        private val artifactRepo: Path = configurationPath.resolve("repository")

        private val logger = InlineLogger()
        private val httpClient = OkHttpClient()
        private val gson = Gson()

        private fun getBootstrap(): Bootstrap {
            SplashScreen.stage(0.1, "Preparing", "Downloading bootstrap")
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

            SplashScreen.stage(0.15, "Preparing", "Verifying bootstrap")

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
            return factory.generateCertificate(Launcher::class.java.getResourceAsStream("rsprox.crt"))
        }
    }
}
