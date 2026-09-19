package net.rsprox.transcriber.rs3.text

import net.rsprox.protocol.rs3.game.outgoing.model.appearance.AppearanceBody
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo.Equipment
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.filteredBoolean
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.filteredScriptVarType
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.string

internal fun Property.appendAppearance(
    info: PlayerExtendedInfo.Appearance,
    filters: PropertyFilterSet,
) {
    group("APPEARANCE") {
        if (filters[PropertyFilter.PLAYER_APPEARANCE_DETAILS]) {
            group("DETAILS") {
                string("name", info.name)
                int("combatlevel", info.combatLevel)
                info.skillLevel?.let { filteredInt("skilllevel", it, 0) }
                info.effectiveCombatLevel?.let { filteredInt("effectivecombatlevel", it, info.combatLevel) }
                info.combatColourParameter?.let { filteredInt("combatcolourparameter", it, -1) }
                int("gender", info.gender)
            }
        }
        if (filters[PropertyFilter.PLAYER_APPEARANCE_STATUS]) {
            group("STATUS") {
                // Do not reduce this to hidden: native consumers distinguish == 1 from != 0.
                filteredInt("visibility", info.visibility, 0)
                filteredInt("size", info.size, 1)
                appendAppearanceTransform(info.npc, info.npcTeam)
            }
            group("BACKGROUND_SOUND") {
                val sound = info.backgroundSound
                filteredInt("range", sound?.range ?: 0, 0)
                if (sound != null) {
                    filteredScriptVarType("stationary", ScriptVarType.SYNTH, sound.stationary, -1)
                    filteredScriptVarType("crawl", ScriptVarType.SYNTH, sound.crawl, -1)
                    filteredScriptVarType("walk", ScriptVarType.SYNTH, sound.walk, -1)
                    filteredScriptVarType("run", ScriptVarType.SYNTH, sound.run, -1)
                    int("volume", sound.volume)
                }
            }
        }
        appendAppearanceBodyDetails(
            AppearanceBody(
                info.npc,
                info.npcTeam,
                info.equipment,
                info.customisations,
                info.primaryColours,
                info.secondaryColours,
                info.renderAnimationSet,
            ),
            filters,
        )
        if (filters[PropertyFilter.PLAYER_APPEARANCE_NAME_EXTRAS]) {
            group("NAME_EXTRAS") {
                filteredInt("title", info.title ?: -1, -1)
                // This selects a title enum, not whether the title is a prefix or suffix.
                filteredInt("titlevariant", info.titleVariant, 0)
                if (info.icons.isNotEmpty()) {
                    group("ICONS") {
                        for (icon in info.icons) {
                            group {
                                scriptVarType("sprite", ScriptVarType.GRAPHIC, icon.sprite)
                                filteredBoolean("menu", icon.menu)
                                filteredBoolean("chatbox", icon.chatbox)
                                filteredInt("unknownflags", icon.flags and 0xFC, 0)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Property.appendAppearanceTransform(
    npc: Int?,
    team: Int?,
) {
    npc?.let { filteredScriptVarType("npc", ScriptVarType.NPC, it, -1) }
    team?.let { filteredInt("team", it, 0) }
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

/** Lobby/snapshot bodies have no player-profile prefix, levels or sound tail. */
internal fun Property.appendAppearanceBody(info: AppearanceBody) {
    if (info.npc != null || info.npcTeam != null) {
        group("STATUS") { appendAppearanceTransform(info.npc, info.npcTeam) }
    }
    appendAppearanceBodyDetails(info, filters = null)
}

private fun Property.appendAppearanceBodyDetails(
    info: AppearanceBody,
    filters: PropertyFilterSet?,
) {
    fun enabled(filter: PropertyFilter): Boolean = filters?.get(filter) ?: true

    if (enabled(PropertyFilter.PLAYER_APPEARANCE_EQUIPMENT) && info.equipment.any { it.kind == Equipment.Kind.ITEM }) {
        group("EQUIPMENT") {
            for (entry in info.equipment) {
                if (entry.kind == Equipment.Kind.ITEM) {
                    group {
                        // Physical cache slot: RS3's slots must not be assigned OSRS's enum blindly.
                        int("slot", entry.slot)
                        scriptVarType("id", ScriptVarType.OBJ, entry.id)
                    }
                }
            }
        }
    }
    if (enabled(PropertyFilter.PLAYER_APPEARANCE_IDENTKIT) && info.equipment.any { it.kind == Equipment.Kind.KIT }) {
        group("IDENTKIT") {
            for (entry in info.equipment) {
                if (entry.kind == Equipment.Kind.KIT) {
                    group {
                        int("slot", entry.slot)
                        scriptVarType("id", ScriptVarType.IDKIT, entry.id)
                    }
                }
            }
        }
    }
    if (enabled(PropertyFilter.PLAYER_APPEARANCE_COLOURS)) {
        group("COLOURS") {
            for (index in info.primaryColours.indices) {
                group {
                    int("slot", index)
                    int("primary", info.primaryColours[index])
                    int("secondary", info.secondaryColours[index])
                }
            }
        }
    }
    if (enabled(PropertyFilter.PLAYER_APPEARANCE_BAS)) {
        group("BAS") { scriptVarType("id", ScriptVarType.BAS, info.renderAnimationSet) }
    }
    if (enabled(PropertyFilter.PLAYER_APPEARANCE_OBJ_TYPE_CUSTOMIZATION) && info.customisations.isNotEmpty()) {
        group("CUSTOMISATIONS") {
            for (entry in info.customisations) {
                group {
                    int("slot", entry.slot)
                    filteredInt("flags", entry.flags, 0)
                    modelPairs("BODY_MODELS", entry.bodyModels)
                    modelPairs("HEAD_MODELS", entry.headModels)
                    palette("RECOLOURS", entry.recolours)
                    palette("RETEXTURES", entry.retextures)
                }
            }
        }
    }
}
