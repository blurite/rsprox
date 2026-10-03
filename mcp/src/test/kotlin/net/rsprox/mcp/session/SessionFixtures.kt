package net.rsprox.mcp.session

import net.rsprox.proxy.target.ProxyTargetConfig
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

internal class FakeLauncher(
    private val targets: List<ProxyTargetConfig> = listOf(target(0, "Old School RuneScape"), target(1, "My Server")),
) : ClientLauncher {
    val reserved = ArrayList<ProxyTargetConfig>()

    // A client that goes away is killed from the bridge's reader thread.
    val killed = CopyOnWriteArrayList<Int>()
    var reserve: (ProxyTargetConfig) -> Unit = {}
    var launch: () -> Unit = {}
    var launcherExited: () -> Boolean = { false }

    override fun targets(): List<ProxyTargetConfig> = targets

    override fun reserve(target: ProxyTargetConfig): Reservation {
        reserve.invoke(target)
        reserved += target
        val earlier = reserved.size - 1

        return Reservation(FIRST_PROXY_PORT + earlier, FIRST_HTTP_PORT + earlier, { launch() }, { launcherExited() })
    }

    override fun kill(proxyPort: Int) {
        killed += proxyPort
    }

    companion object {
        const val FIRST_PROXY_PORT = 43751
        const val FIRST_HTTP_PORT = 43650
    }
}
