package net.rsprox.patch

public enum class NativeClientType(
    public val systemShortName: String,
) {
    WIN("win"),
    MAC("mac"),
    RS3_WIN("rs3-win"),
}
