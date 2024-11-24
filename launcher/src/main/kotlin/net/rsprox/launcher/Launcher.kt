package net.rsprox.launcher

import com.github.michaelbull.logging.InlineLogger
import com.google.gson.Gson
import joptsimple.OptionParser
import net.rsprox.gui.SplashScreen
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.*
import java.util.stream.Collectors
import javax.swing.UIManager
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

public fun main(args: Array<String>) {
    val logger = InlineLogger()
    val parser = OptionParser(false)
    parser.allowsUnrecognizedOptions()
    parser.accepts("runelite", "Whether we are launching the RuneLite client")
    parser.accepts("classpath", "Classpath for the process are are launching").withRequiredArg()

    val options = parser.parse(*args)
    if (options.has("classpath")) {
        // Sometimes we will be getting launched as a process by an existing RSProx process or a new runelite
        // process, e.g. from a Linux AppImage launcher. In this case we need to load the classpath and launch
        // the main class ourselves using reflection, passing along any necessary arguments.

        val loadingRunelite = options.has("runelite")
        val classpathOpt = options.valueOf("classpath").toString()
        val classpath = classpathOpt.split(File.pathSeparator).stream().map {
            if (loadingRunelite) {
                // runelite-launcher doesn't pass the fully qualified paths of jars, so construct them ourselves
                Paths.get(System.getProperty("user.home"), ".runelite", "repository2", it).toFile()
            } else {
                Paths.get(it).toFile()
            }
        }.collect(Collectors.toList())

        val jarUrls = classpath.map { it.toURI().toURL() }.toTypedArray()
        val parent = ClassLoader.getPlatformClassLoader()
        val loader = URLClassLoader(jarUrls, parent)

        UIManager.put("ClassLoader", loader)
        val thread = Thread {
            try {
                val mainClassPath = if (loadingRunelite) "net.runelite.client.RuneLite" else listOf("net" +
                    ".runelite.launcher.Launcher", "net.rsprox.gui.ProxyToolGuiKt").first { it in args}
                val mainClass = loader.loadClass(mainClassPath)
                val mainArgs = if (loadingRunelite) {
                    // RuneLite doesn't allow unrecognised arguments so only use arguments after --classpath
                    args.copyOfRange(args.indexOfFirst { it == "--classpath" } + 2, args.size)
                }else {
                    args.copyOfRange(args.indexOfFirst { it == mainClass.name } + 1, args.size)
                }

                logger.info { "Launching process using reflection: $mainClassPath ${mainArgs.joinToString(", ")}" }

                val main = mainClass.getMethod("main", Array<String>::class.java)
                main.invoke(null, mainArgs)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        thread.name = "RSProx"
        thread.start()

        return
    }

    Locale.setDefault(Locale.US)
    SplashScreen.init()
    SplashScreen.stage(0.0, "Preparing", "Setting up environment")

    val launcher = Launcher(args)
    val launcherArgs = launcher.getLaunchArgs(args)
    logger.info { "Running process: ${launcherArgs.joinToString(" ")}" }

    val builder =
        ProcessBuilder()
            .inheritIO()
            .command(launcher.getLaunchArgs(args))

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

public class Launcher(args: Array<String>) {
    private val bootstrap = getBootstrap()

    init {
        logger.info { "Initialising RSProx launcher ${bootstrap.proxy.version}" }
        logger.info {
            "OS name: ${System.getProperty("os.name")}, version: ${System.getProperty("os.version")}, arch: ${
                System.getProperty(
                    "os.arch"
                )
            }"
        }
        logger.info { "Java path: ${getJava()}" }
        logger.info { "Java version: ${System.getProperty("java.version")}" }
        logger.info { "Launcher args: ${args.joinToString(",")}" }
        logger.info { "AppImage: ${System.getenv("APPIMAGE")}" }

        SplashScreen.stage(0.30, "Preparing", "Creating directories")
        Files.createDirectories(artifactRepo)
    }

    private fun getJava(): String {
        val javaHome = Paths.get(System.getProperty("java.home"))

        if (!Files.exists(javaHome)) {
            throw FileNotFoundException("JAVA_HOME is not set correctly! directory '$javaHome' does not exist.")
        }

        var javaPath = Paths.get(javaHome.toString(), "bin", "java.exe")

        if (!Files.exists(javaPath)) {
            javaPath = Paths.get(javaHome.toString(), "bin", "java")
        }

        if (!Files.exists(javaPath)) {
            throw FileNotFoundException("java executable not found in directory '${javaPath.parent}'")
        }

        return javaPath.toAbsolutePath().toString()
    }

    public fun getLaunchArgs(launcherArgs: Array<String>): List<String> {
        clean()
        download()

        val classpath = StringBuilder()
        for (artifact in bootstrap.artifacts) {
            if (classpath.isNotEmpty()) {
                classpath.append(File.pathSeparator)
            }
            classpath.append(artifactRepo.resolve(artifact.name).absolutePathString())
        }

        if (System.getenv("APPIMAGE") != null) {
            return listOf(
                System.getenv("APPIMAGE"),
                "-c",
                "-J",
                "-XX:+DisableAttachMechanism",
                "--",
                "--classpath",
                classpath.toString(),
                bootstrap.proxy.mainClass,
                *launcherArgs
            )
        } else {
            return listOf(
                getJava(),
                "-cp",
                classpath.toString(),
                bootstrap.proxy.mainClass,
                *launcherArgs
            )
        }
    }

    private fun download() {
        val downloadStartProgress = 0.50
        SplashScreen.stage(downloadStartProgress, "Downloading", "Downloading artifacts")
        logger.info { "Downloading proxy-tool artifacts" }

        val toDownload = mutableListOf<Artifact>()
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
            toDownload += artifact
        }

        val totalDownloadSize = toDownload.sumOf { it.size }
        var progress = downloadStartProgress
        for (artifact in toDownload) {
            val dest = artifactRepo.resolve(artifact.name)
            logger.info { "Downloading artifact ${artifact.name} hash=${artifact.hash}" }
            SplashScreen.stage(progress, "Downloading", "Downloading ${artifact.name}")

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

            val toAdd = artifact.size.toDouble() / totalDownloadSize.toDouble()
            progress += toAdd * (1 - downloadStartProgress)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun hash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(file.readBytes())
        return hashBytes.toHexString()
    }

    private fun clean() {
        SplashScreen.stage(0.40, "Downloading", "Cleaning up old artifacts")
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
            SplashScreen.stage(0.10, "Preparing", "Downloading bootstrap")
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

            SplashScreen.stage(0.20, "Preparing", "Verifying bootstrap")

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
