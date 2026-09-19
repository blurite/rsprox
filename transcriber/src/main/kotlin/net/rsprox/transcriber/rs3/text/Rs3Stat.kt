package net.rsprox.transcriber.rs3.text

import net.rsprox.shared.property.NamedEnum

/** The wire skill ID is the ordinal; RS3's skills differ from OSRS. */
internal enum class Rs3Stat : NamedEnum {
    ATTACK,
    DEFENCE,
    STRENGTH,
    CONSTITUTION,
    RANGED,
    PRAYER,
    MAGIC,
    COOKING,
    WOODCUTTING,
    FLETCHING,
    FISHING,
    FIREMAKING,
    CRAFTING,
    SMITHING,
    MINING,
    HERBLORE,
    AGILITY,
    THIEVING,
    SLAYER,
    FARMING,
    RUNECRAFTING,
    HUNTER,
    CONSTRUCTION,
    SUMMONING,
    DUNGEONEERING,
    DIVINATION,
    INVENTION,
    ARCHAEOLOGY,
    NECROMANCY,
    ;

    override val prettyName: String
        get() = name.lowercase()
}
