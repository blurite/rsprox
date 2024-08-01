package net.rsprox.proxy.filters

import net.rsprox.shared.StreamDirection
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.ProtCategory

public class UnmodifiablePropertyFilterSet : PropertyFilterSet {
    override fun getCreationTime(): Long {
        return Long.MIN_VALUE
    }

    override fun getName(): String {
        return "Default"
    }

    override fun setName(name: String) {
    }

    override fun get(filter: PropertyFilter): Boolean {
        return filter.enabled
    }

    override fun set(
        filter: PropertyFilter,
        enabled: Boolean,
    ) {
    }

    override fun set(
        category: ProtCategory,
        enabled: Boolean,
    ) {
    }

    override fun set(
        streamDirection: StreamDirection,
        enabled: Boolean,
    ) {
    }

    override fun setAll(enabled: Boolean) {
    }

    override fun setDefaults() {
    }
}
