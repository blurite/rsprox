package net.rsprox.cache.clientscript

public enum class ClientScriptGame(
    internal val archiveGame: String,
    internal val namespace: String,
) {
    OLD_SCHOOL("oldschool", ""),
    RUNESCAPE("runescape", "rs3"),
}
