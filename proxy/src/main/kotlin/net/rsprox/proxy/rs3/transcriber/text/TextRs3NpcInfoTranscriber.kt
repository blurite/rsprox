package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.AnimationExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcMask
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.OpaqueExtendedInfo
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3NpcInfoTranscriber
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.any
import net.rsprox.shared.property.filteredBoolean
import net.rsprox.shared.property.group
import net.rsprox.shared.property.namedEnum
import net.rsprox.shared.property.regular.AnyProperty
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore

public class TextRs3NpcInfoTranscriber(
    private val sessionState: Rs3SessionState,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : Rs3NpcInfoTranscriber {
    private val coordinates = Rs3CoordinateProperties(sessionState, settingSetStore)
    private val entities = Rs3EntityProperties(sessionState, settingSetStore)

    private fun Property.coordGrid(
        level: Int,
        x: Int,
        z: Int,
        name: String = "coord",
    ): ScriptVarTypeProperty<*> = coordinates.append(this, level, x, z, name)

    private val root: RootProperty
        get() =
            checkNotNull(sessionState.root.lastOrNull()) {
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
        return entities.npc(this, index)
    }

    private fun enabled(info: NpcExtendedInfo): Boolean {
        val filter =
            when (info) {
                is AnimationExtendedInfo -> PropertyFilter.NPC_SEQUENCE
                is OpaqueExtendedInfo -> return true
                is NpcMask ->
                    when (info.key.bit) {
                        6 -> PropertyFilter.NPC_SAY
                        20 -> PropertyFilter.NPC_HEAD_CUSTOMISATION
                        28 -> PropertyFilter.NPC_TINTING
                        2 -> PropertyFilter.NPC_TRANSFORMATION
                        14 -> PropertyFilter.NPC_EXACTMOVE
                        18 -> PropertyFilter.NPC_NAME_CHANGE
                        25 -> PropertyFilter.NPC_ENABLED_OPS
                        1, 7 -> PropertyFilter.NPC_FACING
                        12 -> PropertyFilter.NPC_BAS
                        10 -> PropertyFilter.NPC_BODY_CUSTOMISATION
                        24 -> PropertyFilter.NPC_SPOTANIMS
                        5, 33 -> PropertyFilter.NPC_HITS
                        17 -> PropertyFilter.NPC_LEVEL_CHANGE
                        3 -> PropertyFilter.NPC_SEQUENCE
                        22 -> PropertyFilter.NPC_HEADICON_CUSTOMISATION
                        else -> return true
                    }
                else -> return true
            }
        return filters[filter]
    }

    private fun Property.extBlock(info: NpcExtendedInfo, baseCoord: CoordGrid) {
        when (info) {
            is NpcMask ->
                appendNpcMask(
                    info,
                    entities,
                    coordinates,
                    baseCoord,
                    settings[Setting.EXACTMOVE_SUBTRACT_FIRST_DELAY],
                )
            is AnimationExtendedInfo -> {
                group("ANIMATION") {
                    scriptVarType("anim", ScriptVarType.SEQ, info.animId)
                    child(AnyProperty("speed", info.speed, Int::class.java))
                }
            }
            is OpaqueExtendedInfo -> {
                val rendered = info.rendered
                val labelEnd =
                    rendered.indexOfFirst { it == '(' || it == '[' }.let {
                        if (it ==
                            -1
                        ) {
                            rendered.length
                        } else {
                            it
                        }
                    }
                val label = rendered.substring(0, labelEnd).ifBlank { "EXT" }
                group(label) {
                    // This is a pre-rendered diagnostic block, not a decoded text field.
                    any("value", rendered)
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
                            val skipMovement =
                                !filters[PropertyFilter.NPC_MOVEMENT] ||
                                    update.movementType == NpcUpdateType.MovementType.EXT_ONLY
                            val skipExtendedInfo = !filters[PropertyFilter.NPC_EXT_INFO]
                            val visibleExtendedInfo = update.extendedInfo.filter(::enabled)
                            if (skipMovement && (skipExtendedInfo || visibleExtendedInfo.isEmpty())) {
                                continue
                            }
                            val label =
                                when (update.movementType) {
                                    NpcUpdateType.MovementType.WALK -> "WALK"
                                    NpcUpdateType.MovementType.RUN -> "RUN"
                                    NpcUpdateType.MovementType.CRAWL -> "CRAWL"
                                    NpcUpdateType.MovementType.EXT_ONLY -> "IDLE"
                                }
                            group(label) {
                                npc(index)
                                if (!skipMovement) {
                                    coordGrid(update.level, update.x, update.z, "newcoord")
                                    update.direction1?.let { first ->
                                        val second = update.direction2
                                        if (second == null) {
                                            namedEnum("step", Rs3NpcStep.entries[first])
                                        } else {
                                            namedEnum("step1", Rs3NpcStep.entries[first])
                                            namedEnum("step2", Rs3NpcStep.entries[second])
                                        }
                                    }
                                }
                                if (!skipExtendedInfo) {
                                    for (info in visibleExtendedInfo) {
                                        // Same-frame movement has already changed the native queue's endpoint;
                                        // sessionState still holds the pre-movement coordinate until after logging.
                                        extBlock(info, CoordGrid(update.level, update.x, update.z))
                                    }
                                }
                            }
                        }
                        is NpcUpdateType.Add -> {
                            val skipAdd = !filters[PropertyFilter.NPC_ADD]
                            val skipExtendedInfo = !filters[PropertyFilter.NPC_EXT_INFO]
                            val visibleExtendedInfo = update.extendedInfo.filter(::enabled)
                            if (skipAdd && (skipExtendedInfo || visibleExtendedInfo.isEmpty())) {
                                continue
                            }
                            group("ADD") {
                                npc(index)
                                if (!skipAdd) {
                                    // Native spawn yaw is direction * pi/4, with the model's half-turn correction.
                                    namedEnum("spawnangle", Rs3NpcStep.entries[update.direction])
                                    filteredBoolean("teleport", update.teleport)
                                }
                                if (!skipExtendedInfo) {
                                    for (info in visibleExtendedInfo) {
                                        extBlock(info, CoordGrid(update.level, update.x, update.z))
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
