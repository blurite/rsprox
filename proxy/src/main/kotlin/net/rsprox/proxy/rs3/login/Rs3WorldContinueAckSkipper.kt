package net.rsprox.proxy.rs3.login

public class Rs3WorldContinueAckSkipper {
    public var isDone: Boolean = false
        private set

    private var scanned = 0

    public fun consume(chunk: ByteArray): ByteArray? {
        for (i in chunk.indices) {
            scanned++
            if (chunk[i] == 0x1A.toByte()) {
                isDone = true
                return chunk.copyOfRange(i + 1, chunk.size)
            }
            if (scanned >= 8) {
                isDone = true
                return chunk.copyOfRange(i + 1, chunk.size)
            }
        }
        return null
    }
}
