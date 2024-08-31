package net.rsprox.shared.settings

public interface SettingSet {
    public fun getCreationTime(): Long

    public fun getName(): String

    public fun setName(name: String)

    public fun deleteBackingFile()

    public operator fun get(setting: Setting): Boolean

    public operator fun set(
        setting: Setting,
        enabled: Boolean,
    )

    public fun set(
        category: SettingCategory,
        enabled: Boolean,
    )

    public fun set(
        group: SettingGroup,
        enabled: Boolean,
    )

    public fun setAll(enabled: Boolean)

    public fun setDefaults()
}
