package net.rsprox.transcriber.rs3.text

import net.rsprox.protocol.common.CoordGrid
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
import net.rsprox.shared.property.varnpc

internal fun Property.appendNpcMask(
    mask: NpcMask,
    entities: Rs3EntityProperties,
    coordinates: Rs3CoordinateProperties,
    baseCoord: CoordGrid,
    subtractFirstDelay: Boolean,
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
    if (mask is NpcMask.HeadIconCustomisation) {
        mask.slots.forEach { icon ->
            group("HEADICON") {
                int("headiconslot", icon.slot)
                scriptVarType("id", ScriptVarType.GRAPHIC, icon.group)
                int("spriteindex", icon.spriteIndex)
            }
        }
        return
    }
    val name = if (mask.key == Rs3NpcUpdateMaskKey.COMBAT_LEVEL_CHANGE) "LEVEL_CHANGE" else mask.key.name
    group(name) {
        when (mask) {
            is NpcMask.Text ->
                string(if (mask.key == Rs3NpcUpdateMaskKey.NAME_CHANGE) "name" else "text", mask.text)
            is NpcMask.Transformation -> scriptVarType("id", ScriptVarType.NPC, mask.id)
            is NpcMask.PriorityOffset -> {
                val offset = mask.offset
                if (offset == null) any<Int>("offset", null) else int("offset", offset)
            }
            is NpcMask.FaceEntity -> entities.entity(this, mask.target)
            is NpcMask.ExactMove -> {
                // Resolve both endpoints in instance space before translating either endpoint.
                // Keep the raw angle: native normalizes it to 14 bits when applying the movement.
                coordinates.append(
                    this,
                    baseCoord.level + mask.deltaLevel1,
                    baseCoord.x + mask.deltaX1,
                    baseCoord.z + mask.deltaZ1,
                    "to1",
                )
                int(if (subtractFirstDelay) "delay" else "delay1", mask.delay1)
                coordinates.append(
                    this,
                    baseCoord.level + mask.deltaLevel2,
                    baseCoord.x + mask.deltaX2,
                    baseCoord.z + mask.deltaZ2,
                    "to2",
                )
                if (subtractFirstDelay) {
                    int("duration", mask.delay2 - mask.delay1)
                } else {
                    int("delay2", mask.delay2)
                }
                int("angle", mask.angle)
            }
            is NpcMask.BasOverride -> {
                val bas = mask.bas
                if (bas == null) any<Int>("bas", null) else scriptVarType("bas", ScriptVarType.BAS, bas)
            }
            is NpcMask.Sequence -> appendSequences(mask.ids, mask.delay)
            is NpcMask.OverlapCulling -> boolean("disabled", mask.disabled)
            is NpcMask.Scalars -> {
                when (mask.key) {
                    Rs3NpcUpdateMaskKey.TINTING -> {
                        val values = mask.values
                        appendTint(values[0], values[1], values[2], values[3], values[4], values[5])
                    }
                    Rs3NpcUpdateMaskKey.COMBAT_LEVEL_CHANGE -> int("level", mask.values.single())
                    Rs3NpcUpdateMaskKey.DISABLED_OPS -> {
                        // Set bits hide options; preserve all eight wire bits, including unknown upper bits.
                        any(
                            "opflags",
                            "0b" +
                                mask.values
                                    .single()
                                    .toString(2)
                                    .padStart(8, '0'),
                        )
                    }
                    Rs3NpcUpdateMaskKey.FACE_TILE -> {
                        val (x2, z2) = mask.values
                        if (x2 and 1 == 1 && z2 and 1 == 1) {
                            coordinates.append(this, baseCoord.level, x2 shr 1, z2 shr 1)
                        } else {
                            int("2x", x2)
                            int("2z", z2)
                        }
                    }
                    else -> mask.values.forEachIndexed { index, value -> int("field$index", value) }
                }
            }
            is NpcMask.Variables -> {
                mask.variables.forEach { variable ->
                    group {
                        varnpc("varnpc", variable.id)
                        appendVariableValue(variable.value)
                    }
                }
            }
            is NpcMask.Stats ->
                mask.stats.forEach { stat ->
                    group {
                        scriptVarType("stat", ScriptVarType.NPC_STAT, stat.slot)
                        int("current", stat.currentLevel)
                        int("base", stat.baseLevel)
                    }
                }
            is NpcMask.Customisation -> appendCustomisation(mask)
            is NpcMask.Spotanims, is NpcMask.Hits, is NpcMask.HeadIconCustomisation -> error("Handled before grouping")
            is NpcMask.Attachments -> {
                int("count", mask.count)
                if (mask.count == 0) boolean("reset", true)
                mask.attachments.forEach { attachment ->
                    group {
                        int("slot", attachment.slot)
                        when {
                            attachment.flags and 0x400 != 0 ->
                                scriptVarType("obj", ScriptVarType.OBJ, requireNotNull(attachment.id))
                            attachment.flags and 0x800 != 0 -> {
                                val id = requireNotNull(attachment.id)
                                // Native narrows only VFX IDs to 16 bits. Preserve unusual wire values too.
                                int("vfx", id and 0xFFFF)
                                if (id != (id and 0xFFFF)) int("rawid", id)
                            }
                            // No resource setter is invoked; listed slots are still retained.
                            else -> boolean("retain", true)
                        }
                        int("flags", attachment.flags)
                        boolean("rotateoffset", attachment.flags and 0x40 != 0)
                        appendAxes("translation", attachment.translation)
                        appendAxes("rotation", attachment.rotation)
                        appendAxes("scale", attachment.scale)
                    }
                }
            }
        }
    }
}

private fun Property.appendCustomisation(mask: NpcMask.Customisation) {
    // Keep flags to distinguish omitted/empty sections and preserve ignored upper bits.
    int("flags", mask.flags)
    if (mask.flags and 1 != 0) {
        boolean("reset", true)
        return
    }
    // This replaces the entire override; absent sections use the NPC definition again.
    mask.models.forEach { model ->
        group("MODEL") {
            scriptVarType("id", ScriptVarType.MODEL, model.id)
            model.scale?.let { any("scale", it) }
            appendAxes("rotation", model.rotation)
            appendAxes("translation", model.translation)
            appendModelReplacements("RECOLOUR", model.recolours, ScriptVarType.INT)
            appendModelReplacements("RETEXTURE", model.retextures, ScriptVarType.TEXTURE)
        }
    }
    require(mask.recolours.size == mask.recolourSlots.size)
    require(mask.retextures.size == mask.retextureSlots.size)
    if (mask.recolours.isNotEmpty()) {
        group("RECOLOUR") {
            mask.recolours.forEachIndexed { index, colour ->
                int("recol${mask.recolourSlots[index] + 1}d", colour)
            }
        }
    }
    if (mask.retextures.isNotEmpty()) {
        group("RETEXTURE") {
            mask.retextures.forEachIndexed { index, texture ->
                scriptVarType("retex${mask.retextureSlots[index] + 1}d", ScriptVarType.TEXTURE, texture)
            }
        }
    }
    if (mask.paletteIndices.isNotEmpty()) {
        group("PALETTE") {
            mask.paletteIndices.forEachIndexed { slot, index ->
                group {
                    int("slot", slot)
                    int("index", index)
                }
            }
        }
    }
}

private fun Property.appendModelReplacements(
    name: String,
    values: List<Int>,
    type: ScriptVarType,
) {
    values.chunked(2).forEach { pair ->
        group(name) {
            // The native renderer uses 16-bit values, but skips pairs containing signed -1.
            pair.forEachIndexed { index, value ->
                val field = if (index == 0) "src" else "dst"
                if (value == -1) any<Int>(field, null) else scriptVarType(field, type, value and 0xFFFF)
            }
            if (-1 in pair) boolean("disabled", true)
            // Keep malformed odd lists visible; never silently discard the final source value.
            if (pair.size != 2) boolean("incomplete", true)
        }
    }
}

private fun Property.appendNpcHits(mask: NpcMask.Hits) {
    mask.hits.forEach { hit ->
        appendHit(hit.id, hit.value, hit.secondaryId, hit.secondaryValue, hit.delay, mask.wide)
    }
    mask.headbars.forEach { bar ->
        group(if (mask.wide) "HEADBAR_V2" else "HEADBAR_V1") {
            scriptVarType("id", ScriptVarType.HEADBAR, bar.id)
            if (bar.duration == 32767) {
                boolean("removed", true)
            } else {
                appendHeadbarFill(
                    requireNotNull(bar.startFill),
                    requireNotNull(bar.endFill),
                    requireNotNull(bar.delay),
                    bar.duration,
                )
                bar.secondaryId?.let { id ->
                    group("secondary") {
                        scriptVarType("id", ScriptVarType.HEADBAR, id)
                        int("startfill", requireNotNull(bar.secondaryStartFill))
                        int("endfill", requireNotNull(bar.secondaryEndFill))
                    }
                }
            }
        }
    }
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
