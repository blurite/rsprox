package net.rsprox.patch

public enum class NativeClientType(
    public val systemShortName: String,
) {
    WIN("win"),
    MAC("mac"),
    RUNELITE_JAR("runelite"),
}
