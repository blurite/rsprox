package net.rsprox.transcriber.rs3.text

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.AnimationExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcMask
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.OpaqueExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey
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
import net.rsprox.transcriber.rs3.interfaces.Rs3NpcInfoTranscriber
import net.rsprox.transcriber.rs3.state.Rs3SessionState

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
                is OpaqueExtendedInfo -> PropertyFilter.NPC_UNKNOWN_EXT_INFO
                is NpcMask ->
                    when (info.key) {
                        Rs3NpcUpdateMaskKey.SAY -> PropertyFilter.NPC_SAY
                        Rs3NpcUpdateMaskKey.HEAD_CUSTOMISATION -> PropertyFilter.NPC_HEAD_CUSTOMISATION
                        Rs3NpcUpdateMaskKey.TINTING -> PropertyFilter.NPC_TINTING
                        Rs3NpcUpdateMaskKey.TRANSFORMATION -> PropertyFilter.NPC_TRANSFORMATION
                        Rs3NpcUpdateMaskKey.EXACT_MOVE -> PropertyFilter.NPC_EXACTMOVE
                        Rs3NpcUpdateMaskKey.NAME_CHANGE -> PropertyFilter.NPC_NAME_CHANGE
                        Rs3NpcUpdateMaskKey.DISABLED_OPS -> PropertyFilter.NPC_ENABLED_OPS
                        Rs3NpcUpdateMaskKey.FACE_ENTITY, Rs3NpcUpdateMaskKey.FACE_TILE -> PropertyFilter.NPC_FACING
                        Rs3NpcUpdateMaskKey.BAS_OVERRIDE -> PropertyFilter.NPC_BAS
                        Rs3NpcUpdateMaskKey.BODY_CUSTOMISATION -> PropertyFilter.NPC_BODY_CUSTOMISATION
                        Rs3NpcUpdateMaskKey.SPOTANIM -> PropertyFilter.NPC_SPOTANIMS
                        Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS_V1,
                        Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS_V2,
                        -> PropertyFilter.NPC_HITS
                        Rs3NpcUpdateMaskKey.COMBAT_LEVEL_CHANGE -> PropertyFilter.NPC_LEVEL_CHANGE
                        Rs3NpcUpdateMaskKey.SEQUENCE -> PropertyFilter.NPC_SEQUENCE
                        Rs3NpcUpdateMaskKey.HEADICON_CUSTOMISATION -> PropertyFilter.NPC_HEADICON_CUSTOMISATION
                        Rs3NpcUpdateMaskKey.PRIORITY_OFFSET -> PropertyFilter.NPC_PRIORITY_OFFSET
                        Rs3NpcUpdateMaskKey.OVERLAP_CULLING -> PropertyFilter.NPC_OVERLAP_CULLING
                        Rs3NpcUpdateMaskKey.VARNPC_FULL -> PropertyFilter.NPC_VARNPC_FULL
                        Rs3NpcUpdateMaskKey.VARNPC_DELTA -> PropertyFilter.NPC_VARNPC_DELTA
                        Rs3NpcUpdateMaskKey.NPC_STATS -> PropertyFilter.NPC_STATS
                        Rs3NpcUpdateMaskKey.ATTACHMENTS -> PropertyFilter.NPC_ATTACHMENTS
                        Rs3NpcUpdateMaskKey.UNUSED_MASK_0 -> PropertyFilter.NPC_UNUSED_MASK_0
                        Rs3NpcUpdateMaskKey.UNUSED_MASK_8 -> PropertyFilter.NPC_UNUSED_MASK_8
                        Rs3NpcUpdateMaskKey.UNUSED_MASK_11 -> PropertyFilter.NPC_UNUSED_MASK_11
                        Rs3NpcUpdateMaskKey.UNUSED_MASK_15 -> PropertyFilter.NPC_UNUSED_MASK_15
                        Rs3NpcUpdateMaskKey.UNUSED_MASK_29 -> PropertyFilter.NPC_UNUSED_MASK_29
                        Rs3NpcUpdateMaskKey.UNUSED_MASK_30 -> PropertyFilter.NPC_UNUSED_MASK_30
                        Rs3NpcUpdateMaskKey.UNUSED_MASK_31 -> PropertyFilter.NPC_UNUSED_MASK_31
                    }
                else -> PropertyFilter.NPC_UNKNOWN_EXT_INFO
            }
        return filters[filter]
    }

    private fun Property.extBlock(
        info: NpcExtendedInfo,
        baseCoord: CoordGrid,
    ) {
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
