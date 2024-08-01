package net.rsprox.shared.filters

public interface PropertyFilterSetStore {
    public fun create(index: Int): PropertyFilterSet

    public fun delete(index: Int): PropertyFilterSet?

    public fun get(index: Int): PropertyFilterSet?

    public fun getOrCreate(index: Int): PropertyFilterSet {
        return get(index) ?: create(index)
    }

    public fun getActive(): PropertyFilterSet

    public fun setActive(index: Int)
}
