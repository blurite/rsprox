package net.rsprox.transcriber.rs3.text

import net.rsprox.shared.property.Property
import net.rsprox.shared.property.any
import net.rsprox.shared.property.int
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.rs3.state.Rs3SessionState

internal class Rs3TelemetryProperties(
    private val state: Rs3SessionState,
    private val settings: SettingSetStore,
) {
    fun groupId(property: Property, name: String, id: Int) {
        label(property, name, id, groupNames[id])
    }

    fun rowId(property: Property, name: String, group: Int?, id: Int) {
        val label = when (group) {
            1, 4 -> skills.getOrNull(id - 1)?.prettyName ?: when (id) {
                0 -> "all_skills_total"
                30 -> "all_combat_skills_total"
                31 -> "wealth"
                32 -> "combat"
                else -> null
            }
            2 -> if (id == 1) "combat" else null
            3 -> if (id == 1) "wealth" else null
            else -> null
        }
        label(property, name, id, label)
    }

    fun columnId(property: Property, name: String, id: Int) {
        label(property, name, id, columnNames[id])
    }

    fun group(property: Property, index: Int) {
        property.int("group", index)
        state.telemetry.groupId(index)?.let { groupId(property, "groupid", it) }
    }

    fun row(property: Property, group: Int, index: Int, name: String = "row") {
        property.int(name, index)
        state.telemetry.rowId(group, index)?.let { rowId(property, "rowid", state.telemetry.groupId(group), it) }
    }

    fun column(property: Property, group: Int, index: Int, name: String = "column") {
        property.int(name, index)
        state.telemetry.columnId(group, index)?.let { columnId(property, "columnid", it) }
    }

    fun value(property: Property, value: Int?) {
        if (value == null || value == Int.MIN_VALUE) property.any<Any>("value", null) else property.int("value", value)
    }

    private fun label(property: Property, name: String, id: Int, label: String?) {
        val settings = settings.getActive()
        if (label == null || settings[Setting.DISABLE_DICTIONARY_NAMES]) {
            property.int(name, id)
        } else {
            val text = if (settings[Setting.SHOW_IDS_AFTER_SYMBOLS]) "$label ($id)" else label
            property.any(name, text)
        }
    }

    private companion object {
        // Note: Check enums 681, 9734 and 10039 when updating RS3. Labels from the 2026-11-08 cache dump.
        val groupNames = mapOf(1 to "skills", 2 to "combat", 3 to "wealth", 4 to "skill_graph")
        val columnNames = mapOf(
            1 to "xp",
            2 to "xp_per_hour",
            4 to "eta",
            11 to "max_hit",
            13 to "npc_kills",
            15 to "damage",
            22 to "damage",
            17 to "damage_per_minute",
            18 to "potential_damage_per_minute",
            19 to "accuracy",
            21 to "deaths",
            31 to "gain",
            36 to "gain",
            33 to "drops",
            35 to "gp_per_hour",
        )
        val skills = listOf(
            Rs3Stat.ATTACK, Rs3Stat.STRENGTH, Rs3Stat.RANGED, Rs3Stat.MAGIC, Rs3Stat.DEFENCE,
            Rs3Stat.CONSTITUTION, Rs3Stat.PRAYER, Rs3Stat.AGILITY, Rs3Stat.HERBLORE, Rs3Stat.THIEVING,
            Rs3Stat.CRAFTING, Rs3Stat.RUNECRAFTING, Rs3Stat.MINING, Rs3Stat.SMITHING, Rs3Stat.FISHING,
            Rs3Stat.COOKING, Rs3Stat.FIREMAKING, Rs3Stat.WOODCUTTING, Rs3Stat.FLETCHING, Rs3Stat.SLAYER,
            Rs3Stat.FARMING, Rs3Stat.CONSTRUCTION, Rs3Stat.HUNTER, Rs3Stat.SUMMONING, Rs3Stat.DUNGEONEERING,
            Rs3Stat.DIVINATION, Rs3Stat.INVENTION, Rs3Stat.ARCHAEOLOGY, Rs3Stat.NECROMANCY,
        )
    }
}
