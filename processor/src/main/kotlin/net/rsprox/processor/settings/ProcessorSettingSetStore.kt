package net.rsprox.processor.settings

import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingCategory
import net.rsprox.shared.settings.SettingGroup
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore

public object ProcessorSettingSetStore : SettingSetStore {
    override val size: Int
        get() = 1

    override fun create(name: String): SettingSet {
        throw UnsupportedOperationException("Cannot mutate processor settings set store.")
    }

    override fun delete(index: Int): SettingSet? {
        throw UnsupportedOperationException("Cannot mutate processor settings set store.")
    }

    override fun get(index: Int): SettingSet? {
        if (index != 0) {
            throw IndexOutOfBoundsException("Processor settings set store only has a single settings set.")
        }
        return PropertySettingSet
    }

    override fun getActive(): SettingSet {
        return PropertySettingSet
    }

    override fun setActive(index: Int) {
        throw UnsupportedOperationException("Cannot mutate processor settings set store.")
    }

    private object PropertySettingSet : SettingSet {
        override fun getCreationTime(): Long {
            return System.currentTimeMillis()
        }

        override fun getName(): String {
            return "Processor"
        }

        override fun setName(name: String) {
            throw UnsupportedOperationException("Cannot mutate processor settings set.")
        }

        override fun deleteBackingFile() {
            throw UnsupportedOperationException("Processor settings set does not have a backing file.")
        }

        override fun get(setting: Setting): Boolean {
            return true
        }

        override fun set(setting: Setting, enabled: Boolean) {
            throw UnsupportedOperationException("Cannot mutate processor settings set.")
        }

        override fun set(category: SettingCategory, enabled: Boolean) {
            throw UnsupportedOperationException("Cannot mutate processor settings set.")
        }

        override fun set(group: SettingGroup, enabled: Boolean) {
            throw UnsupportedOperationException("Cannot mutate processor settings set.")
        }

        override fun setAll(enabled: Boolean) {
            throw UnsupportedOperationException("Cannot mutate processor settings set.")
        }

        override fun setDefaults() {
            throw UnsupportedOperationException("Cannot mutate processor settings set.")
        }
    }
}
