package net.rsprox.proxy.rs3.login

public interface Rs3LoginResponseFramer {
    public val isDone: Boolean

    public fun consume(chunk: ByteArray): ByteArray?
}
