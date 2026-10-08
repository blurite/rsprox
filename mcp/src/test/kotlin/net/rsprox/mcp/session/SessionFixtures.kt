package net.rsprox.mcp.session

import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.target.ProxyTargetConfig
import net.rsprox.shared.SessionMonitor
import java.util.concurrent.CopyOnWriteArrayList

internal fun target(
    id: Int,
    name: String,
): ProxyTargetConfig =
    ProxyTargetConfig(
        id = id,
        name = name,
        javConfigUrl = "http://127.0.0.1/jav_config.ws",
        modulus = null,
        varpCount = 5000,
        revision = null,
        runeliteBootstrapUrl = null,
        runeliteBootstrapCommitHash = null,
        runeliteGamepackUrl = null,
        binaryFolder = null,
    )

/** The header of a login to world 301, which the proxy hands to a session monitor. */
internal val loginHeader =
    BinaryHeader(
        headerVersion = 1,
        revision = 235,
        subRevision = 1,
        clientType = 1,
        platformType = 1,
        timestamp = 1_700_000_000_000,
        worldId = 301,
        worldFlags = 0,
        worldLocation = 0,
        worldHost = "127.0.1.3",
        worldActivity = "",
        localPlayerIndex = 7,
        accountHash = ByteArray(0),
        clientName = "RuneLite",
        js5MasterIndex = ByteArray(0),
    )

internal class FakeLauncher : ClientLauncher {
    var targets = listOf(target(0, "Old School RuneScape"), target(1, "My Server"))
    val reserved = ArrayList<ProxyTargetConfig>()

    // A client that goes away is killed from the bridge's reader thread.
    val killed = CopyOnWriteArrayList<Int>()
    var launch: () -> Unit = {}
    var launcherExited: () -> Boolean = { false }
    var bridged = true

    // The packet tap of the newest launch, which a test drives the way the proxy does.
    lateinit var monitor: SessionMonitor<BinaryHeader>

    override fun targets(): List<ProxyTargetConfig> = targets

    override fun reserve(target: ProxyTargetConfig): Reservation {
        reserved += target
        val earlier = reserved.size - 1

        return Reservation(
            FIRST_PROXY_PORT + earlier,
            FIRST_HTTP_PORT + earlier,
            { tap ->
                monitor = tap
                launch()
            },
            { launcherExited() },
            bridged,
        )
    }

    override fun kill(proxyPort: Int) {
        killed += proxyPort
    }

    companion object {
        const val FIRST_PROXY_PORT = 43751
        const val FIRST_HTTP_PORT = 43650
    }
}
