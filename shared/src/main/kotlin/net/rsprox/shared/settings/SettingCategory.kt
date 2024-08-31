package net.rsprox.shared.settings

public enum class SettingCategory(
    public val label: String,
) {
    PREFS("Preferences"),
    PLAYER_INFO("Player info"),
    NPC_INFO("NPC info"),
    MISC("Miscellaneous"),
}
