package net.rsprox.proxy.loopback

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.proxy.config.CONFIGURATION_PATH
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import kotlin.concurrent.thread

internal object MacLoopbackAliases {
    private const val MAX_ALIASES = 8
    private const val IDLE_GRACE_MILLIS = 10_000L
    private const val POLL_INTERVAL_MILLIS = 300L
    private val whitespace = Regex("\\s+")
    private val loopbackAddress = Regex("^127(?:\\.(?:25[0-5]|2[0-4][0-9]|1?[0-9]?[0-9])){3}$")
    private val logger = InlineLogger()
    private val proxyPorts = mutableSetOf<Int>()
    private var desiredAliases: Map<String, Long> = emptyMap()
    private var desiredFile: Path? = null
    private var helperProcess: Process? = null
    private var poller: Thread? = null
    private var attempted = false

    @Volatile
    private var active = false

    internal data class ActiveWorldConnections(
        val all: Set<String>,
        val connecting: Set<String>,
    )

    @Synchronized
    internal fun registerProxyPort(port: Int) {
        proxyPorts += port
        if (attempted) return
        attempted = true
        try {
            desiredFile = Files.createTempFile(CONFIGURATION_PATH, "loopback-aliases-", ".txt")
        } catch (t: Throwable) {
            logger.error(t) { "Unable to create the macOS loopback alias state file" }
            return
        }
        active = true
        thread(isDaemon = true, name = "mac-loopback-helper") { runHelper() }
        poller = thread(isDaemon = true, name = "mac-loopback-poller") { pollConnections() }
    }

    @Synchronized
    internal fun shutdown() {
        active = false
        poller?.interrupt()
        desiredAliases = emptyMap()
        desiredFile?.let { file -> runCatching { writeDesiredAliases(file, emptySet()) } }
        helperProcess?.destroy()
        desiredFile?.let { file -> runCatching { Files.deleteIfExists(file) } }
        desiredFile = null
        proxyPorts.clear()
    }

    internal fun sessionHelperScript(): String =
        """
        set -u
        parent_pid=${'$'}1
        desired_file=${'$'}2
        applied=" "

        cleanup() {
            for address in ${'$'}applied; do
                /sbin/ifconfig lo0 -alias "${'$'}address" >/dev/null 2>&1 || true
            done
        }
        trap cleanup EXIT
        trap 'exit 0' INT TERM

        valid_address() {
            [ "${'$'}1" != "127.0.0.1" ] &&
                [[ "${'$'}1" =~ ^127\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])${'$'} ]]
        }

        while kill -0 "${'$'}parent_pid" 2>/dev/null; do
            desired=" "
            count=0
            if [ -r "${'$'}desired_file" ]; then
                while IFS= read -r address && [ "${'$'}count" -lt 8 ]; do
                    valid_address "${'$'}address" || continue
                    case "${'$'}desired" in *" ${'$'}address "*) continue ;; esac
                    desired="${'$'}desired${'$'}address "
                    count=${'$'}((count + 1))
                    case "${'$'}applied" in
                        *" ${'$'}address "*) ;;
                        *)
                            if /sbin/ifconfig lo0 alias "${'$'}address"; then
                                applied="${'$'}applied${'$'}address "
                            fi
                            ;;
                    esac
                done < "${'$'}desired_file"
            fi

            kept=" "
            for address in ${'$'}applied; do
                case "${'$'}desired" in
                    *" ${'$'}address "*) kept="${'$'}kept${'$'}address " ;;
                    *) /sbin/ifconfig lo0 -alias "${'$'}address" >/dev/null 2>&1 || true ;;
                esac
            done
            applied=${'$'}kept
            sleep 0.3
        done
        """.trimIndent()

    private fun runHelper() {
        val stateFile = synchronized(this) { desiredFile } ?: return
        val shellCommand =
            "/bin/bash -c ${shellQuote(sessionHelperScript())} rsprox-loopback-helper " +
                "${shellQuote(ProcessHandle.current().pid().toString())} ${shellQuote(stateFile.toString())}"
        val appleScript =
            "do shell script \"${escapeAppleScript(shellCommand)}\" " +
                "with prompt \"RSProx needs administrator access to route RuneLite worlds on this Mac.\" " +
                "with administrator privileges"
        try {
            val process =
                ProcessBuilder("/usr/bin/osascript", "-e", appleScript)
                    .redirectErrorStream(true)
                    .start()
            synchronized(this) {
                if (!active) {
                    process.destroy()
                    return
                }
                helperProcess = process
            }
            val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
            val exitCode = process.waitFor()
            if (active && exitCode != 0) {
                logger.warn { "macOS loopback setup was cancelled or failed: $output" }
            }
        } catch (t: Throwable) {
            if (active) logger.error(t) { "Unable to start macOS loopback setup" }
        } finally {
            synchronized(this) {
                helperProcess = null
                active = false
                poller?.interrupt()
            }
        }
    }

    private fun pollConnections() {
        try {
            while (active) {
                try {
                    val output =
                        ProcessBuilder("/usr/sbin/netstat", "-an", "-p", "tcp")
                            .redirectErrorStream(true)
                            .start()
                            .inputStream
                            .bufferedReader()
                            .use { it.readText() }
                    synchronized(this) {
                        if (!active) return
                        val next =
                            nextDesiredAliases(
                                desiredAliases,
                                parseActiveWorldConnections(output, proxyPorts.toSet()),
                                System.currentTimeMillis(),
                            )
                        if (next.keys != desiredAliases.keys) {
                            desiredFile?.let { writeDesiredAliases(it, next.keys) }
                        }
                        desiredAliases = next
                    }
                } catch (t: Throwable) {
                    if (t is InterruptedException) return
                    logger.error(t) { "Unable to inspect macOS loopback connections" }
                }
                Thread.sleep(POLL_INTERVAL_MILLIS)
            }
        } catch (_: InterruptedException) {
            // Shutdown.
        }
    }

    private fun writeDesiredAliases(
        file: Path,
        addresses: Set<String>,
    ) {
        val temporary = file.resolveSibling("${file.fileName}.tmp")
        Files.writeString(temporary, addresses.sorted().joinToString(separator = "\n", postfix = "\n"))
        Files.move(temporary, file, REPLACE_EXISTING, ATOMIC_MOVE)
    }

    private fun shellQuote(value: String): String = "'${value.replace("'", "'\"'\"'")}'"

    private fun escapeAppleScript(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")

    internal fun parseActiveWorldConnections(
        output: String,
        proxyPorts: Set<Int>,
    ): ActiveWorldConnections {
        val all = HashSet<String>()
        val connecting = HashSet<String>()
        for (line in output.lineSequence()) {
            val fields = line.trim().split(whitespace)
            if (fields.size < 6 || fields[0] != "tcp4") continue
            val foreign = parseForeignAddress(fields[4]) ?: continue
            if (foreign.second !in proxyPorts) continue
            all += foreign.first
            if (fields[5] == "SYN_SENT") connecting += foreign.first
        }
        return ActiveWorldConnections(all, connecting)
    }

    internal fun nextDesiredAliases(
        current: Map<String, Long>,
        active: ActiveWorldConnections,
        now: Long,
    ): Map<String, Long> {
        val next =
            current
                .filterNot { (address, lastSeen) ->
                    address !in active.all && now - lastSeen > IDLE_GRACE_MILLIS
                }.toMutableMap()
        for (address in active.all) {
            if (address in next) next[address] = now
        }
        for (address in active.connecting.sorted()) {
            if (address in next || next.size >= MAX_ALIASES) continue
            next[address] = now
        }
        return next
    }

    private fun parseForeignAddress(value: String): Pair<String, Int>? {
        val separator = value.lastIndexOf('.')
        if (separator <= 0) return null
        val address = value.substring(0, separator)
        val port = value.substring(separator + 1).toIntOrNull() ?: return null
        if (!loopbackAddress.matches(address) || address == "127.0.0.1") return null
        return address to port
    }
}
