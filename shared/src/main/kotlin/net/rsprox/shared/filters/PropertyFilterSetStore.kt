package net.rsprox.shared.filters

public interface PropertyFilterSetStore {

    public val size: Int

    public fun create(name: String): PropertyFilterSet

    public fun delete(index: Int): PropertyFilterSet?

    public fun get(index: Int): PropertyFilterSet?

    public fun getActive(): PropertyFilterSet

    public fun setActive(index: Int)
}
