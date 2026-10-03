package net.rsprox.mcp.packets

import net.rsprox.shared.StreamDirection
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.filters.ProtCategory
import net.rsprox.shared.filters.RegexFilter
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingCategory
import net.rsprox.shared.settings.SettingGroup
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore

/**
 * Every filter enabled, no regex filters, nothing persisted. The stores on disk belong to the GUI,
 * and every mutator of the default filter set saves to them.
 */
internal object UnfilteredFilterSetStore : PropertyFilterSetStore {
    override val size: Int = 1

    override fun create(name: String): PropertyFilterSet = AllEnabled

    override fun delete(index: Int): PropertyFilterSet? = null

    override fun get(index: Int): PropertyFilterSet? = if (index == 0) AllEnabled else null

    override fun getActive(): PropertyFilterSet = AllEnabled

    override fun setActive(index: Int) {
        //
    }

    private object AllEnabled : PropertyFilterSet {
        override fun getCreationTime(): Long = 0

        override fun getName(): String = "unfiltered"

        override fun setName(name: String) {
            //
        }

        override fun deleteBackingFile() {
            //
        }

        override fun get(filter: PropertyFilter): Boolean = true

        override fun set(
            filter: PropertyFilter,
            enabled: Boolean,
        ) {
            //
        }

        override fun set(
            category: ProtCategory,
            enabled: Boolean,
        ) {
            //
        }

        override fun set(
            streamDirection: StreamDirection,
            enabled: Boolean,
        ) {
            //
        }

        override fun setAll(enabled: Boolean) {
            //
        }

        override fun setDefaults() {
            //
        }

        override fun getRegexFilters(): List<RegexFilter> = emptyList()

        override fun addRegexFilter(regexFilter: RegexFilter) {
            //
        }

        override fun removeRegexFilter(regexFilter: RegexFilter) {
            //
        }

        override fun replaceRegexFilter(
            oldRegexFilter: RegexFilter,
            newRegexFilter: RegexFilter,
        ) {
            //
        }

        override fun clearRegexFilters() {
            //
        }
    }
}

/**
 * The default of every setting, except that each setting the transcriber reads to drop output is off.
 * Fixed in memory so the transcript does not depend on what the user ticked in the GUI.
 *
 * Settings that drop output, and where the transcriber reads them:
 * - `SKIP_FIRST_TICK`: every packet of tick 0 (`TextTranscriber`, `TextRs3Transcriber`).
 * - `HIDE_UNNECESSARY_VARPS`, `HIDE_SAME_VALUE_VARPS`: varp packets (`TextServerPacketTranscriber`).
 * - `PLAYER_INFO_HIDE_EMPTY`: an empty player info packet (`TextPlayerInfoTranscriber`).
 * - `PLAYER_INFO_LOCAL_PLAYER_ONLY`: updates of every other player (`TextPlayerInfoTranscriber`).
 * - `NPC_INFO_HIDE_EMPTY`: an empty NPC info packet (`TextNpcInfoTranscriber`).
 * - `WORLDENTITY_INFO_HIDE_EMPTY`: an empty world entity info packet (`TextServerPacketTranscriber`).
 * - `HIDE_RS3_LOBBY`: RuneScape 3 lobby packets (`TextRs3Transcriber`).
 */
internal object TapSettingSetStore : SettingSetStore {
    private val off =
        setOf(
            Setting.SKIP_FIRST_TICK,
            Setting.HIDE_UNNECESSARY_VARPS,
            Setting.HIDE_SAME_VALUE_VARPS,
            Setting.PLAYER_INFO_HIDE_EMPTY,
            Setting.PLAYER_INFO_LOCAL_PLAYER_ONLY,
            Setting.NPC_INFO_HIDE_EMPTY,
            Setting.WORLDENTITY_INFO_HIDE_EMPTY,
            Setting.HIDE_RS3_LOBBY,
        )

    override val size: Int = 1

    override fun create(name: String): SettingSet = Fixed

    override fun delete(index: Int): SettingSet? = null

    override fun get(index: Int): SettingSet? = if (index == 0) Fixed else null

    override fun getActive(): SettingSet = Fixed

    override fun setActive(index: Int) {
        //
    }

    private object Fixed : SettingSet {
        override fun getCreationTime(): Long = 0

        override fun getName(): String = "mcp"

        override fun setName(name: String) {
            //
        }

        override fun deleteBackingFile() {
            //
        }

        override fun get(setting: Setting): Boolean = setting.enabled && setting !in off

        override fun set(
            setting: Setting,
            enabled: Boolean,
        ) {
            //
        }

        override fun set(
            category: SettingCategory,
            enabled: Boolean,
        ) {
            //
        }

        override fun set(
            group: SettingGroup,
            enabled: Boolean,
        ) {
            //
        }

        override fun setAll(enabled: Boolean) {
            //
        }

        override fun setDefaults() {
            //
        }
    }
}
