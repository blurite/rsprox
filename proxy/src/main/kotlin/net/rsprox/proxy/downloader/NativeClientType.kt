package net.rsprox.proxy.downloader

public enum class NativeClientType(
    public val systemShortName: String,
) {
    WIN("win"),
    MAC("mac"),
}
