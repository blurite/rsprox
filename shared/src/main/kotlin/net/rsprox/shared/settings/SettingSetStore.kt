package net.rsprox.shared.settings

public interface SettingSetStore {
    public val size: Int

    public fun create(name: String): SettingSet

    public fun delete(index: Int): SettingSet?

    public fun get(index: Int): SettingSet?

    public fun getActive(): SettingSet

    public fun setActive(index: Int)
}
