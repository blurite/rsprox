package net.rsprox.shared.settings

public enum class Setting(
    public val group: SettingGroup,
    public val category: SettingCategory,
    public val label: String,
    public val enabled: Boolean,
    public val tooltip: String? = null,
) {
    PREFER_SINGLE_QUOTE_ON_STRINGS(
        SettingGroup.LOGGING,
        SettingCategory.PREFS,
        "Prefer single quote",
        true,
        "When enabled, uses single quote whenever logging string-types. Otherwise, sticks to double quote.",
    ),
}
