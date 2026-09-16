package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcMask
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.any
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.string

internal fun Property.appendNpcMask(
    mask: NpcMask,
    entities: Rs3EntityProperties,
    coordinates: Rs3CoordinateProperties,
    level: Int,
) {
    // Like OSRS, emit each hit, headbar and spotanim as its own extended-info entry.
    if (mask is NpcMask.Hits) {
        appendNpcHits(mask)
        return
    }
    if (mask is NpcMask.Spotanims) {
        mask.removals.forEach { appendSpotanimRemoval(it) }
        mask.additions.forEach { spot ->
            appendSpotanim(spot.slot, spot.id, spot.packedHeightDelay, spot.rotationFlags, spot.packedOffsets)
        }
        return
    }
    val name = if (mask.key == Rs3NpcUpdateMaskKey.COMBAT_LEVEL_CHANGE) "LEVEL_CHANGE" else mask.key.name
    group(name) {
        when (mask) {
            is NpcMask.Text ->
                string(if (mask.key == Rs3NpcUpdateMaskKey.NAME_CHANGE) "name" else "text", mask.text)
            is NpcMask.Transformation -> scriptVarType("id", ScriptVarType.NPC, mask.id)
            is NpcMask.FaceEntity -> entities.face(this, mask.target)
            is NpcMask.Sequence -> appendSequences(mask.ids, mask.delay)
            is NpcMask.Scalars -> {
                when (mask.key) {
                    Rs3NpcUpdateMaskKey.TINTING -> {
                        val values = mask.values
                        appendTint(values[0], values[1], values[2], values[3], values[4], values[5])
                    }
                    Rs3NpcUpdateMaskKey.COMBAT_LEVEL_CHANGE -> int("level", mask.values.single())
                    Rs3NpcUpdateMaskKey.FACE_TILE -> {
                        val (x2, z2) = mask.values
                        if (x2 and 1 == 1 && z2 and 1 == 1) {
                            coordinates.append(this, level, x2 shr 1, z2 shr 1)
                        } else {
                            int("2x", x2)
                            int("2z", z2)
                        }
                    }
                    else -> mask.values.forEachIndexed { index, value -> int("field$index", value) }
                }
            }
            is NpcMask.Variables -> {
                int("prefix", mask.discardedPrefix)
                appendVariables(mask.variables)
            }
            is NpcMask.Stats ->
                mask.stats.forEach { stat ->
                    group {
                        scriptVarType("stat", ScriptVarType.NPC_STAT, stat.slot)
                        int("value", stat.value)
                        int("auxiliary", stat.auxiliary)
                    }
                }
            is NpcMask.SlotPairs ->
                mask.slots.forEach { slot ->
                    group {
                        int("slot", slot.slot)
                        int("id", slot.id)
                        int("index", slot.index)
                    }
                }
            is NpcMask.Customisation -> {
                int("flags", mask.flags)
                group("models") {
                    mask.models.forEach { model ->
                        group {
                            scriptVarType("id", ScriptVarType.MODEL, model.id)
                            model.scale?.let { any("scale", it) }
                            appendInts("translation", model.translation)
                            appendInts("rotation", model.rotation)
                            appendInts("values32", model.values32)
                            appendInts("values64", model.values64)
                        }
                    }
                }
                appendInts("recolours", mask.recolours)
                appendInts("retextures", mask.retextures)
                appendInts("colours", mask.colours)
            }
            is NpcMask.Spotanims, is NpcMask.Hits -> error("Handled before grouping")
            is NpcMask.BoneTransforms -> {
                int("count", mask.count)
                mask.transforms.forEach { transform ->
                    group {
                        int("slot", transform.slot)
                        int("flags", transform.flags)
                        transform.id?.let { int("id", it) }
                        appendAxes("translation", transform.translation)
                        appendAxes("rotation", transform.rotation)
                        appendAxes("scale", transform.scale)
                    }
                }
            }
        }
    }
}

private fun Property.appendNpcHits(mask: NpcMask.Hits) {
    mask.hits.forEach { hit ->
        appendHit(hit.id, hit.value, hit.secondaryId, hit.secondaryValue, hit.delay)
    }
    mask.headbars.forEach { bar ->
        group("HEADBAR") {
            scriptVarType("id", ScriptVarType.HEADBAR, bar.id)
            if (bar.duration == 32767) {
                boolean("removed", true)
            } else {
                appendHeadbarFill(
                    requireNotNull(bar.first),
                    requireNotNull(bar.second),
                    requireNotNull(bar.delay),
                    bar.duration,
                )
                bar.extraId?.let { id ->
                    group("extra") {
                        int("id", id)
                        int("startfill", requireNotNull(bar.extraFirst))
                        int("endfill", requireNotNull(bar.extraSecond))
                    }
                }
            }
        }
    }
}

private fun Property.appendInts(
    name: String,
    values: List<Int>,
) {
    if (values.isEmpty()) return
    group(name) { values.forEachIndexed { index, value -> int("slot$index", value) } }
}

private fun Property.appendAxes(
    name: String,
    values: List<Int?>,
) {
    if (values.all { it == null }) return
    group(name) {
        values.forEachIndexed { index, value -> value?.let { int(listOf("x", "y", "z")[index], it) } }
    }
}
