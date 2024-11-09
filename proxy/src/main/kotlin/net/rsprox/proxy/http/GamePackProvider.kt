package net.rsprox.proxy.http

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

public class GamePackProvider {
    private var lastFetchTime = TimeSource.Monotonic.markNow()
    private var lastPayload: ByteArray? = null

    @Volatile
    private var currentTask: ForkJoinTask<*>? = null

    internal fun prefetch(await: Boolean = false) {
        val lastPayload = this.lastPayload
        if (lastPayload == null || lastFetchTime <= TimeSource.Monotonic.markNow().minus(5.minutes)) {
            val task = currentTask
            // Wait for the old task to finish
            if (task != null) {
                if (!await) {
                    task.get()
                }
                return
            }
            this.lastFetchTime = TimeSource.Monotonic.markNow()
            if (await) {
                currentTask = ForkJoinPool.commonPool().submit { fetch() }
            } else {
                fetch()
            }
        }
    }

    private fun fetch() {
        val randomValue = Random.Default.nextInt(1_000_000..7_000_000)
        val forwarded = URL("http://oldschool1.runescape.com/gamepack_$randomValue.jar")
        val con = forwarded.openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        this.lastPayload = con.inputStream.readAllBytes()
        this.currentTask = null
    }

    internal fun get(): ByteArray {
        prefetch(await = true)
        return checkNotNull(lastPayload) {
            "Payload unavailable."
        }
    }
}
