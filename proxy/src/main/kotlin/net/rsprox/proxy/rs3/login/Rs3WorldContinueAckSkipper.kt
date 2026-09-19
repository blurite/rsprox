package net.rsprox.proxy.rs3.login

public class Rs3WorldContinueAckSkipper {
    public var isDone: Boolean = false
        private set

    private var expected = false

    public fun skipForReconnect() {
        check(!expected && !isDone) { "Continue acknowledgement already started" }
        isDone = true
    }

    public fun expect() {
        check(!expected && !isDone) { "Duplicate world-login continue acknowledgement" }
        expected = true
    }

    public fun consume(chunk: ByteArray): ByteArray? {
        if (chunk.isEmpty()) return null
        if (isDone) return chunk
        check(expected) { "Client data before the world-login variable sequence completed" }
        check(chunk[0] == 0x1A.toByte()) { "Expected world-login continue acknowledgement" }
        isDone = true
        return chunk.copyOfRange(1, chunk.size)
    }
}
