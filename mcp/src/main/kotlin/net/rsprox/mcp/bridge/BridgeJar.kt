package net.rsprox.mcp.bridge

import net.rsprox.proxy.target.ProxyTargetConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/** Installs the embedded bridge plugin jar into the sideload directory of a target's client. */
public class BridgeJar internal constructor(
    /** The sideload directory to use for every target, or null for the default of each target. */
    private val overrideDir: Path?,
    /** The home directory that holds the client's files. */
    private val home: Path,
    /** The reader of the jar that this build embeds. */
    private val embedded: () -> ByteArray,
) {
    /** Create an installer of the jar that ships with this build, for the clients of the current user. */
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
     * Install the jar unless the installed one already has the same content. A client that has the
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
        /** The name of the jar, both as a resource and as installed. */
        private const val FILE_NAME = "rsprox-mcp-bridge.jar"

        /** The name of the directory, under the client's own, that plugins are sideloaded from. */
        private const val SIDELOAD_DIR = "sideloaded-plugins"
    }
}
