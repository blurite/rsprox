package net.rsprox.mcp.packets

import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.proxy.filters.UnmodifiablePropertyFilterSet
import net.rsprox.proxy.settings.DefaultSettingSetStore
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.settings.NopSettingSet
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore
import java.nio.file.Path

/**
 * Every filter enabled, no regex filters, nothing persisted. The stores on disk belong to the GUI,
 * and every mutator of the default filter set saves to them. Outside the GUI only the active set is
 * asked for, which reads and writes no file.
 */
internal object UnfilteredFilterSetStore :
    PropertyFilterSetStore by DefaultPropertyFilterSetStore(Path.of("."), mutableListOf(AllEnabled))

/** The one filter set of the store, in which every filter is on, so that no packet is omitted. */
private object AllEnabled : PropertyFilterSet by UnmodifiablePropertyFilterSet() {
    /** Report the filter as enabled, as every filter is. */
    override fun get(filter: PropertyFilter): Boolean = true
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
internal object TapSettingSetStore :
    SettingSetStore by DefaultSettingSetStore(Path.of("."), mutableListOf(Fixed))

/** The one setting set of the store, whose values cannot be changed. */
private object Fixed : SettingSet by NopSettingSet {
    /** The settings that drop output, which this set reports as off. */
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

    /** Determine if the setting is on: its default, unless it is one that drops output. */
    override fun get(setting: Setting): Boolean = setting.enabled && setting !in off
}
