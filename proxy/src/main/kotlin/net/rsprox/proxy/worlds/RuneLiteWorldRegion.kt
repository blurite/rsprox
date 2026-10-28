package net.rsprox.proxy.worlds

public enum class RuneLiteWorldRegion(
    public val id: Int,
) {
    UNITED_STATES_OF_AMERICA(0),
    UNITED_KINGDOM(1),
    AUSTRALIA(3),
    GERMANY(7),
    BRAZIL(8),
    JAPAN(9),
    SINGAPORE(10),
    SOUTH_AFRICA(12),
    ;

    public companion object {
        public fun fromLocationId(id: Int): RuneLiteWorldRegion {
            return requireNotNull(entries.firstOrNull { it.id == id }) {
                "Unknown RuneLite world location: $id"
            }
        }
    }
}
