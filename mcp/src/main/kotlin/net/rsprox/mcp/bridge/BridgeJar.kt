package net.rsprox.mcp.bridge

import net.rsprox.proxy.target.ProxyTargetConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/** Puts the bridge plugin jar that this build embeds where the client of a target sideloads plugins from. */
public class BridgeJar internal constructor(
    private val overrideDir: Path?,
    private val home: Path,
    private val embedded: () -> ByteArray,
) {
    public constructor(overrideDir: Path?) : this(
        overrideDir,
        Path.of(System.getProperty("user.home")),
        {
            val resource =
                BridgeJar::class.java.getResourceAsStream("/$FILE_NAME")
                    ?: error("The bridge plugin jar is missing from this build")
            resource.use { it.readAllBytes() }
        },
    )

    /**
     * Installs the jar unless the installed one already has the same content. A client that has the
     * old jar open keeps reading it, because the new one is moved into place rather than written over it.
     */
    public fun installFor(target: ProxyTargetConfig): Path {
        // Custom targets run a client that keeps its files apart from those of the official one.
        val dir = overrideDir ?: home.resolve(if (target.id == 0) ".runelite" else ".rlcustom").resolve(SIDELOAD_DIR)
        val installed = dir.resolve(FILE_NAME)
        val wanted = embedded()
        if (Files.isRegularFile(installed) && Files.readAllBytes(installed).contentEquals(wanted)) return installed
        Files.createDirectories(dir)
        // The client loads every `.jar` in the directory, so the partial file must not look like one.
        val temp = Files.createTempFile(dir, FILE_NAME, ".tmp")
        Files.write(temp, wanted)
        Files.move(temp, installed, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        return installed
    }

    private companion object {
        private const val FILE_NAME = "rsprox-mcp-bridge.jar"
        private const val SIDELOAD_DIR = "sideloaded-plugins"
    }
}
