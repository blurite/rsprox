package net.rsprox.proxy.util

public interface TranscribeCallback {
    public fun indeterminate(note: String)

    public fun report(
        percent: Int,
        note: String,
    )

    public fun isCancelled(): Boolean
}
