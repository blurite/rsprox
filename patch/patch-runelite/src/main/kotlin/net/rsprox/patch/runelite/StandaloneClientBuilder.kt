package net.rsprox.patch.runelite

import com.github.michaelbull.logging.InlineLogger
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

public class StandaloneClientBuilder {
    public companion object {
        private val logger = InlineLogger()
        private const val LAUNCHER_CLASS_NAME: String = "JvmLauncher"
    }

    public fun build(output: Path, dependenciesDir: Path, excludeJars: List<String>) {
        require(Files.isDirectory(dependenciesDir)) { "dependenciesDir must be a valid directory: $dependenciesDir" }
        Files.createDirectories(output.parent)
        require(!Files.exists(output)) { "Output file already exists: $output" }

        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            mainAttributes[Attributes.Name.MAIN_CLASS] = LAUNCHER_CLASS_NAME
        }

        JarOutputStream(Files.newOutputStream(output), manifest).use { jarOut ->
            addClass(jarOut)
            addDependencies(jarOut, dependenciesDir, excludeJars)
        }

        logger.debug { "Fat JAR built successfully: $output" }
    }

    private fun addClass(jarOut: JarOutputStream) {
        val launcherClassStream = RuneLitePatcher::class.java.getResourceAsStream("JvmLauncher.class")
        requireNotNull(launcherClassStream) { "JvmLauncher.class not found in resources" }

        val jarEntry = JarEntry("$LAUNCHER_CLASS_NAME.class")
        jarOut.putNextEntry(jarEntry)
        launcherClassStream.copyTo(jarOut)
        jarOut.closeEntry()
    }

    private fun addDependencies(jarOut: JarOutputStream, dependenciesDir: Path, excludeJars: List<String>) {
        Files.walk(dependenciesDir).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".jar") }
                .filter { jarPath ->
                    val fileName = jarPath.fileName.toString()
                    excludeJars.none { fileName.startsWith(it) } || fileName.endsWith("-patched.jar")
                }
                .forEach { jarPath ->
                    addJarToFatJar(jarOut, jarPath)
                }
        }
    }

    private fun addJarToFatJar(jarOut: JarOutputStream, jarPath: Path) {
        val jarEntry = JarEntry(jarPath.fileName.toString())
        jarOut.putNextEntry(jarEntry)

        Files.newInputStream(jarPath).use { inputStream ->
            inputStream.copyTo(jarOut)
        }

        jarOut.closeEntry()
    }
}
