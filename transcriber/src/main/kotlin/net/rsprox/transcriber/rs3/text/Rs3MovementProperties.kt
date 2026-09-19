package net.rsprox.transcriber.rs3.text

import net.rsprox.shared.property.NamedEnum
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.int
import net.rsprox.shared.property.namedEnum

/** Wire/table indices, not the native enum object's stored values (-1, 0, 1, 2, 3). */
internal enum class Rs3MovementMode(override val prettyName: String) : NamedEnum {
    STATIONARY("stationary"),
    CRAWL("crawl"),
    WALK("walk"),
    RUN("run"),
    TELEPORT("teleport"),
    ;

    companion object {
        fun fromId(id: Int): Rs3MovementMode? = entries.getOrNull(id)
    }
}

internal fun Property.appendMovementMode(id: Int) {
    val mode = Rs3MovementMode.fromId(id)
    if (mode == null) {
        int("unknownmovementmode", id)
    } else {
        namedEnum("speed", mode)
    }
}

/** Native NPC stepping is clockwise from north, unlike the OSRS step-code table. */
internal enum class Rs3NpcStep(override val prettyName: String) : NamedEnum {
    NORTH("north"),
    NORTH_EAST("north-east"),
    EAST("east"),
    SOUTH_EAST("south-east"),
    SOUTH("south"),
    SOUTH_WEST("south-west"),
    WEST("west"),
    NORTH_WEST("north-west"),
}
