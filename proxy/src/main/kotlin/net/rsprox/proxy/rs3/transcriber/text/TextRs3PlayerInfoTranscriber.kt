package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.PlayerUpdateType
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3PlayerInfoTranscriber
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.group
import net.rsprox.shared.property.regular.AnyProperty
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore

public class TextRs3PlayerInfoTranscriber(
    private val sessionState: Rs3SessionState,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : Rs3PlayerInfoTranscriber {
    private val root: RootProperty
        get() = checkNotNull(sessionState.root.lastOrNull()) {
            "No active root - onTranscribeStart() must run before dispatching to a transcriber method"
        }
    private val filters: PropertyFilterSet
        get() = filterSetStore.getActive()
    private val settings: SettingSet
        get() = settingSetStore.getActive()

    private fun omit() {
        sessionState.deleteRoot()
    }

    private fun Property.player(index: Int): ChildProperty<*> {
        return child(AnyProperty("player", sessionState.playerLabel(index), String::class.java))
    }

    private fun Property.extBlock(info: String) {
        val labelEnd = info.indexOfFirst { it == '(' || it == '[' }.let { if (it == -1) info.length else it }
        val label = info.substring(0, labelEnd).ifBlank { "EXT" }
        group(label) {
            child(AnyProperty("value", info, String::class.java))
        }
    }

    override fun playerInfo(message: PlayerInfo) {
        if (!filters[PropertyFilter.PLAYER_INFO]) return omit()
        val group =
            root.group {
                for ((index, update) in message.updates) {
                    when (update) {
                        PlayerUpdateType.LowResolutionIdle -> {
                            // noop
                        }
                        is PlayerUpdateType.HighResolutionIdle -> {
                            val skipExtendedInfo = !filters[PropertyFilter.PLAYER_EXT_INFO]
                            if (skipExtendedInfo || update.extendedInfo.isEmpty()) continue
                            group("IDLE") {
                                player(index)
                                for (info in update.extendedInfo) extBlock(info)
                            }
                        }
                        is PlayerUpdateType.LowResolutionToHighResolution -> {
                            val skipAdd = !filters[PropertyFilter.PLAYER_ADD]
                            val skipExtendedInfo = !filters[PropertyFilter.PLAYER_EXT_INFO]
                            if (skipAdd && (skipExtendedInfo || update.extendedInfo.isEmpty())) continue
                            group("ADD") {
                                player(index)
                                if (!skipAdd) {
                                    child(AnyProperty("level", update.level, Int::class.java))
                                    child(AnyProperty("x", update.x, Int::class.java))
                                    child(AnyProperty("z", update.z, Int::class.java))
                                }
                                if (!skipExtendedInfo) {
                                    for (info in update.extendedInfo) extBlock(info)
                                }
                            }
                        }
                        is PlayerUpdateType.HighResolutionMovement -> {
                            val skipMovement = !filters[PropertyFilter.PLAYER_MOVEMENT]
                            val skipExtendedInfo = !filters[PropertyFilter.PLAYER_EXT_INFO]
                            if (skipMovement && (skipExtendedInfo || update.extendedInfo.isEmpty())) continue
                            val label = if (update.teleport) "TELEPORT" else "MOVE"
                            group(label) {
                                player(index)
                                if (!skipMovement) {
                                    if (update.ambiguous) {
                                        child(AnyProperty("ambiguous", true, Boolean::class.java))
                                        child(AnyProperty("rawX", update.rawX ?: 0, Int::class.java))
                                        child(AnyProperty("rawZ", update.rawZ ?: 0, Int::class.java))
                                    } else {
                                        child(AnyProperty("level", update.level ?: -1, Int::class.java))
                                        child(AnyProperty("x", update.x ?: -1, Int::class.java))
                                        child(AnyProperty("z", update.z ?: -1, Int::class.java))
                                    }
                                }
                                if (!skipExtendedInfo) {
                                    for (info in update.extendedInfo) extBlock(info)
                                }
                            }
                        }
                        is PlayerUpdateType.LowResolutionMovement -> {
                            if (!filters[PropertyFilter.PLAYER_MOVEMENT]) continue
//                            group("LOWRES_MOVE") {
//                                player(index)
//                                child(AnyProperty("level", update.level, Int::class.java))
//                                child(AnyProperty("chunkX", update.chunkX, Int::class.java))
//                                child(AnyProperty("chunkZ", update.chunkZ, Int::class.java))
//                            }
                        }
                        is PlayerUpdateType.HighResolutionToLowResolution -> {
                            if (!filters[PropertyFilter.PLAYER_DEL]) continue
                            group("DEL") {
                                player(index)
                                child(AnyProperty("level", update.level, Int::class.java))
                                child(AnyProperty("chunkX", update.chunkX, Int::class.java))
                                child(AnyProperty("chunkZ", update.chunkZ, Int::class.java))
                            }
                        }
                    }
                }
            }

        val children = group.children
        if (children.isEmpty()) {
            if (settings[Setting.PLAYER_INFO_HIDE_EMPTY]) {
                return omit()
            }
            root.children.clear()
            return
        }
        root.children.clear()
        root.children.addAll(children)
    }
}
