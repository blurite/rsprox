package net.rsprox.shared.settings

public object NopSettingSet : SettingSet {
    override fun getCreationTime(): Long = throw UnsupportedOperationException()

    override fun getName(): String = throw UnsupportedOperationException()

    override fun setName(name: String) {
        throw UnsupportedOperationException()
    }

    override fun deleteBackingFile() {
        throw UnsupportedOperationException()
    }

    override fun get(setting: Setting): Boolean {
        return false
    }

    override fun set(
        setting: Setting,
        enabled: Boolean,
    ) {
        throw UnsupportedOperationException()
    }

    override fun set(
        category: SettingCategory,
        enabled: Boolean,
    ) {
        throw UnsupportedOperationException()
    }

    override fun set(
        group: SettingGroup,
        enabled: Boolean,
    ) {
        throw UnsupportedOperationException()
    }

    override fun setAll(enabled: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun setDefaults() {
        throw UnsupportedOperationException()
    }
}
