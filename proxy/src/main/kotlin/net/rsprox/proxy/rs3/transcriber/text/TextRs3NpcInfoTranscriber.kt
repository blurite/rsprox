package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.extendedinfo.NpcExtendedInfo
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.extendedinfo.AnimationExtendedInfo
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.extendedinfo.OpaqueExtendedInfo
import net.rsprox.proxy.rs3.gameval.Rs3GamevalLookup
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3NpcInfoTranscriber
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

public class TextRs3NpcInfoTranscriber(
    private val sessionState: Rs3SessionState,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : Rs3NpcInfoTranscriber {
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

    private fun Property.npc(index: Int): ChildProperty<*> {
        return child(AnyProperty("npc", sessionState.npcLabel(index), String::class.java))
    }

    private fun Property.extBlock(info: NpcExtendedInfo) {
        when (info) {
            is AnimationExtendedInfo -> {
                group("ANIMATION") {
                    child(AnyProperty("anim", Rs3GamevalLookup.seq(info.animId), String::class.java))
                    child(AnyProperty("speed", info.speed, Int::class.java))
                }
            }
            is OpaqueExtendedInfo -> {
                val rendered = info.rendered
                val labelEnd = rendered.indexOfFirst { it == '(' || it == '[' }.let { if (it == -1) rendered.length else it }
                val label = rendered.substring(0, labelEnd).ifBlank { "EXT" }
                group(label) {
                    child(AnyProperty("value", rendered, String::class.java))
                }
            }
        }
    }

    override fun npcInfo(message: NpcInfo) {
        if (!filters[PropertyFilter.NPC_INFO]) return omit()
        val group =
            root.group {
                for ((index, update) in message.updates) {
                    when (update) {
                        NpcUpdateType.Idle -> {
                        }
                        is NpcUpdateType.Active -> {
                            val skipMovement = !filters[PropertyFilter.NPC_MOVEMENT]
                            val skipExtendedInfo = !filters[PropertyFilter.NPC_EXT_INFO]
                            if (skipMovement && (skipExtendedInfo || update.extendedInfo.isEmpty())) {
                                continue
                            }
                            val label =
                                when (update.movementType) {
                                    NpcUpdateType.MovementType.WALK -> "WALK"
                                    NpcUpdateType.MovementType.RUN -> "RUN"
                                    NpcUpdateType.MovementType.STEP_ALT -> "STEP_ALT"
                                    NpcUpdateType.MovementType.EXT_ONLY -> "EXT_ONLY"
                                }
                            group(label) {
                                npc(index)
                                if (!skipMovement) {
                                    child(AnyProperty("level", update.level, Int::class.java))
                                    child(AnyProperty("x", update.x, Int::class.java))
                                    child(AnyProperty("z", update.z, Int::class.java))
                                }
                                if (!skipExtendedInfo) {
                                    for (info in update.extendedInfo) {
                                        extBlock(info)
                                    }
                                }
                            }
                        }
                        is NpcUpdateType.Add -> {
                            val skipAdd = !filters[PropertyFilter.NPC_ADD]
                            val skipExtendedInfo = !filters[PropertyFilter.NPC_EXT_INFO]
                            if (skipAdd && (skipExtendedInfo || update.extendedInfo.isEmpty())) {
                                continue
                            }
                            group("ADD") {
                                npc(index)
                                if (!skipAdd) {
                                    child(AnyProperty("id", Rs3GamevalLookup.npc(update.id), String::class.java))
                                    child(AnyProperty("level", update.level, Int::class.java))
                                    child(AnyProperty("x", update.x, Int::class.java))
                                    child(AnyProperty("z", update.z, Int::class.java))
                                    child(AnyProperty("dir", update.direction, Int::class.java))
                                }
                                if (!skipExtendedInfo) {
                                    for (info in update.extendedInfo) {
                                        extBlock(info)
                                    }
                                }
                            }
                        }
                        NpcUpdateType.Remove -> {
                            if (filters[PropertyFilter.NPC_DEL]) {
                                group("DEL") {
                                    npc(index)
                                }
                            }
                        }
                    }
                }
            }

        val children = group.children
        if (children.isEmpty()) {
            if (settings[Setting.NPC_INFO_HIDE_EMPTY]) {
                return omit()
            }
            root.children.clear()
            return
        }
        root.children.clear()
        root.children.addAll(children)
    }
}
