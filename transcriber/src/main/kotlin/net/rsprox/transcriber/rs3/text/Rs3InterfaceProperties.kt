package net.rsprox.transcriber.rs3.text

import net.rsprox.shared.property.NamedEnum
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.int
import net.rsprox.shared.property.namedEnum

internal enum class Rs3IfType(
    val id: Int,
    override val prettyName: String,
) : NamedEnum {
    MODAL(0, "modal"),
    OVERLAY(1, "overlay"),
    CLIENT(3, "client"),
}

internal fun Property.ifType(id: Int) {
    val type = Rs3IfType.entries.firstOrNull { it.id == id }
    if (type == null) {
        int("type", id)
    } else {
        namedEnum("type", type)
    }
}

/** Revision-950 native consumers and naming evidence: RS3_950_INTERFACE_EVENTS.md. */
internal object Rs3InterfaceEvents {
    fun list(mask: Int): List<String> =
        buildList {
            if (mask and 1 != 0) add("PAUSEBUTTON")
            for (op in 1..10) {
                if (mask and (1 shl op) != 0) add("OP$op")
            }
            for ((index, target) in targets.withIndex()) {
                if (mask and (1 shl (11 + index)) != 0) add(target)
            }
            // This is one three-bit integer, not three independent flags.
            val depth = mask ushr 18 and 7
            if (depth != 0) add("DEPTH$depth")
            if (mask and (1 shl 21) != 0) add("DRAGTARGET")
            if (mask and (1 shl 22) != 0) add("TARGET")
            if (mask and (1 shl 23) != 0) add("DRAGROOT")
            if (mask and (1 shl 24) != 0) add("TRANSMITINPUT")
            // Preserve unproven bits, including the sign bit, without guessing their meaning.
            for (bit in 25..31) {
                if (mask and (1 shl bit) != 0) add("BIT$bit")
            }
        }

    private val targets = listOf("TGTOBJ", "TGTNPC", "TGTLOC", "TGTPLAYER", "TGTSELF", "TGTCOM", "TGTCOORD")
}
