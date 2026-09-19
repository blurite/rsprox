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
import net.rsprox.shared.property.any
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.long
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.string
import net.rsprox.shared.property.varp
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
                    is PlayerExtendedInfo.FaceEntity, is PlayerExtendedInfo.FaceAngle ->
                        filters[PropertyFilter.PLAYER_FACING]
                    is PlayerExtendedInfo.Tinting -> filters[PropertyFilter.PLAYER_TINTING]
                    is PlayerExtendedInfo.Sequence -> filters[PropertyFilter.PLAYER_SEQUENCE]
                    is PlayerExtendedInfo.Spotanims ->
                        filters[PropertyFilter.PLAYER_SPOTANIMS] &&
                            (it.removals.isNotEmpty() || it.additions.isNotEmpty())
                    is PlayerExtendedInfo.ExactMove -> filters[PropertyFilter.PLAYER_EXACTMOVE]
                    is PlayerExtendedInfo.SayV1, is PlayerExtendedInfo.SayV2 -> filters[PropertyFilter.PLAYER_SAY]
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
            // Movement is committed after transcription; use this update's destination for mask offsets.
            val baseCoord = when (update) {
                is PlayerUpdateType.HighResolutionMovement -> CoordGrid(update.level, update.x, update.z)
                is PlayerUpdateType.LowResolutionToHighResolution -> CoordGrid(update.level, update.x, update.z)
                else -> sessionState.getPlayerOrNull(index)?.let { player ->
                    val level = player.level
                    val x = player.x
                    val z = player.z
                    if (level == null || x == null || z == null) null else CoordGrid(level, x, z)
                }
            }
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
                if (showMasks) for (info in visibleInfo) extendedInfo(info, baseCoord)
            }
        }
        if (root.children.isEmpty() && settings[Setting.PLAYER_INFO_HIDE_EMPTY]) sessionState.deleteRoot()
    }

    private fun Property.appendExactMove(info: PlayerExtendedInfo.ExactMove, baseCoord: CoordGrid?) {
        val subtractFirstDelay = settingSetStore.getActive()[Setting.EXACTMOVE_SUBTRACT_FIRST_DELAY]
        if (baseCoord == null) {
            int("deltax1", info.deltaX1)
            int("deltaz1", info.deltaZ1)
            int("deltalevel1", info.deltaLevel1)
        } else {
            // Resolve each endpoint in instance space before translating it, including plane changes.
            coordGrid(
                baseCoord.level + info.deltaLevel1,
                baseCoord.x + info.deltaX1,
                baseCoord.z + info.deltaZ1,
                "to1",
            )
        }
        int(if (subtractFirstDelay) "delay" else "delay1", info.delay1)
        if (baseCoord == null) {
            int("deltax2", info.deltaX2)
            int("deltaz2", info.deltaZ2)
            int("deltalevel2", info.deltaLevel2)
        } else {
            coordGrid(
                baseCoord.level + info.deltaLevel2,
                baseCoord.x + info.deltaX2,
                baseCoord.z + info.deltaZ2,
                "to2",
            )
        }
        if (subtractFirstDelay) {
            int("duration", info.delay2 - info.delay1)
        } else {
            int("delay2", info.delay2)
        }
        int("angle", info.angle)
    }

    private fun Property.extendedInfo(info: PlayerExtendedInfo, baseCoord: CoordGrid?) {
        when (info) {
            is PlayerExtendedInfo.UndecodedAppearance -> group("APPEARANCE") {
                string("status", "undecoded: ${info.reason}")
                int("length", info.payload.size)
                string("payload", info.payload.joinToString(" ") { (it.toInt() and 255).toString(16).padStart(2, '0') })
            }
            is PlayerExtendedInfo.Appearance -> appendAppearance(info, filterSetStore.getActive())
            is PlayerExtendedInfo.Hits -> appendHits(info)
            is PlayerExtendedInfo.Variables -> group(if (info.full) "VARP_FULL" else "VARP_DELTA") {
                for (entry in info.entries) {
                    group {
                        varp("varp", entry.id)
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
            is PlayerExtendedInfo.Attachments -> group("ATTACHMENTS") {
                int("count", info.count)
                if (info.count == 0) boolean("reset", true)
                for (attachment in info.attachments) {
                    group {
                        int("slot", attachment.slot)
                        when {
                            attachment.flags and 0x400 != 0 ->
                                scriptVarType("obj", ScriptVarType.OBJ, requireNotNull(attachment.id))
                            attachment.flags and 0x800 != 0 -> {
                                val id = requireNotNull(attachment.id)
                                // Native narrows only VFX IDs to 16 bits. Preserve unusual wire values too.
                                int("vfx", id and 0xFFFF)
                                if (id != (id and 0xFFFF)) int("rawid", id)
                            }
                            // No resource setter is invoked; listed slots are still retained.
                            else -> boolean("retain", true)
                        }
                        int("flags", attachment.flags)
                        boolean("rotateoffset", attachment.flags and 0x40 != 0)
                        axes("translation", attachment.translation)
                        axes("rotation", attachment.rotation)
                        axes("scale", attachment.scale)
                    }
                }
            }
            is PlayerExtendedInfo.Sequence -> group("SEQUENCE") {
                appendSequences(info.ids, info.delay)
            }
            is PlayerExtendedInfo.ClanMember -> group("CLAN_MEMBER") {
                boolean("enabled", info.enabled)
            }
            is PlayerExtendedInfo.FaceEntity -> group("FACE_ENTITY") { entities.entity(this, info.target) }
            is PlayerExtendedInfo.Unused -> group(info.kind.name) {
                int("field0", info.field0)
                int("field1", info.field1)
                int("field2", info.field2)
            }
            is PlayerExtendedInfo.SayV2 -> group("SAY_V2") {
                string("text", info.text)
                boolean("chatbox", info.chatbox)
            }
            is PlayerExtendedInfo.ExactMove -> group("EXACTMOVE") { appendExactMove(info, baseCoord) }
            is PlayerExtendedInfo.Tinting -> group("TINTING") {
                appendTint(info.hue, info.saturation, info.lightness, info.weight, info.start, info.end)
            }
            is PlayerExtendedInfo.UnusedMask16 -> group("UNUSED_MASK_16") {
                int("field0", info.field0)
                int("field1", info.field1)
                int("field2", info.field2)
                int("field3", info.field3)
            }
            is PlayerExtendedInfo.PlayerStatus -> group("PLAYER_STATUS") {
                when (info.value) {
                    0 -> any("status", "normal")
                    1 -> any("status", "group_member")
                    // Note: status 2 selects cyan minimap/alternate headbar sprites.
                    // Its exact role still needs confirmation.
                    else -> int("status", info.value)
                }
            }
            is PlayerExtendedInfo.SayV1 -> group("SAY_V1") { string("text", info.text) }
            is PlayerExtendedInfo.FaceAngle -> group("FACE_ANGLE") { int("angle", info.angle) }
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
            is PlayerExtendedInfo.HeadIcons -> group("HEAD_ICONS") {
                boolean("update", info.update)
                // Removal can fade out using previously stored flags/duration; it is not an instant reset.
                if (info.update && info.slots.isEmpty()) boolean("removeall", true)
                for (slot in info.slots) {
                    group {
                        int("headiconslot", slot.slot)
                        scriptVarType("id", ScriptVarType.GRAPHIC, slot.id)
                        int("spriteindex", slot.spriteIndex)
                        int("flags", slot.flags)
                        boolean("fadein", slot.fadeIn)
                        boolean("fadeout", slot.fadeOut)
                        int("fadeinms", slot.fadeInDuration)
                        int("fadeoutms", slot.fadeOutDuration)
                        int("restartkey", slot.restartKey)
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
            appendHit(hit.type, hit.value, hit.secondaryType, hit.secondaryValue, hit.delay, info.wide)
        }
        for (headbar in info.headbars) {
            group(if (info.wide) "HEADBAR_V2" else "HEADBAR_V1") {
                scriptVarType("id", ScriptVarType.HEADBAR, headbar.type)
                when (headbar) {
                    is PlayerExtendedInfo.Headbar.Remove -> boolean("removed", true)
                    is PlayerExtendedInfo.Headbar.Update -> {
                        appendHeadbarFill(headbar.startFill, headbar.endFill, headbar.delay, headbar.duration)
                        headbar.secondary?.let { secondary ->
                            group("secondary") {
                                scriptVarType("id", ScriptVarType.HEADBAR, secondary.id)
                                int("startfill", secondary.startFill)
                                int("endfill", secondary.endFill)
                            }
                        }
                    }
                }
            }
        }
    }
}
