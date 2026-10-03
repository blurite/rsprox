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
    /** The number of filter sets, which is always one. */
    override val size: Int = 1

    /** Get the one filter set, since this store never makes another. */
    override fun create(name: String): PropertyFilterSet = AllEnabled

    /** Keep the one filter set and report that nothing was deleted. */
    override fun delete(index: Int): PropertyFilterSet? = null

    /** Get the one filter set, which sits at index 0. */
    override fun get(index: Int): PropertyFilterSet? = if (index == 0) AllEnabled else null

    /** Get the one filter set, which is always the active one. */
    override fun getActive(): PropertyFilterSet = AllEnabled

    /** Ignore the index, since the one filter set is always the active one. */
    override fun setActive(index: Int) {
        //
    }

    private object AllEnabled : PropertyFilterSet {
        /** Get a creation time of zero, since the set was never saved. */
        override fun getCreationTime(): Long = 0

        /** Get the fixed name of the set. */
        override fun getName(): String = "unfiltered"

        /** Ignore the name, which this in-memory set never shows. */
        override fun setName(name: String) {
            //
        }

        /** Delete nothing, since the set has no file. */
        override fun deleteBackingFile() {
            //
        }

        /** Report the filter as enabled, as every filter is. */
        override fun get(filter: PropertyFilter): Boolean = true

        /** Ignore the change, so the filter stays enabled. */
        override fun set(
            filter: PropertyFilter,
            enabled: Boolean,
        ) {
            //
        }

        /** Ignore the change, so every filter of the category stays enabled. */
        override fun set(
            category: ProtCategory,
            enabled: Boolean,
        ) {
            //
        }

        /** Ignore the change, so every filter of the direction stays enabled. */
        override fun set(
            streamDirection: StreamDirection,
            enabled: Boolean,
        ) {
            //
        }

        /** Ignore the change, so every filter stays enabled. */
        override fun setAll(enabled: Boolean) {
            //
        }

        /** Keep every filter enabled, in place of the defaults that disable some. */
        override fun setDefaults() {
            //
        }

        /** Get an empty list, since the set has no regex filters. */
        override fun getRegexFilters(): List<RegexFilter> = emptyList()

        /** Ignore the filter, so the set stays without regex filters. */
        override fun addRegexFilter(regexFilter: RegexFilter) {
            //
        }

        /** Remove nothing, since the set holds no regex filters. */
        override fun removeRegexFilter(regexFilter: RegexFilter) {
            //
        }

        /** Replace nothing, since the set holds no regex filters. */
        override fun replaceRegexFilter(
            oldRegexFilter: RegexFilter,
            newRegexFilter: RegexFilter,
        ) {
            //
        }

        /** Clear nothing, since the set holds no regex filters. */
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
    /** The settings that drop output, which this store reports as off. */
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

    /** The number of setting sets, which is always one. */
    override val size: Int = 1

    /** Get the one setting set, since this store never makes another. */
    override fun create(name: String): SettingSet = Fixed

    /** Keep the one setting set and report that nothing was deleted. */
    override fun delete(index: Int): SettingSet? = null

    /** Get the one setting set, which sits at index 0. */
    override fun get(index: Int): SettingSet? = if (index == 0) Fixed else null

    /** Get the one setting set, which is always the active one. */
    override fun getActive(): SettingSet = Fixed

    /** Ignore the index, since the one setting set is always the active one. */
    override fun setActive(index: Int) {
        //
    }

    private object Fixed : SettingSet {
        /** Get a creation time of zero, since the set was never saved. */
        override fun getCreationTime(): Long = 0

        /** Get the fixed name of the set. */
        override fun getName(): String = "mcp"

        /** Ignore the name, which this in-memory set never shows. */
        override fun setName(name: String) {
            //
        }

        /** Delete nothing, since the set has no file. */
        override fun deleteBackingFile() {
            //
        }

        /** Determine if the setting is on: its default, unless it is one that drops output. */
        override fun get(setting: Setting): Boolean = setting.enabled && setting !in off

        /** Ignore the change, so the setting keeps its fixed value. */
        override fun set(
            setting: Setting,
            enabled: Boolean,
        ) {
            //
        }

        /** Ignore the change, so every setting of the category keeps its fixed value. */
        override fun set(
            category: SettingCategory,
            enabled: Boolean,
        ) {
            //
        }

        /** Ignore the change, so every setting of the group keeps its fixed value. */
        override fun set(
            group: SettingGroup,
            enabled: Boolean,
        ) {
            //
        }

        /** Ignore the change, so every setting keeps its fixed value. */
        override fun setAll(enabled: Boolean) {
            //
        }

        /** Keep the fixed values, in place of the defaults that drop output. */
        override fun setDefaults() {
            //
        }
    }
}
