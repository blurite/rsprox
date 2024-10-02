package net.rsprox.shared.filters

import net.rsprox.shared.StreamDirection

public interface PropertyFilterSet {
    public fun getCreationTime(): Long

    public fun getName(): String

    public fun setName(name: String)

    public fun deleteBackingFile()

    public operator fun get(filter: PropertyFilter): Boolean

    public operator fun set(
        filter: PropertyFilter,
        enabled: Boolean,
    )

    public fun set(
        category: ProtCategory,
        enabled: Boolean,
    )

    public fun set(
        streamDirection: StreamDirection,
        enabled: Boolean,
    )

    public fun setAll(enabled: Boolean)

    public fun setDefaults()

    public fun getRegexFilters(): List<RegexFilter>

    public fun addRegexFilter(regexFilter: RegexFilter)

    public fun removeRegexFilter(regexFilter: RegexFilter)

    public fun replaceRegexFilter(oldRegexFilter: RegexFilter, newRegexFilter: RegexFilter)

    public fun clearRegexFilters()
}
