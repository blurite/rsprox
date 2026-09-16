package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.rs3.game.outgoing.model.appearance.AppearanceBody
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo.Equipment
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.string

internal fun Property.appendAppearance(info: PlayerExtendedInfo.Appearance) {
    group("APPEARANCE") {
        string("name", info.name)
        int("bodyType", info.bodyType)
        int("size", info.size)
        int("flags", info.flags)
        info.title?.let { int("title", it) }
        if (info.icons.isNotEmpty()) {
            group("icons") {
                for (icon in info.icons) {
                    group {
                        int("id", icon.id)
                        int("value", icon.value)
                    }
                }
            }
        }
        appendAppearanceBody(
            AppearanceBody(
                info.npc,
                info.npcTeam,
                info.equipment,
                info.customisations,
                info.primaryColours,
                info.secondaryColours,
                info.renderAnimationSet,
            ),
        )
        int("combatLevel", info.combatLevel)
        info.totalLevel?.let { int("totalLevel", it) }
        info.visibleCombatLevel?.let { int("visibleCombatLevel", it) }
        info.combatDifference?.let { int("combatDifference", it) }
        int("extraFlag", info.extraFlag)
        if (info.extra.isNotEmpty()) {
            group("extra") {
                for ((index, value) in info.extra.withIndex()) int("value$index", value)
            }
        }
    }
}

private fun Property.modelPairs(
    name: String,
    models: List<PlayerExtendedInfo.ModelPair>,
) {
    if (models.isEmpty()) return
    group(name) {
        for (model in models) {
            group {
                int("slot", model.slot)
                scriptVarType("male", ScriptVarType.MODEL, model.male)
                scriptVarType("female", ScriptVarType.MODEL, model.female)
            }
        }
    }
}

private fun Property.palette(
    name: String,
    replacements: List<PlayerExtendedInfo.PaletteReplacement>,
) {
    if (replacements.isEmpty()) return
    group(name) {
        for (replacement in replacements) {
            group {
                int("index", replacement.index)
                int("value", replacement.value)
            }
        }
    }
}

internal fun Property.appendAppearanceBody(info: AppearanceBody) {
    info.npc?.let { scriptVarType("npc", ScriptVarType.NPC, it) }
    info.npcTeam?.let { int("team", it) }
    if (info.equipment.isNotEmpty()) {
        group("equipment") {
            for (entry in info.equipment) {
                group {
                    int("slot", entry.slot)
                    when (entry.kind) {
                        Equipment.Kind.ITEM -> scriptVarType("obj", ScriptVarType.OBJ, entry.id)
                        Equipment.Kind.KIT -> int("kit", entry.id)
                        Equipment.Kind.EMPTY -> int("empty", entry.id)
                    }
                }
            }
        }
    }
    if (info.customisations.isNotEmpty()) {
        group("customisations") {
            for (entry in info.customisations) {
                group {
                    int("slot", entry.slot)
                    int("flags", entry.flags)
                    modelPairs("bodyModels", entry.bodyModels)
                    modelPairs("headModels", entry.headModels)
                    palette("recolours", entry.recolours)
                    palette("retextures", entry.retextures)
                }
            }
        }
    }
    group("colours") {
        for (index in info.primaryColours.indices) {
            group {
                int("slot", index)
                int("primary", info.primaryColours[index])
                int("secondary", info.secondaryColours[index])
            }
        }
    }
    scriptVarType("bas", ScriptVarType.BAS, info.renderAnimationSet)
}
