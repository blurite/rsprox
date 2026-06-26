package net.rsprox.proxy.loopback

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.proxy.config.CONFIGURATION_PATH
import net.rsprox.proxy.util.OperatingSystem
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.concurrent.thread
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

/**
 * Automatically manages loopback (`lo0`) aliases on macOS so the proxy's per-world
 * loopback addresses (`127.x.x.x`) are reachable without the user having to run the
 * manual `ifconfig lo0 alias` script.
 *
 * Unlike Linux and Windows, macOS only assigns `127.0.0.1` to `lo0` by default and
 * does not treat the rest of `127.0.0.0/8` as local - every address the client
 * connects to must be explicitly aliased, which requires root.
 *
 * Adding an alias is the only privileged step, and it is confined to a **session-scoped
 * helper**: the first time an alias is needed RSProx shows a single macOS administrator
 * prompt that launches a small root helper for the lifetime of that RSProx run. The
 * helper reconciles `lo0` to a desired-state file RSProx writes (adding/removing
 * `127.0.0.0/8` aliases only) and removes every alias again the moment RSProx exits or
 * dies. Nothing is installed, no `sudoers` entry is written, and no standing password-less
 * root access is left behind.
 *
 * Because macOS source-address selection iterates every interface address on each
 * outbound connection, aliases are added strictly **on demand**: a poller watches
 * `netstat` for the client trying to reach the proxy (a `SYN_SENT` socket on a proxy
 * port), so typically only the world currently in use is aliased. A hard cap and idle
 * grace bound the live count so the system-wide DNS slowdown cannot come back. Keying off
 * the proxy port means this works for any target, including private servers.
 */
public object MacLoopbackAliases {
    private val logger = InlineLogger()
    private val lock = Any()

    /** World loopback addresses that should currently be aliased, mapped to last-active time. */
    private val desired = HashMap<String, Long>()

    /** Ports the proxy listens on; a `SYN_SENT` to one of these is a client connecting through us. */
    private val proxyPorts = HashSet<Int>()
    private var pollerThread: Thread? = null
    private var helperLaunched = false
    private var lastWritten: Set<String> = emptySet()

    @Volatile
    private var maxAliases: Int = DEFAULT_MAX_ALIASES

    @Volatile
    private var graceMillis: Long = DEFAULT_GRACE_SECONDS * 1000L

    private const val POLL_INTERVAL_MS = 300L
    public const val DEFAULT_MAX_ALIASES: Int = 8
    public const val DEFAULT_GRACE_SECONDS: Int = 10
    private val helperScriptFile: Path = CONFIGURATION_PATH.resolve("rsprox-loopback-session.sh")
    private val desiredFile: Path = CONFIGURATION_PATH.resolve("rsprox-loopback-desired.txt")

    private const val OCTET = "(25[0-5]|2[0-4][0-9]|1?[0-9]?[0-9])"
    private val loopbackRegex = Regex("^127\\.$OCTET\\.$OCTET\\.$OCTET$")
    private val whitespace = Regex("\\s+")

    public fun isSupported(operatingSystem: OperatingSystem): Boolean {
        return operatingSystem == OperatingSystem.MAC
    }

    /**
     * Registers a proxy port so the poller recognises clients connecting through it,
     * applies the alias cap and idle grace, launches the session helper (one admin
     * prompt) if it is not already running, and starts the on-demand poller.
     */
    public fun registerProxyPort(
        port: Int,
        maxAliases: Int,
        graceSeconds: Int,
    ) {
        runCatchingLogged("register proxy port for loopback aliasing") {
            synchronized(lock) {
                this.maxAliases = maxAliases.coerceAtLeast(1)
                this.graceMillis = graceSeconds.coerceAtLeast(1) * 1000L
                proxyPorts.add(port)
                if (!helperLaunched && !launchSessionHelper()) {
                    logger.error {
                        "macOS loopback helper was not started; worlds will be unreachable. " +
                            "Re-open the client to retry, or add the aliases manually (see README)."
                    }
                    return@synchronized
                }
                startPollerLocked()
            }
        }
    }

    /**
     * Stops the poller and asks the session helper to remove every alias. The helper also
     * removes them by itself once this process exits, so cleanup is guaranteed either way.
     */
    public fun shutdown() {
        runCatchingLogged("release loopback aliases") {
            val thread: Thread?
            synchronized(lock) {
                thread = pollerThread
                pollerThread = null
                desired.clear()
                if (helperLaunched) {
                    // An empty desired set makes the helper drop every alias promptly.
                    writeDesiredStateLocked()
                }
                helperLaunched = false
            }
            thread?.interrupt()
        }
    }

    private fun startPollerLocked() {
        if (pollerThread != null) return
        pollerThread =
            thread(start = true, isDaemon = true, name = "rsprox-loopback-poller") {
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        pollOnce()
                    } catch (t: Throwable) {
                        logger.debug(t) { "Loopback alias poll failed" }
                    }
                    try {
                        Thread.sleep(POLL_INTERVAL_MS)
                    } catch (_: InterruptedException) {
                        break
                    }
                }
            }
    }

    private fun pollOnce() {
        val ports: Set<Int>
        synchronized(lock) {
            if (proxyPorts.isEmpty()) return
            ports = proxyPorts.toSet()
        }
        val output = runCommand(listOf("/usr/sbin/netstat", "-an", "-p", "tcp")) ?: return
        val active = parseActiveWorldConnections(output, ports)
        synchronized(lock) {
            val now = System.currentTimeMillis()
            // A world the client is trying to reach should be aliased.
            for (ip in active.connecting) {
                desired.putIfAbsent(ip, now)
            }
            // Keep worlds with live sockets fresh.
            for (ip in active.all) {
                if (ip in desired) desired[ip] = now
            }
            // Drop worlds idle past the grace period or over the hard cap.
            val toRemove = computeRemovals(desired, active.all, now, maxAliases, graceMillis)
            desired.keys.removeAll(toRemove)
            writeDesiredStateLocked()
        }
    }

    private fun writeDesiredStateLocked() {
        val current = desired.keys.toSet()
        if (current == lastWritten) return
        try {
            Files.createDirectories(desiredFile.parent)
            val tmp = desiredFile.resolveSibling("${desiredFile.fileName}.tmp")
            tmp.writeText(current.joinToString("\n"))
            Files.move(tmp, desiredFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            lastWritten = current
        } catch (t: Throwable) {
            logger.warn(t) { "Unable to write loopback desired-state file" }
        }
    }

    /**
     * Decides which desired aliases to drop this tick: those idle (no live socket) for
     * longer than [graceMillis], plus the oldest idle ones needed to get back under
     * [maxAliases]. Worlds with a live socket ([active]) are never removed.
     */
    internal fun computeRemovals(
        desired: Map<String, Long>,
        active: Set<String>,
        now: Long,
        maxAliases: Int,
        graceMillis: Long,
    ): Set<String> {
        val idleOldestFirst = desired.entries.filter { it.key !in active }.sortedBy { it.value }
        val expired = idleOldestFirst.filter { now - it.value > graceMillis }.map { it.key }
        val overCapCount = (desired.size - maxAliases).coerceAtLeast(0)
        val overCap = idleOldestFirst.take(overCapCount).map { it.key }
        return (expired + overCap).toSet()
    }

    internal data class ActiveWorldConnections(
        val all: Set<String>,
        val connecting: Set<String>,
    )

    /**
     * Parses `netstat -an -p tcp` output, returning the world loopback addresses with a
     * live socket to one of [proxyPorts], and the subset that are mid-connection
     * (`SYN_SENT`). Filtering by the proxy port - rather than a known world list - means
     * any target works, including private servers.
     */
    internal fun parseActiveWorldConnections(
        output: String,
        proxyPorts: Set<Int>,
    ): ActiveWorldConnections {
        val all = HashSet<String>()
        val connecting = HashSet<String>()
        for (line in output.lineSequence()) {
            val fields = line.trim().split(whitespace)
            if (fields.size < 6 || fields[0] != "tcp4") continue
            val foreign = parseForeign(fields[4]) ?: continue
            if (foreign.second !in proxyPorts) continue
            all.add(foreign.first)
            if (fields[5] == "SYN_SENT") connecting.add(foreign.first)
        }
        return ActiveWorldConnections(all, connecting)
    }

    private fun parseForeign(foreignAddress: String): Pair<String, Int>? {
        // macOS formats the foreign address as "127.1.44.2.43701" (ip then port).
        val portSeparator = foreignAddress.lastIndexOf('.')
        if (portSeparator <= 0) return null
        val ip = foreignAddress.substring(0, portSeparator)
        val port = foreignAddress.substring(portSeparator + 1).toIntOrNull() ?: return null
        if (!isManageable(ip)) return null
        return ip to port
    }

    private fun isManageable(address: String): Boolean {
        return loopbackRegex.matches(address) && address != "127.0.0.1"
    }

    private fun launchSessionHelper(): Boolean {
        try {
            Files.createDirectories(CONFIGURATION_PATH)
            // Start from a clean desired set so a fresh helper does not inherit stale state.
            desired.clear()
            lastWritten = emptySet()
            desiredFile.writeText("")
            helperScriptFile.writeText(sessionHelperScript())
            val pid = ProcessHandle.current().pid().toString()
            val shellCommand =
                "nohup /bin/bash ${shellQuote(helperScriptFile.absolutePathString())} " +
                    "${shellQuote(pid)} ${shellQuote(desiredFile.absolutePathString())} >/dev/null 2>&1 &"
            val appleScript =
                "do shell script \"${escapeForAppleScript(shellCommand)}\" " +
                    "with prompt \"${escapeForAppleScript(HELPER_PROMPT)}\" with administrator privileges"
            logger.info { "Requesting administrator approval to manage loopback aliases for this session" }
            val result = runCommand(listOf("/usr/bin/osascript", "-e", appleScript))
            if (result == null) {
                logger.error { "macOS loopback helper launch was declined or failed" }
                return false
            }
            helperLaunched = true
            return true
        } catch (t: Throwable) {
            logger.error(t) { "Unable to launch the macOS loopback helper" }
            return false
        }
    }

    internal fun sessionHelperScript(): String {
        return """
            |#!/bin/bash
            |# RSProx session loopback helper. Runs as root only while RSProx is open.
            |# Reconciles lo0 127.0.0.0/8 aliases to the desired-state file written by RSProx
            |# and removes all of them when RSProx exits. Installs nothing, leaves nothing behind.
            |set -u
            |ppid="${'$'}{1:-}"
            |desired_file="${'$'}{2:-}"
            |[ -n "${'$'}ppid" ] && [ -n "${'$'}desired_file" ] || exit 2
            |applied=" "
            |cleanup() {
            |  for ip in ${'$'}applied; do
            |    [ -n "${'$'}ip" ] && /sbin/ifconfig lo0 -alias "${'$'}ip" 2>/dev/null
            |  done
            |  exit 0
            |}
            |trap cleanup EXIT INT TERM
            |valid() {
            |  echo "${'$'}1" | /usr/bin/grep -Eq '^127\.(25[0-5]|2[0-4][0-9]|1?[0-9]?[0-9])(\.(25[0-5]|2[0-4][0-9]|1?[0-9]?[0-9])){2}${'$'}'
            |}
            |while kill -0 "${'$'}ppid" 2>/dev/null; do
            |  d=" "
            |  if [ -f "${'$'}desired_file" ]; then
            |    while IFS= read -r ip || [ -n "${'$'}ip" ]; do
            |      [ -n "${'$'}ip" ] || continue
            |      [ "${'$'}ip" = "127.0.0.1" ] && continue
            |      valid "${'$'}ip" || continue
            |      case "${'$'}d" in *" ${'$'}ip "*) ;; *) d="${'$'}d${'$'}ip " ;; esac
            |    done < "${'$'}desired_file"
            |  fi
            |  for ip in ${'$'}d; do
            |    case "${'$'}applied" in *" ${'$'}ip "*) ;; *)
            |      /sbin/ifconfig lo0 alias "${'$'}ip" 2>/dev/null && applied="${'$'}applied${'$'}ip " ;;
            |    esac
            |  done
            |  new=" "
            |  for ip in ${'$'}applied; do
            |    [ -n "${'$'}ip" ] || continue
            |    case "${'$'}d" in
            |      *" ${'$'}ip "*) new="${'$'}new${'$'}ip " ;;
            |      *) /sbin/ifconfig lo0 -alias "${'$'}ip" 2>/dev/null ;;
            |    esac
            |  done
            |  applied="${'$'}new"
            |  /bin/sleep 0.3
            |done
            |# Parent exited; the EXIT trap removes every alias we added.
            |
            """.trimMargin()
    }

    private fun shellQuote(value: String): String {
        return "'" + value.replace("'", "'\\''") + "'"
    }

    private fun escapeForAppleScript(value: String): String {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
    }

    private inline fun <T> runCatchingLogged(
        action: String,
        block: () -> T,
    ): T? {
        return try {
            block()
        } catch (t: Throwable) {
            logger.error(t) { "Unable to $action" }
            null
        }
    }

    private fun runCommand(command: List<String>): String? {
        return try {
            val process =
                ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start()
            val output = process.inputStream.bufferedReader().readText()
            val exit = process.waitFor()
            if (exit != 0) {
                logger.debug { "Command ${command.first()} exited with $exit: ${output.trim()}" }
                null
            } else {
                output
            }
        } catch (t: Throwable) {
            logger.debug(t) { "Unable to run command $command" }
            null
        }
    }

    private const val HELPER_PROMPT =
        "RSProx needs administrator access to add temporary loopback (lo0) network " +
            "aliases so the game client can reach worlds. They are scoped to 127.0.0.0/8 " +
            "and removed automatically when RSProx closes."
}
