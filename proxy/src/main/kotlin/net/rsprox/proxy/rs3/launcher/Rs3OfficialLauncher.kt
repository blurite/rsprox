package net.rsprox.proxy.rs3.launcher

import com.sun.jna.platform.win32.Shell32Util
import com.sun.jna.platform.win32.ShlObj
import net.rsprox.proxy.rs3.Rs3LaunchProgress
import net.rsprox.proxy.rs3.config.Rs3JavConfig
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/** The real launcher owns IPC and preferences; RSProx owns executable selection and process lifetime. */
internal class Rs3OfficialLauncher private constructor(
    private val slot: Rs3LauncherSlot,
    private val config: Rs3LauncherConfigServer,
    private val onProgress: (Rs3LaunchProgress) -> Unit,
) : AutoCloseable {
    private val closed = AtomicBoolean()

    @Volatile
    private var bootstrap: ProcessHandle? = null

    @Volatile
    private var client: ProcessHandle? = null
    val executable: Path get() = slot.launcher
    val arguments: List<String> get() = listOf("--configURI", config.url)

    fun attach(
        process: ProcessHandle,
        onClient: (ProcessHandle) -> Unit,
    ) {
        synchronized(this) {
            if (closed.get()) {
                process.destroyForcibly()
                error("RuneScape launcher startup was cancelled")
            }
            check(bootstrap == null) { "RuneScape launcher already attached" }
            bootstrap = process
        }
        try {
            onProgress(Rs3LaunchProgress("Waiting for RuneScape launcher"))
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30)
            var game: ProcessHandle? = null
            while (game == null) {
                check(!closed.get() && process.isAlive) {
                    "RuneScape launcher exited before starting the game"
                }
                game =
                    process.descendants().use { descendants ->
                        descendants
                            .filter { child ->
                                child
                                    .info()
                                    .command()
                                    .map { command ->
                                        Path.of(command).toAbsolutePath().normalize() ==
                                            slot.client.toAbsolutePath().normalize()
                                    }.orElse(false)
                            }.findFirst()
                            .orElse(null)
                    }
                check(System.nanoTime() < deadline) {
                    if (config.requested.get()) {
                        "RuneScape launcher did not start its verified client; check its error window"
                    } else {
                        "RuneScape launcher did not request its local configuration"
                    }
                }
                if (game == null) Thread.sleep(25)
            }
            val child = game
            synchronized(this) {
                client = child
                if (closed.get()) {
                    child.destroyForcibly()
                    error("RuneScape launcher startup was cancelled")
                }
            }
            onClient(child)
            // Normally the bootstrap exits with the game, after saving its settings. Bound a stuck
            // bootstrap without cutting off its normal close/save handshake.
            child.onExit().thenRun {
                CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute {
                    if (process.isAlive) close()
                }
            }
            onProgress(Rs3LaunchProgress("Initializing RuneScape client"))
            val initializationDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(300)
            while (Rs3ClientWindow.find(child.pid()) == null) {
                check(!closed.get() && process.isAlive && child.isAlive) { "RuneScape client exited during startup" }
                check(System.nanoTime() < initializationDeadline) { "RuneScape client initialization timed out" }
                Thread.sleep(50)
            }
        } catch (e: Throwable) {
            close()
            throw e
        }
    }

    override fun close() {
        val owned =
            synchronized(this) {
                if (!closed.compareAndSet(false, true)) return
                val parent = bootstrap
                val descendants =
                    parent?.descendants()?.use { it.toArray().map { value -> value as ProcessHandle } }.orEmpty()
                (descendants + listOfNotNull(client, parent)).distinctBy { it.pid() }
            }
        try {
            for (process in owned) if (process.isAlive) process.destroyForcibly()
        } finally {
            try {
                config.close()
            } finally {
                // Do not release the slot while an executable can still be running from it.
                CompletableFuture.allOf(*owned.map { it.onExit() }.toTypedArray()).whenComplete { _, _ -> slot.close() }
            }
        }
    }

    companion object {
        fun prepare(
            directory: Path,
            patchedClient: Path,
            javConfig: Rs3JavConfig,
            host: String,
            onProgress: (Rs3LaunchProgress) -> Unit,
        ): Rs3OfficialLauncher {
            val source = Rs3LauncherDistribution.load(directory, onProgress)
            val cacheRoot = Path.of(Shell32Util.getFolderPath(ShlObj.CSIDL_COMMON_APPDATA), "Jagex")
            val slot = Rs3LauncherSlot.acquire(directory, cacheRoot)
            var server: Rs3LauncherConfigServer? = null
            try {
                onProgress(Rs3LaunchProgress("Preparing RuneScape launcher"))
                val patch = Rs3LauncherPatch()
                val bytes = Files.readAllBytes(patchedClient)
                Files.write(slot.client, bytes)
                Files.write(slot.launcher, patch.patch(source, slot.name))
                val config = Rs3LauncherConfigServer(host)
                server = config
                config.start(patch.configuration(javConfig, bytes, config.codebase))
                return Rs3OfficialLauncher(slot, config, onProgress)
            } catch (e: Throwable) {
                try {
                    server?.close()
                } finally {
                    slot.close()
                }
                throw e
            }
        }
    }
}
