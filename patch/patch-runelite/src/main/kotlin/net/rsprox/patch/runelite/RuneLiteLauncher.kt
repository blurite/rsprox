package net.rsprox.patch.runelite

import com.github.michaelbull.logging.InlineLogger
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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

public class RuneLiteLauncher(private val bootstrap: Bootstrap, private val artifactsDir: Path) {
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
    ): List<String> {
        val guiArgs = System.getProperty("net.rsprox.gui.args")?.split("|") ?: emptyList()
        val classpath = StringBuilder()
        for (artifact in bootstrap.artifacts) {
            if (classpath.isNotEmpty()) {
                classpath.append(File.pathSeparator)
            }
            classpath.append(artifactsDir.resolve(artifact.name).absolutePathString())
        }

        return listOf(
            getJava(),
            "-cp",
            classpath.toString(),
            bootstrap.launcher.mainClass,
            "--rsa=$rsa",
            "--port=$port",
            "--jav_config=$javConfig",
            "--socket_id=$socket",
            "--developer-mode",
            *guiArgs.toTypedArray(),
        ).filterNot { it.isEmpty() }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
