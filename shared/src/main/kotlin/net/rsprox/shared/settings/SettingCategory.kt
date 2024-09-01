package net.rsprox.shared.settings

public enum class SettingCategory(
    public val label: String,
) {
    PREFS("Preferences"),
    PLAYER_INFO("Player Info"),
    NPC_INFO("NPC Info"),
    MISC("Miscellaneous"),
}
