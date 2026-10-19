package net.rsprox.proxy.rs3.replay

import net.rsprox.cache.rs3.Rs3ReplayGroupStore
import net.rsprox.patch.NativeClientType
import net.rsprox.patch.PatchResult
import net.rsprox.patch.native.NativePatchCriteria
import net.rsprox.patch.native.NativePatcher
import net.rsprox.proxy.rs3.Rs3LaunchProgress
import net.rsprox.proxy.rs3.Rs3ProtocolRevision
import net.rsprox.proxy.rs3.config.Rs3JavConfig
import net.rsprox.proxy.rs3.launcher.Rs3OfficialLauncher
import net.rsprox.proxy.rs3.relay.Rs3RelayPorts
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

/** Owns only this replay's listeners and launcher. Authentication never leaves these listeners. */
public class Rs3ReplayClient private constructor(
    public val server: Rs3ReplayServer,
    private val http: Rs3ReplayHttpServer,
    private val launcher: Rs3OfficialLauncher,
    private val patchedClient: Path,
) : AutoCloseable {
    private val closed = AtomicBoolean()
    public val isClosed: Boolean get() = closed.get()
    internal val executable: Path get() = launcher.executable
    internal val arguments: List<String> get() = launcher.arguments

    internal fun attach(
        process: ProcessHandle,
        onClient: (ProcessHandle) -> Unit,
    ) = launcher.attach(process, onClient)

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        try {
            launcher.close()
        } finally {
            try {
                http.close()
            } finally {
                server.close()
                Files.deleteIfExists(patchedClient)
            }
        }
    }

    public companion object {
        internal fun prepare(
            recording: Rs3ReplayRecording,
            sourceClient: Path,
            configuration: Rs3JavConfig,
            directory: Path,
            ports: Rs3RelayPorts,
            key: RSAPrivateCrtKeyParameters,
            onProgress: (Rs3LaunchProgress) -> Unit,
            onFailure: (Throwable) -> Unit,
        ): Rs3ReplayClient {
            val revision = Rs3ProtocolRevision(recording.header.revision)
            require(
                configuration.getServerVersion() == revision.wireRevision,
            ) { "Replay client configuration revision mismatch" }
            Files.createDirectories(directory)
            val patched = Files.createTempFile(directory, "replay-", ".exe")
            val cache = Rs3ReplayGroupStore(recording.header.js5MasterIndex)
            val resources =
                try {
                    Rs3ReplayHttpServer.bind(recording.header.worldId, cache, onFailure)
                } catch (error: Exception) {
                    cache.close()
                    Files.deleteIfExists(patched)
                    throw error
                }
            val host = resources.host
            val server = Rs3ReplayServer(recording, key, host, ports, cache, onFailure)
            var launcher: Rs3OfficialLauncher? = null
            try {
                Files.copy(sourceClient, patched, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                onProgress(Rs3LaunchProgress("Patching replay client"))
                val criteria =
                    NativePatchCriteria
                        .Builder(NativeClientType.RS3_WIN)
                        .rsaModulus(key.modulus.toString(16))
                        .rs3LoginPorts(ports.primary, ports.alternate, recording.header.revision)
                        .rs3WindowTitle()
                        .build()
                check(
                    NativePatcher().patch(patched, criteria) is PatchResult.Success,
                ) { "RS3 replay client patch failed" }
                onProgress(Rs3LaunchProgress("Starting on-demand replay cache"))
                server.start()
                resources.start()
                // 37 is the JS5 socket hostname; 49 is the HTTP resource hostname (port 80).
                val params = mutableMapOf(3 to host, 37 to host, 49 to host)
                for (id in 41..48) params[id] = (if (id % 2 == 1) ports.primary else ports.alternate).toString()
                // Reuse native assets across recordings, separately from live play. The single launcher
                // slot below keeps the shared cache and preferences exclusive until the client exits.
                val rewritten =
                    configuration.text
                        .lineSequence()
                        .map { line ->
                            when {
                                line.startsWith("cache_variant_suffix=") -> "cache_variant_suffix=replay"
                                line.startsWith("param=") -> {
                                    val id = line.substringAfter("param=").substringBefore('=').toIntOrNull()
                                    val value = line.substringAfter('=', "").substringAfter('=', "")
                                    val replacement =
                                        params[id]
                                            ?: if (value.startsWith("http://") ||
                                                value.startsWith("https://")
                                            ) {
                                                resources.url + "/"
                                            } else {
                                                null
                                            }
                                    if (replacement != null) "param=$id=$replacement" else line
                                }
                                else -> line
                            }
                        }.toMutableList()
                if (rewritten.none { it.startsWith("cache_variant_suffix=") }) {
                    rewritten +=
                        "cache_variant_suffix=replay"
                }
                val prepared =
                    Rs3OfficialLauncher.prepare(
                        directory,
                        patched,
                        Rs3JavConfig(rewritten.joinToString("\n")),
                        host,
                        singleInstance = true,
                        onProgress = onProgress,
                    )
                launcher = prepared
                return Rs3ReplayClient(server, resources, prepared, patched)
            } catch (error: Exception) {
                launcher?.close()
                resources.close()
                server.close()
                Files.deleteIfExists(patched)
                throw error
            }
        }
    }
}
