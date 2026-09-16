package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.PlayerUpdateType
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3PlayerInfoTranscriber
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.long
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.string
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSetStore

public class TextRs3PlayerInfoTranscriber(
    private val sessionState: Rs3SessionState,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : Rs3PlayerInfoTranscriber {
    private val coordinates = Rs3CoordinateProperties(sessionState, settingSetStore)
    private val entities = Rs3EntityProperties(sessionState, settingSetStore)

    private fun Property.coordGrid(
        level: Int,
        x: Int,
        z: Int,
        name: String = "coord",
    ): ScriptVarTypeProperty<*> = coordinates.append(this, level, x, z, name)

    private val root: RootProperty
        get() = checkNotNull(sessionState.root.lastOrNull()) { "No active transcript root" }

    private fun Property.player(index: Int): ChildProperty<*> {
        return entities.player(this, index)
    }

    override fun playerInfo(message: PlayerInfo) {
        val filters = filterSetStore.getActive()
        val settings = settingSetStore.getActive()
        if (!filters[PropertyFilter.PLAYER_INFO]) return sessionState.deleteRoot()
        for ((index, update) in message.updates) {
            if (settings[Setting.PLAYER_INFO_LOCAL_PLAYER_ONLY] && index != sessionState.localPlayerIndex) continue
            val extendedInfo = when (update) {
                is PlayerUpdateType.HighResolutionIdle -> update.extendedInfo
                is PlayerUpdateType.LowResolutionToHighResolution -> update.extendedInfo
                is PlayerUpdateType.HighResolutionMovement -> update.extendedInfo
                else -> emptyList()
            }
            val visibleInfo = extendedInfo.filter {
                when (it) {
                    is PlayerExtendedInfo.Hits ->
                        filters[PropertyFilter.PLAYER_HITS] && (it.hits.isNotEmpty() || it.headbars.isNotEmpty())
                    is PlayerExtendedInfo.UndecodedAppearance, is PlayerExtendedInfo.Appearance ->
                        filters[PropertyFilter.PLAYER_APPEARANCE]
                    is PlayerExtendedInfo.FaceEntity -> filters[PropertyFilter.PLAYER_FACING]
                    else -> true
                }
            }
            val showMasks = filters[PropertyFilter.PLAYER_EXT_INFO] && visibleInfo.isNotEmpty()
            val showUpdate = when (update) {
                is PlayerUpdateType.LowResolutionToHighResolution -> filters[PropertyFilter.PLAYER_ADD]
                is PlayerUpdateType.HighResolutionToLowResolution -> filters[PropertyFilter.PLAYER_DEL]
                is PlayerUpdateType.HighResolutionMovement -> filters[PropertyFilter.PLAYER_MOVEMENT]
                else -> false
            }
            if (!showUpdate && !showMasks) continue
            val label = when (update) {
                is PlayerUpdateType.LowResolutionToHighResolution -> "ADD"
                is PlayerUpdateType.HighResolutionToLowResolution -> "DEL"
                is PlayerUpdateType.HighResolutionMovement ->
                    if (update.teleport) "TELEPORT" else Rs3MovementMode.fromId(update.movementMode)?.name ?: "MOVE"
                else -> "IDLE"
            }
            root.group(label) {
                player(index)
                if (showUpdate) {
                    when (update) {
                        is PlayerUpdateType.LowResolutionToHighResolution -> {
                            coordGrid(update.level, update.x, update.z)
                            appendMovementMode(update.movementMode)
                        }
                        is PlayerUpdateType.HighResolutionMovement -> {
                            coordGrid(update.level, update.x, update.z, "newcoord")
                            if (Rs3MovementMode.fromId(update.movementMode) == null) {
                                appendMovementMode(update.movementMode)
                            }
                            if (update.steps.size > 1) {
                                group("steps") {
                                    for (step in update.steps) group { coordGrid(update.level, step.x, step.z) }
                                }
                            }
                        }
                        else -> Unit
                    }
                }
                if (showMasks) for (info in visibleInfo) extendedInfo(info)
            }
        }
        if (root.children.isEmpty() && settings[Setting.PLAYER_INFO_HIDE_EMPTY]) sessionState.deleteRoot()
    }

    private fun Property.extendedInfo(info: PlayerExtendedInfo) {
        when (info) {
            is PlayerExtendedInfo.UndecodedAppearance -> group("APPEARANCE") {
                string("status", "undecoded: ${info.reason}")
                int("length", info.payload.size)
                string("payload", info.payload.joinToString(" ") { (it.toInt() and 255).toString(16).padStart(2, '0') })
            }
            is PlayerExtendedInfo.Appearance -> appendAppearance(info)
            is PlayerExtendedInfo.Hits -> appendHits(info)
            is PlayerExtendedInfo.Variables -> group(if (info.full) "VARIABLES_FULL" else "VARIABLES_DELTA") {
                for (entry in info.entries) {
                    group {
                        int("id", entry.id)
                        when (val value = entry.value) {
                            is PlayerExtendedInfo.VariableValue.IntegerValue -> int("value", value.value)
                            is PlayerExtendedInfo.VariableValue.LongValue -> long("value", value.value)
                            is PlayerExtendedInfo.VariableValue.StringValue -> string("value", value.value)
                            is PlayerExtendedInfo.VariableValue.Coordinate -> group("value") {
                                // This is a four-component fine coordinate, not a packed tile COORDGRID.
                                int("level", value.level)
                                int("x", value.x)
                                int("y", value.y)
                                int("z", value.z)
                            }
                        }
                    }
                }
            }
            is PlayerExtendedInfo.BoneTransforms -> group("BONE_TRANSFORMS") {
                int("count", info.count)
                for (transform in info.transforms) {
                    group {
                        int("slot", transform.slot)
                        transform.id?.let { int("id", it) }
                        int("flags", transform.flags)
                        axes("translation", transform.translation)
                        axes("rotation", transform.rotation)
                        axes("scale", transform.scale)
                    }
                }
            }
            is PlayerExtendedInfo.Sequence -> group("SEQUENCE") {
                appendSequences(info.animations, info.delay)
            }
            is PlayerExtendedInfo.PriorityFlag -> group("PRIORITY_FLAG") {
                boolean("enabled", info.enabled)
                filteredInt("raw", info.value, if (info.enabled) 1 else 0)
            }
            is PlayerExtendedInfo.FaceEntity -> group("FACE_ENTITY") { entities.face(this, info.target) }
            is PlayerExtendedInfo.EnabledOps -> group("ENABLED_OPS") {
                int("field0", info.field0)
                int("field1", info.field1)
                int("field2", info.field2)
            }
            is PlayerExtendedInfo.TimedEffect -> group(info.kind.name) {
                int("field0", info.field0)
                int("field1", info.field1)
                int("field2", info.field2)
            }
            is PlayerExtendedInfo.ForwardedChat -> group("CHAT") {
                string("text", info.message)
                int("flags", info.flags)
            }
            is PlayerExtendedInfo.ExactMove -> group("EXACT_MOVE") {
                int("field0", info.field0)
                int("field1", info.field1)
                int("field2", info.field2)
                int("field3", info.field3)
                int("field4", info.field4)
                int("field5", info.field5)
                int("field6", info.field6)
                int("field7", info.field7)
                int("field8", info.field8)
            }
            is PlayerExtendedInfo.Tinting -> group("TINTING") {
                appendTint(info.field0, info.field1, info.field2, info.field3, info.field4, info.field5)
            }
            is PlayerExtendedInfo.ScaleChange -> group("SCALE_CHANGE") {
                int("field0", info.field0)
                int("field1", info.field1)
                int("field2", info.field2)
                int("field3", info.field3)
            }
            is PlayerExtendedInfo.Transparency -> group("TRANSPARENCY") { int("value", info.value) }
            is PlayerExtendedInfo.Say -> group("SAY") { string("text", info.text) }
            is PlayerExtendedInfo.HeadTurn -> group("FACE_ANGLE") { int("angle", info.angle) }
            is PlayerExtendedInfo.Spotanims -> {
                for (id in info.removals) appendSpotanimRemoval(id)
                for (spotanim in info.additions) {
                    appendSpotanim(
                        spotanim.slot,
                        spotanim.id,
                        spotanim.packedHeightDelay,
                        spotanim.rotationFlags,
                        spotanim.packedOffsets,
                    )
                }
            }
            is PlayerExtendedInfo.MotionSlots -> group("MOTION_SLOTS") {
                for (slot in info.slots) {
                    group {
                        int("slot", slot.slot)
                        int("id", slot.id)
                        int("value", slot.value)
                        int("flags", slot.flags)
                        int("startDuration", slot.startDuration)
                        int("endDuration", slot.endDuration)
                        int("key", slot.key)
                    }
                }
            }
        }
    }

    private fun Property.axes(name: String, axes: PlayerExtendedInfo.TransformAxes) {
        if (axes.x == null && axes.y == null && axes.z == null) return
        group(name) {
            axes.x?.let { int("x", it) }
            axes.y?.let { int("y", it) }
            axes.z?.let { int("z", it) }
        }
    }

    private fun Property.appendHits(info: PlayerExtendedInfo.Hits) {
        for (hit in info.hits) {
            appendHit(hit.type, hit.value, hit.secondaryType, hit.secondaryValue, hit.delay)
        }
        for (headbar in info.headbars) {
            group("HEADBAR") {
                scriptVarType("id", ScriptVarType.HEADBAR, headbar.type)
                when (headbar) {
                    is PlayerExtendedInfo.Headbar.Remove -> boolean("removed", true)
                    is PlayerExtendedInfo.Headbar.Update -> {
                        appendHeadbarFill(headbar.startFill, headbar.endFill, headbar.delay, headbar.duration)
                        headbar.extra?.let { extra ->
                            group("extra") {
                                int("id", extra.id)
                                int("startfill", extra.startFill)
                                int("endfill", extra.endFill)
                            }
                        }
                    }
                }
            }
        }
    }
}
