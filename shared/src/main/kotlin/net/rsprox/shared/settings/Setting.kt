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
        false,
        "When enabled, uses single quote whenever logging string-types. Otherwise, sticks to double quote.",
    ),
    HIDE_UNNECESSARY_VARPS(
        SettingGroup.LOGGING,
        SettingCategory.MISC,
        "Hide Unnecessary Varps",
        true,
        "Hides the varp packet header if all the bit changes can be explained by varbits.",
    ),
    COLLAPSE_CLIENTSCRIPT_PARAMS(
        SettingGroup.LOGGING,
        SettingCategory.MISC,
        "Collapse Clientscript Params",
        true,
        "Collapses clientscript params & types by putting them all in one line.",
    ),
}
