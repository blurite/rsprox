package net.rsprox.shared

public interface SessionMonitor<T> {
    public fun onLogin(header: T)

    public fun onLogout(header: T)

    public fun onNameUpdate(name: String)

    public fun onTranscribe(
        cycle: Int,
        message: String,
    )
}
