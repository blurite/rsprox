package net.rsprox.proxy.worlds

/** Disjoint last-octet ranges; the middle octets remain the unsigned world/lobby ID. */
public object LocalAddressRanges {
    public const val OSRS_BASE: Int = 2
    public const val RS3_WORLD_BASE: Int = 25
    public const val RS3_LOBBY_BASE: Int = 50
    public const val RS3_TARGET_COUNT: Int = 25
    public const val REPLAY_TARGET_ID: Int = 98
    public const val REPLAY_SUFFIX: Int = 100

    public fun osrsSuffix(targetId: Int): Int {
        if (targetId == REPLAY_TARGET_ID) return REPLAY_SUFFIX
        require(targetId in 0 until RS3_WORLD_BASE - OSRS_BASE) {
            "OSRS target $targetId exceeds the reserved .2-.24 suffix range"
        }
        return OSRS_BASE + targetId
    }
}
