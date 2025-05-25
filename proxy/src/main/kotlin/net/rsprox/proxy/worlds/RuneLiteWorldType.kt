package net.rsprox.proxy.worlds

import java.util.EnumSet

public enum class RuneLiteWorldType(
    public val mask: Int,
) {
    /**
     * Members world type.
     */
    MEMBERS(1),

    /**
     * Pvp world type.
     */
    PVP(1 shl 2),

    /**
     * Bounty world type.
     */
    BOUNTY(1 shl 5),

    /**
     * PVP arena world type.
     */
    PVP_ARENA(1 shl 6),

    /**
     * Skill total world type.
     */
    SKILL_TOTAL(1 shl 7),

    /**
     * Quest speedrunning
     */
    QUEST_SPEEDRUNNING(1 shl 8),

    /**
     * High risk world type.
     */
    HIGH_RISK(1 shl 10),

    /**
     * Last man standing world type.
     */
    LAST_MAN_STANDING(1 shl 14),

    /**
     * Beta world.
     */
    BETA_WORLD(1 shl 16),
    LEGACY_ONLY(1 shl 22),
    EOC_ONLY(1 shl 23),

    /**
     * Beta worlds without profiles that are saved.
     */
    NOSAVE_MODE(1 shl 25),

    /**
     * Tournament world type
     */
    TOURNAMENT(1 shl 26),

    /**
     * Fresh start world type
     */
    FRESH_START_WORLD(1 shl 27),

    /**
     * Deadman world type.
     */
    DEADMAN(1 shl 29),

    /**
     * Seasonal world type for leagues and seasonal deadman.
     */
    SEASONAL(1 shl 30),
    ;

    public companion object {
        public fun fromMask(mask: Int): EnumSet<RuneLiteWorldType> {
            val set = EnumSet.noneOf(RuneLiteWorldType::class.java)
            for (type in RuneLiteWorldType.entries) {
                if (type.mask and mask != 0) {
                    set += type
                }
            }
            return set
        }
    }
}
