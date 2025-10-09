package net.rsprox.proxy.runelite

import com.github.michaelbull.logging.InlineLogger
import com.google.gson.Gson
import net.rsprox.proxy.config.RUNELITE_LAUNCHER_REPO_DIRECTORY
import net.rsprox.proxy.target.ProxyTarget
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import kotlin.io.path.absolutePathString

public data class Artifact(
    val name: String,
    val path: String,
    val hash: String,
    val size: Int,
)

public data class Launcher(
    val mainClass: String,
    val version: String,
)

public data class Bootstrap(
    val artifacts: List<Artifact>,
    val launcher: Launcher,
)

public class RuneliteLauncher {
    private val bootstrap = getBootstrap()

    init {
        logger.info { "Initialising RuneLite launcher ${bootstrap.launcher.version}" }
    }

    private fun getJava(): String {
        val javaHome = Paths.get(System.getProperty("java.home"))

        if (!Files.exists(javaHome)) {
            throw FileNotFoundException("JAVA_HOME is not set correctly! directory \"$javaHome\" does not exist.")
        }

        var javaPath = Paths.get(javaHome.toString(), "bin", "java.exe")

        if (!Files.exists(javaPath)) {
            javaPath = Paths.get(javaHome.toString(), "bin", "java")
        }

        if (!Files.exists(javaPath)) {
            throw FileNotFoundException("java executable not found in directory \"" + javaPath.parent + "\"")
        }

        return javaPath.toAbsolutePath().toString()
    }

    public fun getLaunchArgs(
        port: Int,
        rsa: String,
        javConfig: String,
        socket: String,
        target: ProxyTarget,
    ): List<String> {
        clean()
        download()

        val guiArgs = System.getProperty("net.rsprox.gui.args")?.split("|") ?: emptyList()
        val classpath = StringBuilder()
        for (artifact in bootstrap.artifacts) {
            if (classpath.isNotEmpty()) {
                classpath.append(File.pathSeparator)
            }
            classpath.append(RUNELITE_LAUNCHER_REPO_DIRECTORY.resolve(artifact.name).absolutePathString())
        }
        // Uncomment and change commit when the next revision is about to push
        // Pick the correct bootstrap commit from here:
        // https://github.com/runelite/static.runelite.net/commits/gh-pages/bootstrap.json
        val repository = "https://raw.githubusercontent.com/runelite/static.runelite.net"
        // val commit = "cea91b9921a3647683ba8a5c22ec75c752c91b07"
        // val bootstrapUrl = "$repository/$commit/bootstrap.json"
        // val bootstrapSigUrl = "$repository/$commit/bootstrap.json.sha256"

        // Any other time, just rely on the latest bootstrap.
        val bootstrapUrl = "https://static.runelite.net/bootstrap.json"
        val bootstrapSigUrl = "https://static.runelite.net/bootstrap.json.sha256"
        val primaryArgs =
            listOf(
                getJava(),
                "-cp",
                classpath.toString(),
                bootstrap.launcher.mainClass,
                "--port=$port",
                "--world_client_port=${target.config.httpPort}",
                "--rsa=$rsa",
                "--jav_config=$javConfig",
                "--socket_id=$socket",
                "--developer-mode",
                "--disable-telemetry",
                "--noupdate",
            )
        val conditionalArgs =
            if (target.config.id != 0) {
                val bootstrapCommitHash = target.config.runeliteBootstrapCommitHash
                val bootstrap =
                    if (bootstrapCommitHash != null) {
                        "$repository/$bootstrapCommitHash/bootstrap.json"
                    } else {
                        null
                    }
                val bootstrapArg =
                    if (bootstrap != null) {
                        "--bootstrap_url=$bootstrap"
                    } else {
                        "--bootstrap_url=$bootstrapUrl"
                    }
                val bootstrapSigArg =
                    if (bootstrap != null) {
                        "--bootstrap_sig_url=$bootstrap.sha256"
                    } else {
                        "--bootstrap_sig_url=$bootstrapSigUrl"
                    }
                listOf(
                    "--client_name=${target.config.name}",
                    "--varp_count=${target.config.varpCount}",
                    bootstrapArg,
                    bootstrapSigArg,
                )
            } else {
                listOf(
                    "--bootstrap_url=$bootstrapUrl",
                    "--bootstrap_sig_url=$bootstrapSigUrl",
                )
            }
        return primaryArgs + conditionalArgs + guiArgs
    }

    private fun download() {
        logger.info { "Downloading RuneLite launcher artifacts" }
        for (artifact in bootstrap.artifacts) {
            val dest = RUNELITE_LAUNCHER_REPO_DIRECTORY.resolve(artifact.name)
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
        val existingFiles = RUNELITE_LAUNCHER_REPO_DIRECTORY.toFile().listFiles() ?: return
        logger.debug { "Cleaning up old artifacts" }
        val artifactNames = bootstrap.artifacts.map { it.name }.toSet()
        for (file in existingFiles) {
            if (file.isFile && file.name !in artifactNames) {
                file.delete()
            }
        }
    }

    private companion object {
        private const val BOOTSTRAP_URL = "https://cdn.rsprox.net/runelite/launcher/bootstrap.json"
        private const val BOOTSTRAP_SIG_URL = "https://cdn.rsprox.net/runelite/launcher/bootstrap.json.sha256"
        private val logger = InlineLogger()
        private val httpClient = OkHttpClient()
        private val gson = Gson()

        private fun getBootstrap(): Bootstrap {
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
            return factory.generateCertificate(RuneliteLauncher::class.java.getResourceAsStream("launcher.crt"))
        }
    }
}
