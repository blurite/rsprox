package net.rsprox.cache.api.rs3

/** A session-pinned snapshot. Lookups must not perform network I/O. */
public interface Rs3AppearanceDefinitions {
    public val equipmentSlotKinds: List<Int>

    public fun getItem(id: Int): Rs3AppearanceItem
}

/** Only definition fields which determine appearance wire layout are retained. */
public data class Rs3AppearanceItem(
    public val maleBodyModels: List<Int>,
    public val femaleBodyModels: List<Int>,
    public val maleHeadModels: List<Int>,
    public val femaleHeadModels: List<Int>,
)
