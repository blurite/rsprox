package net.rsprox.transcriber.rs3.text

import net.rsprox.shared.property.NamedEnum
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.int
import net.rsprox.shared.property.namedEnum

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

internal fun Property.skill(name: String, id: Int) {
    val skill = Rs3Stat.entries.getOrNull(id)
    if (skill != null) namedEnum(name, skill) else int(name, id)
}
