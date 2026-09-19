package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.any
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.filteredBoolean
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.namedEnum
import net.rsprox.shared.property.scriptVarType

internal fun Property.appendTint(
    hue: Int,
    saturation: Int,
    lightness: Int,
    weight: Int,
    start: Int,
    end: Int,
) {
    int("start", start)
    int("end", end)
    int("hue", hue)
    int("saturation", saturation)
    int("lightness", lightness)
    int("weight", weight)
}

internal fun Property.appendSequences(ids: List<Int>, delay: Int) {
    if (ids.size == 4 && ids.all { it == ids[0] }) {
        any("slot", "all")
        scriptVarType("id", ScriptVarType.SEQ, ids[0])
    } else {
        ids.forEachIndexed { slot, id ->
            group {
                // Native selects one of four alternatives using movement kind + 1.
                if (slot in 0..3) {
                    namedEnum("slot", Rs3MovementMode.entries[slot])
                } else {
                    int("slot", slot)
                }
                scriptVarType("id", ScriptVarType.SEQ, id)
            }
        }
    }
    filteredInt("delay", delay, 0)
}

internal fun Property.appendSpotanimRemoval(id: Int) {
    group("SPOTANIM") {
        // Native removal searches the attached effects by type ID, not by attachment slot.
        if (id == -1 || id == 65535) {
            boolean("all", true)
        } else {
            scriptVarType("id", ScriptVarType.SPOTANIM, id)
        }
        boolean("removed", true)
    }
}

internal fun Property.appendSpotanim(
    slot: Int,
    id: Int,
    packedHeightDelay: Int,
    rotationFlags: Int,
    packedOffsets: Int,
) {
    group("SPOTANIM") {
        int("slot", slot)
        scriptVarType("id", ScriptVarType.SPOTANIM, if (id == 65535) -1 else id)
        filteredInt("delay", packedHeightDelay and 0x7FFF, 0)
        // Signed wire height; the renderer multiplies it by four.
        filteredInt("height", packedHeightDelay shr 16, 0)
        // Rotation is in eighth turns. Bits 3..6 have no consumer in the native handler.
        filteredInt("rotation", rotationFlags and 7, 0)
        filteredBoolean("loop", rotationFlags and 0x80 != 0)
        filteredInt("offsetx", (packedOffsets and 0x7FF) - 1023, 0)
        filteredInt("offsetz", (packedOffsets ushr 11 and 0x7FF) - 1023, 0)
        // The client tests equality to 1, not just whether bit 22 is set.
        filteredBoolean("relativeoffset", packedOffsets ushr 22 == 1)
        filteredBoolean("independentrotation", packedHeightDelay and 0x8000 != 0)
    }
}

internal fun Property.appendHit(
    id: Int,
    value: Int,
    secondaryId: Int,
    secondaryValue: Int,
    delay: Int,
    wide: Boolean,
) {
    group(if (wide) "HIT_V2" else "HIT_V1") {
        scriptVarType("id", ScriptVarType.HITMARK, id)
        int("value", value)
        if (secondaryId != -1) {
            scriptVarType("soaktype", ScriptVarType.HITMARK, secondaryId)
            int("soakvalue", secondaryValue)
        }
        filteredInt("delay", delay, 0)
    }
}

internal fun Property.appendHeadbarFill(startFill: Int, endFill: Int, delay: Int, duration: Int) {
    if (startFill == endFill && delay == 0 && duration == 0) {
        int("fill", startFill)
    } else {
        int("startfill", startFill)
        int("endfill", endFill)
    }
    // RS3 supplies a delay/duration, not the absolute times used by newer OSRS masks.
    filteredInt("delay", delay, 0)
    filteredInt("duration", duration, 0)
}
