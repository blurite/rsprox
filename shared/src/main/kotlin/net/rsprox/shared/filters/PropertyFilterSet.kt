package net.rsprox.shared.filters

import net.rsprox.shared.StreamDirection

public interface PropertyFilterSet {
    public fun getCreationTime(): Long

    public fun getName(): String

    public fun setName(name: String)

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
}
