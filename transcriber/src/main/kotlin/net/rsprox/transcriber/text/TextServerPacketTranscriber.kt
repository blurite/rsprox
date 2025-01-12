package net.rsprox.transcriber.text

import net.rsprot.crypto.xtea.XteaKey
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.type.VarBitType
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAt
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtEasedCoord
import net.rsprox.protocol.game.outgoing.model.camera.CamMode
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveTo
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToArc
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToCycles
import net.rsprox.protocol.game.outgoing.model.camera.CamReset
import net.rsprox.protocol.game.outgoing.model.camera.CamRotateBy
import net.rsprox.protocol.game.outgoing.model.camera.CamRotateTo
import net.rsprox.protocol.game.outgoing.model.camera.CamShake
import net.rsprox.protocol.game.outgoing.model.camera.CamSmoothReset
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetV1
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetV2
import net.rsprox.protocol.game.outgoing.model.camera.OculusSync
import net.rsprox.protocol.game.outgoing.model.clan.ClanChannelDelta
import net.rsprox.protocol.game.outgoing.model.clan.ClanChannelFull
import net.rsprox.protocol.game.outgoing.model.clan.ClanSettingsDelta
import net.rsprox.protocol.game.outgoing.model.clan.ClanSettingsFull
import net.rsprox.protocol.game.outgoing.model.clan.MessageClanChannel
import net.rsprox.protocol.game.outgoing.model.clan.MessageClanChannelSystem
import net.rsprox.protocol.game.outgoing.model.clan.VarClan
import net.rsprox.protocol.game.outgoing.model.clan.VarClanDisable
import net.rsprox.protocol.game.outgoing.model.clan.VarClanEnable
import net.rsprox.protocol.game.outgoing.model.friendchat.MessageFriendChannel
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelFullV1
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelFullV2
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelSingleUser
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.SetNpcUpdateOrigin
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfo
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV1
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV2
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV3
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityUpdateType
import net.rsprox.protocol.game.outgoing.model.interfaces.IfClearInv
import net.rsprox.protocol.game.outgoing.model.interfaces.IfCloseSub
import net.rsprox.protocol.game.outgoing.model.interfaces.IfMoveSub
import net.rsprox.protocol.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.game.outgoing.model.interfaces.IfOpenTop
import net.rsprox.protocol.game.outgoing.model.interfaces.IfResync
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetAngle
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetAnim
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetColour
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetEvents
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetHide
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetModel
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetNpcHead
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetNpcHeadActive
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetObject
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerHead
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelBaseColour
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelBodyType
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelObj
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelSelf
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPosition
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetRotateSpeed
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetScrollPos
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetText
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvFull
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvPartial
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvStopTransmit
import net.rsprox.protocol.game.outgoing.model.logout.Logout
import net.rsprox.protocol.game.outgoing.model.logout.LogoutTransfer
import net.rsprox.protocol.game.outgoing.model.logout.LogoutWithReason
import net.rsprox.protocol.game.outgoing.model.map.RebuildLogin
import net.rsprox.protocol.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.game.outgoing.model.map.RebuildRegion
import net.rsprox.protocol.game.outgoing.model.map.RebuildWorldEntityV1
import net.rsprox.protocol.game.outgoing.model.map.RebuildWorldEntityV2
import net.rsprox.protocol.game.outgoing.model.map.Reconnect
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea
import net.rsprox.protocol.game.outgoing.model.misc.client.HideLocOps
import net.rsprox.protocol.game.outgoing.model.misc.client.HideNpcOps
import net.rsprox.protocol.game.outgoing.model.misc.client.HideObjOps
import net.rsprox.protocol.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.game.outgoing.model.misc.client.HiscoreReply
import net.rsprox.protocol.game.outgoing.model.misc.client.MinimapToggle
import net.rsprox.protocol.game.outgoing.model.misc.client.PacketGroupEnd
import net.rsprox.protocol.game.outgoing.model.misc.client.PacketGroupStart
import net.rsprox.protocol.game.outgoing.model.misc.client.ReflectionChecker
import net.rsprox.protocol.game.outgoing.model.misc.client.ResetAnims
import net.rsprox.protocol.game.outgoing.model.misc.client.ResetInteractionMode
import net.rsprox.protocol.game.outgoing.model.misc.client.SendPing
import net.rsprox.protocol.game.outgoing.model.misc.client.ServerTickEnd
import net.rsprox.protocol.game.outgoing.model.misc.client.SetHeatmapEnabled
import net.rsprox.protocol.game.outgoing.model.misc.client.SetInteractionMode
import net.rsprox.protocol.game.outgoing.model.misc.client.SiteSettings
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateRebootTimer
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateUid192
import net.rsprox.protocol.game.outgoing.model.misc.client.UrlOpen
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettings
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettingsPrivateChat
import net.rsprox.protocol.game.outgoing.model.misc.player.MessageGame
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript
import net.rsprox.protocol.game.outgoing.model.misc.player.SetMapFlag
import net.rsprox.protocol.game.outgoing.model.misc.player.SetPlayerOp
import net.rsprox.protocol.game.outgoing.model.misc.player.TriggerOnDialogAbort
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateRunEnergy
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateRunWeight
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatV1
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatV2
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStockMarketSlot
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateTradingPost
import net.rsprox.protocol.game.outgoing.model.social.FriendListLoaded
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivate
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivateEcho
import net.rsprox.protocol.game.outgoing.model.social.UpdateFriendList
import net.rsprox.protocol.game.outgoing.model.social.UpdateIgnoreList
import net.rsprox.protocol.game.outgoing.model.sound.MidiJingle
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongStop
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongV1
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongV2
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongWithSecondary
import net.rsprox.protocol.game.outgoing.model.sound.MidiSwap
import net.rsprox.protocol.game.outgoing.model.sound.SynthSound
import net.rsprox.protocol.game.outgoing.model.specific.LocAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.MapAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcHeadIconSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcSpotAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.PlayerAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.PlayerSpotAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV3
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownString
import net.rsprox.protocol.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.game.outgoing.model.varp.VarpReset
import net.rsprox.protocol.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.game.outgoing.model.varp.VarpSync
import net.rsprox.protocol.game.outgoing.model.worldentity.ClearEntities
import net.rsprox.protocol.game.outgoing.model.worldentity.SetActiveWorld
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocAddChange
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocDel
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocMerge
import net.rsprox.protocol.game.outgoing.model.zone.payload.MapAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.MapProjAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjAdd
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjCount
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjCustomise
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjDel
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjEnabledOps
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjUncustomise
import net.rsprox.protocol.game.outgoing.model.zone.payload.SoundArea
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea
import net.rsprox.protocol.reflection.ReflectionCheck
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.*
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.regular.ZoneCoordProperty
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.interfaces.ServerPacketTranscriber
import net.rsprox.transcriber.maxUShortToMinusOne
import net.rsprox.transcriber.state.SessionState
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@Suppress("SpellCheckingInspection", "DuplicatedCode")
public class TextServerPacketTranscriber(
    private val sessionState: SessionState,
    private val cache: Cache,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
    private val formatterCollection: PropertyFormatterCollection,
) : ServerPacketTranscriber {
    private val root: RootProperty
        get() = checkNotNull(sessionState.root.last())
    private val filters: PropertyFilterSet
        get() = filterSetStore.getActive()

    private val settings: SettingSet
        get() = settingSetStore.getActive()

    private fun omit() {
        sessionState.deleteRoot()
    }

    private fun Property.npc(index: Int): ChildProperty<*> {
        val world = sessionState.getActiveWorld()
        val npc = world.getNpcOrNull(index) ?: return unidentifiedNpc(index)
        val finalIndex =
            if (settings[Setting.HIDE_NPC_INDICES]) {
                Int.MIN_VALUE
            } else {
                index
            }
        val multinpc = sessionState.resolveMultinpc(npc.id, cache)
        val coord = sessionState.getActiveWorld().getInstancedCoordOrSelf(npc.coord)
        return if (multinpc != null) {
            identifiedMultinpc(
                finalIndex,
                npc.id,
                multinpc.id,
                multinpc.name,
                coord.level,
                coord.x,
                coord.z,
            )
        } else {
            identifiedNpc(
                finalIndex,
                npc.id,
                npc.name ?: "null",
                coord.level,
                coord.x,
                coord.z,
            )
        }
    }

    private fun Property.player(
        index: Int,
        name: String = "player",
    ): ChildProperty<*> {
        val player = sessionState.getPlayerOrNull(index)
        val finalIndex =
            if (settings[Setting.PLAYER_HIDE_INDEX]) {
                Int.MIN_VALUE
            } else {
                index
            }
        return if (player != null) {
            val coord = sessionState.getActiveWorld().getInstancedCoordOrSelf(player.coord)
            identifiedPlayer(
                finalIndex,
                player.name,
                coord.level,
                coord.x,
                coord.z,
                name,
            )
        } else {
            unidentifiedPlayer(index, name)
        }
    }

    private fun Property.worldentity(
        index: Int,
        name: String = "worldentity",
    ): ChildProperty<*> {
        if (index == -1) {
            return string("worldentity", "root")
        }
        val world = sessionState.getWorldOrNull(index)
        return if (world != null) {
            identifiedWorldEntity(
                index,
                world.coord.level,
                world.coord.x,
                world.coord.z,
                world.sizeX,
                world.sizeZ,
                world.centerFineOffsetX,
                world.centerFineOffsetZ,
                name,
            )
        } else {
            unidentifiedWorldEntity(index, name)
        }
    }

    private fun buildAreaCoordGrid(
        xInBuildArea: Int,
        zInBuildArea: Int,
        level: Int = -1,
    ): CoordGrid {
        val world = sessionState.getActiveWorld()
        val coord =
            world.relativizeBuildAreaCoord(
                xInBuildArea,
                zInBuildArea,
                if (level == -1) sessionState.level() else level,
            )
        return world.getInstancedCoordOrSelf(coord)
    }

    private fun Property.coordGrid(coordGrid: CoordGrid): ScriptVarTypeProperty<*> {
        val coord = sessionState.getActiveWorld().getInstancedCoordOrSelf(coordGrid)
        return coordGridProperty(coord.level, coord.x, coord.z)
    }

    private fun Property.coordGrid(
        name: String,
        coordGrid: CoordGrid,
    ): ScriptVarTypeProperty<*> {
        val coord = sessionState.getActiveWorld().getInstancedCoordOrSelf(coordGrid)
        return coordGridProperty(coord.level, coord.x, coord.z, name)
    }

    private fun Property.coordGrid(
        level: Int,
        x: Int,
        z: Int,
        name: String = "coord",
    ): ScriptVarTypeProperty<*> {
        val coord = sessionState.getActiveWorld().getInstancedCoordOrSelf(CoordGrid(level, x, z))
        return coordGridProperty(coord.level, coord.x, coord.z, name)
    }

    private fun Property.zoneCoord(
        name: String,
        level: Int,
        zoneX: Int,
        zoneZ: Int,
    ): ZoneCoordProperty {
        return zoneCoordGrid(level, zoneX shl 3, zoneZ shl 3, name)
    }

    override fun camLookAt(message: CamLookAt) {
        if (!filters[PropertyFilter.CAM_LOOKAT]) return omit()
        val coordInBuildArea = CoordInBuildArea(message.destinationXInBuildArea, message.destinationZInBuildArea)
        if (coordInBuildArea.invalid()) {
            root.any(
                "outofboundsbuildareacoord",
                "[zoneX=${coordInBuildArea.zoneX}, xInZone=${coordInBuildArea.xInZone}, " +
                    "zoneZ=${coordInBuildArea.zoneZ}, zInZone=${coordInBuildArea.zInZone}]",
            )
        } else {
            root.coordGrid(buildAreaCoordGrid(coordInBuildArea.xInBuildArea, coordInBuildArea.zInBuildArea))
        }
        root.int("height", message.height)
        root.int("rate", message.speed)
        root.int("rate2", message.acceleration)
    }

    override fun camLookAtEasedCoord(message: CamLookAtEasedCoord) {
        if (!filters[PropertyFilter.CAM_LOOKAT]) return omit()
        val coordInBuildArea = CoordInBuildArea(message.destinationXInBuildArea, message.destinationZInBuildArea)
        if (coordInBuildArea.invalid()) {
            root.any(
                "outofboundsbuildareacoord",
                "[zoneX=${coordInBuildArea.zoneX}, xInZone=${coordInBuildArea.xInZone}, " +
                    "zoneZ=${coordInBuildArea.zoneZ}, zInZone=${coordInBuildArea.zInZone}]",
            )
        } else {
            root.coordGrid(buildAreaCoordGrid(coordInBuildArea.xInBuildArea, coordInBuildArea.zInBuildArea))
        }
        root.int("height", message.height)
        root.int("cycles", message.duration)
        root.enum("easing", message.function)
    }

    override fun camMode(message: CamMode) {
        if (!filters[PropertyFilter.CAM_MODE]) return omit()
        root.int("mode", message.mode)
    }

    override fun camMoveTo(message: CamMoveTo) {
        if (!filters[PropertyFilter.CAM_MOVETO]) return omit()
        val coordInBuildArea = CoordInBuildArea(message.destinationXInBuildArea, message.destinationZInBuildArea)
        if (coordInBuildArea.invalid()) {
            root.any(
                "outofboundsbuildareacoord",
                "[zoneX=${coordInBuildArea.zoneX}, xInZone=${coordInBuildArea.xInZone}, " +
                    "zoneZ=${coordInBuildArea.zoneZ}, zInZone=${coordInBuildArea.zInZone}]",
            )
        } else {
            root.coordGrid(buildAreaCoordGrid(coordInBuildArea.xInBuildArea, coordInBuildArea.zInBuildArea))
        }
        root.int("height", message.height)
        root.int("rate", message.speed)
        root.int("rate2", message.acceleration)
    }

    override fun camMoveToArc(message: CamMoveToArc) {
        if (!filters[PropertyFilter.CAM_MOVETO]) return omit()
        val coordInBuildArea = CoordInBuildArea(message.destinationXInBuildArea, message.destinationZInBuildArea)
        if (coordInBuildArea.invalid()) {
            root.any(
                "outofboundsbuildareacoord",
                "[zoneX=${coordInBuildArea.zoneX}, xInZone=${coordInBuildArea.xInZone}, " +
                    "zoneZ=${coordInBuildArea.zoneZ}, zInZone=${coordInBuildArea.zInZone}]",
            )
        } else {
            root.coordGrid(buildAreaCoordGrid(coordInBuildArea.xInBuildArea, coordInBuildArea.zInBuildArea))
        }
        root.int("height", message.height)
        root.coordGrid("tertiarycoord", buildAreaCoordGrid(message.centerXInBuildArea, message.centerZInBuildArea))
        root.int("cycles", message.duration)
        root.boolean("ignoreterrain", message.ignoreTerrain)
        root.enum("easing", message.function)
    }

    override fun camMoveToCycles(message: CamMoveToCycles) {
        if (!filters[PropertyFilter.CAM_MOVETO]) return omit()
        val coordInBuildArea = CoordInBuildArea(message.destinationXInBuildArea, message.destinationZInBuildArea)
        if (coordInBuildArea.invalid()) {
            root.any(
                "outofboundsbuildareacoord",
                "[zoneX=${coordInBuildArea.zoneX}, xInZone=${coordInBuildArea.xInZone}, " +
                    "zoneZ=${coordInBuildArea.zoneZ}, zInZone=${coordInBuildArea.zInZone}]",
            )
        } else {
            root.coordGrid(buildAreaCoordGrid(coordInBuildArea.xInBuildArea, coordInBuildArea.zInBuildArea))
        }
        root.int("height", message.height)
        root.int("cycles", message.duration)
        root.boolean("ignoreterrain", message.ignoreTerrain)
        root.enum("easing", message.function)
    }

    override fun camReset(message: CamReset) {
        if (!filters[PropertyFilter.CAM_RESET]) return omit()
    }

    override fun camRotateBy(message: CamRotateBy) {
        if (!filters[PropertyFilter.CAM_LOOKAT]) return omit()
        root.int("pitch", message.xAngle)
        root.int("yaw", message.yAngle)
        root.int("cycles", message.duration)
        root.enum("easing", message.function)
    }

    override fun camRotateTo(message: CamRotateTo) {
        if (!filters[PropertyFilter.CAM_LOOKAT]) return omit()
        root.int("pitch", message.xAngle)
        root.int("yaw", message.yAngle)
        root.int("cycles", message.duration)
        root.enum("easing", message.function)
    }

    override fun camShake(message: CamShake) {
        if (!filters[PropertyFilter.CAM_SHAKE]) return omit()
        root.int("axis", message.type)
        root.int("random", message.randomAmount)
        root.int("amplitude", message.sineAmount)
        root.int("rate", message.sineFrequency)
    }

    override fun camSmoothReset(message: CamSmoothReset) {
        if (!filters[PropertyFilter.CAM_RESET]) return omit()
        root.int("moveconstantspeed", message.cameraMoveConstantSpeed)
        root.int("moveproportionalspeed", message.cameraMoveProportionalSpeed)
        root.int("lookconstantspeed", message.cameraLookConstantSpeed)
        root.int("lookproportionalspeed", message.cameraLookProportionalSpeed)
    }

    override fun camTargetV2(message: CamTargetV2) {
        if (!filters[PropertyFilter.CAM_TARGET]) return omit()
        when (val type = message.type) {
            is CamTargetV2.NpcCamTarget -> {
                root.npc(type.index)
            }
            is CamTargetV2.PlayerCamTarget -> {
                root.player(type.index)
            }
            is CamTargetV2.WorldEntityTarget -> {
                root.worldentity(type.index)
                if (type.cameraLockedPlayerIndex != -1) {
                    root.player(type.index, "cameralockedplayer")
                }
            }
        }
    }

    override fun camTargetV1(message: CamTargetV1) {
        if (!filters[PropertyFilter.CAM_TARGET]) return omit()
        when (val type = message.type) {
            is CamTargetV1.NpcCamTarget -> {
                root.npc(type.index)
            }
            is CamTargetV1.PlayerCamTarget -> {
                root.player(type.index)
            }
            is CamTargetV1.WorldEntityTarget -> {
                root.worldentity(type.index)
            }
        }
    }

    override fun oculusSync(message: OculusSync) {
        if (!filters[PropertyFilter.OCULUS_SYNC]) return omit()
        root.int("value", message.value)
    }

    override fun clanChannelDelta(message: ClanChannelDelta) {
        if (!filters[PropertyFilter.CLANCHANNEL]) return omit()
        root.int("clantype", message.clanType)
        root.long("clanhash", message.clanHash)
        root.long("updatenum", message.updateNum)
        for (event in message.events) {
            when (event) {
                is ClanChannelDelta.ClanChannelDeltaAddUserEvent -> {
                    root.group("ADD_USER") {
                        string("name", event.name)
                        int("world", event.world)
                        int("rank", event.rank)
                    }
                }
                is ClanChannelDelta.ClanChannelDeltaDeleteUserEvent -> {
                    root.group("DEL_USER") {
                        int("memberindex", event.index)
                    }
                }
                is ClanChannelDelta.ClanChannelDeltaUpdateBaseSettingsEvent -> {
                    root.group("UPDATE_BASE_SETTINGS") {
                        filteredString("clanname", event.clanName, null)
                        int("talkrank", event.talkRank)
                        int("kickrank", event.kickRank)
                    }
                }
                is ClanChannelDelta.ClanChannelDeltaUpdateUserDetailsEvent -> {
                    root.group("UPDATE_USER_DETAILS") {
                        int("memberindex", event.index)
                        string("name", event.name)
                        int("rank", event.rank)
                        int("world", event.world)
                    }
                }
                is ClanChannelDelta.ClanChannelDeltaUpdateUserDetailsV2Event -> {
                    root.group("UPDATE_USER_DETAILS") {
                        int("memberindex", event.index)
                        string("name", event.name)
                        int("rank", event.rank)
                        int("world", event.world)
                    }
                }
            }
        }
    }

    override fun clanChannelFull(message: ClanChannelFull) {
        if (!filters[PropertyFilter.CLANCHANNEL]) return omit()
        root.int("clantype", message.clanType)
        when (val update = message.update) {
            is ClanChannelFull.ClanChannelFullJoinUpdate -> {
                root.group("DETAILS") {
                    int("flags", update.flags)
                    int("version", update.version)
                    long("clanhash", update.clanHash)
                    long("updatenum", update.updateNum)
                    string("clanname", update.clanName)
                    boolean("discarded", update.discardedBoolean)
                    int("kickrank", update.kickRank)
                    int("talkrank", update.talkRank)
                }
                root.group("MEMBERS") {
                    for (member in update.members) {
                        group {
                            string("name", member.name)
                            int("rank", member.rank)
                            int("world", member.world)
                            boolean("discarded", member.discardedBoolean)
                        }
                    }
                }
            }
            ClanChannelFull.ClanChannelFullLeaveUpdate -> {
            }
        }
    }

    override fun clanSettingsDelta(message: ClanSettingsDelta) {
        if (!filters[PropertyFilter.CLANSETTINGS]) return omit()
        root.int("clantype", message.clanType)
        root.long("ownerhash", message.owner)
        root.int("updatenum", message.updateNum)
        root.group("UPDATES") {
            for (update in message.updates) {
                when (update) {
                    is ClanSettingsDelta.ClanSettingsDeltaSetClanOwnerUpdate -> {
                        group("SET_CLAN_OWNER") {
                            int("memberindex", update.index)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaAddBannedUpdate -> {
                        group("ADD_BANNED") {
                            filteredLong("hash", update.hash, 0)
                            filteredString("name", update.name, null)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaAddMemberV1Update -> {
                        group("ADD_MEMBER_V1") {
                            filteredLong("hash", update.hash, 0)
                            filteredString("name", update.name, null)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaAddMemberV2Update -> {
                        group("ADD_MEMBER_V2") {
                            filteredLong("hash", update.hash, 0)
                            filteredString("name", update.name, null)
                            int("joinruneday", update.joinRuneDay)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaBaseSettingsUpdate -> {
                        group("BASE_SETTINGS") {
                            boolean("allowunaffined", update.allowUnaffined)
                            int("talkrank", update.talkRank)
                            int("kickrank", update.kickRank)
                            int("lootsharerank", update.lootshareRank)
                            int("coinsharerank", update.coinshareRank)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaDeleteBannedUpdate -> {
                        group("DELETE_BANNED") {
                            int("memberindex", update.index)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaDeleteMemberUpdate -> {
                        group("DELETE_MEMBER") {
                            int("memberindex", update.index)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaSetClanNameUpdate -> {
                        group("SET_CLAN_NAME") {
                            string("clanname", update.clanName)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaSetIntSettingUpdate -> {
                        group("SET_INT_SETTING") {
                            int("id", update.setting)
                            int("value", update.value)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaSetLongSettingUpdate -> {
                        group("SET_LONG_SETTING") {
                            int("id", update.setting)
                            long("value", update.value)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaSetMemberExtraInfoUpdate -> {
                        group("SET_MEMBER_EXTRA_INFO") {
                            int("memberindex", update.index)
                            int("value", update.value)
                            int("startbit", update.startBit)
                            int("endbit", update.endBit)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaSetMemberMutedUpdate -> {
                        group("SET_MEMBER_MUTED") {
                            int("memberindex", update.index)
                            boolean("muted", update.muted)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaSetMemberRankUpdate -> {
                        group("SET_MEMBER_RANK") {
                            int("memberindex", update.index)
                            int("rank", update.rank)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaSetStringSettingUpdate -> {
                        group("SET_STRING_SETTING") {
                            int("id", update.setting)
                            string("value", update.value)
                        }
                    }
                    is ClanSettingsDelta.ClanSettingsDeltaSetVarbitSettingUpdate -> {
                        group("SET_VARBIT_SETTING") {
                            int("id", update.setting)
                            int("value", update.value)
                            int("startbit", update.startBit)
                            int("endbit", update.endBit)
                        }
                    }
                }
            }
        }
    }

    private fun formatEpochTimeMinute(num: Int): String {
        val epochTimeMillis = TimeUnit.MINUTES.toMillis(num.toLong())
        return SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(epochTimeMillis))
    }

    override fun clanSettingsFull(message: ClanSettingsFull) {
        if (!filters[PropertyFilter.CLANSETTINGS_FULL_REQUEST]) return omit()
        root.int("clantype", message.clanType)
        when (val update = message.update) {
            is ClanSettingsFull.ClanSettingsFullJoinUpdate -> {
                root.int("flags", update.flags)
                root.int("updatenum", update.updateNum)
                root.string("creationtime", formatEpochTimeMinute(update.creationTime))
                root.string("clanname", update.clanName)
                root.boolean("allowunaffined", update.allowUnaffined)
                root.int("talkrank", update.talkRank)
                root.int("kickrank", update.kickRank)
                root.filteredInt("lootsharerank", update.lootshareRank, 0)
                root.filteredInt("coinsharerank", update.coinshareRank, 0)
                if (update.affinedMembers.isNotEmpty()) {
                    root.group("AFFINED_MEMBERS") {
                        for (member in update.affinedMembers) {
                            group {
                                filteredLong("hash", member.hash, 0)
                                filteredString("name", member.name, null)
                                int("rank", member.rank)
                                int("extrainfo", member.extraInfo)
                                int("joinruneday", member.joinRuneDay)
                                filteredBoolean("muted", member.muted)
                            }
                        }
                    }
                }
                if (update.bannedMembers.isNotEmpty()) {
                    root.group("BANNED_MEMBERS") {
                        for (member in update.bannedMembers) {
                            group {
                                filteredLong("hash", member.hash, 0)
                                filteredString("name", member.name, null)
                            }
                        }
                    }
                }
                if (update.settings.isNotEmpty()) {
                    root.group("SETTINGS") {
                        for (setting in update.settings) {
                            group {
                                when (setting) {
                                    is ClanSettingsFull.IntClanSetting -> {
                                        int("id", setting.id)
                                        int("int", setting.value)
                                    }
                                    is ClanSettingsFull.LongClanSetting -> {
                                        int("id", setting.id)
                                        long("long", setting.value)
                                    }
                                    is ClanSettingsFull.StringClanSetting -> {
                                        int("id", setting.id)
                                        string("string", setting.value)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ClanSettingsFull.ClanSettingsFullLeaveUpdate -> {}
        }
    }

    override fun messageClanChannel(message: MessageClanChannel) {
        if (!filters[PropertyFilter.MESSAGE_CLANCHANNEL]) return omit()
        root.int("clantype", message.clanType)
        root.string("name", message.name)
        root.int("world", message.worldId)
        root.int("mescount", message.worldMessageCounter)
        root.filteredInt("chatcrown", message.chatCrownType, 0)
        root.string("message", message.message)
    }

    override fun messageClanChannelSystem(message: MessageClanChannelSystem) {
        if (!filters[PropertyFilter.MESSAGE_CLANCHANNEL]) return omit()
        root.int("clantype", message.clanType)
        root.int("world", message.worldId)
        root.int("mescount", message.worldMessageCounter)
        root.string("message", message.message)
    }

    override fun varClan(message: VarClan) {
        if (!filters[PropertyFilter.VARCLAN]) return omit()
        root.int("id", message.id)
        when (val value = message.value) {
            is VarClan.UnknownVarClanData -> {
                root.string("unknown", value.data.contentToString())
            }
            is VarClan.VarClanIntData -> {
                root.int("int", value.value)
            }
            is VarClan.VarClanLongData -> {
                root.long("long", value.value)
            }
            is VarClan.VarClanStringData -> {
                root.string("string", value.value)
            }
        }
    }

    override fun varClanDisable(message: VarClanDisable) {
        if (!filters[PropertyFilter.VARCLAN]) return omit()
    }

    override fun varClanEnable(message: VarClanEnable) {
        if (!filters[PropertyFilter.VARCLAN]) return omit()
    }

    override fun messageFriendChannel(message: MessageFriendChannel) {
        if (!filters[PropertyFilter.MESSAGE_FRIENDCHANNEL]) return omit()
        root.string("name", message.sender)
        root.string("channelname", message.channelName)
        root.int("world", message.worldId)
        root.int("mescount", message.worldMessageCounter)
        root.int("chatcrown", message.chatCrownType)
        root.string("message", message.message)
    }

    override fun updateFriendChatChannelFullV1(message: UpdateFriendChatChannelFullV1) {
        if (!filters[PropertyFilter.UPDATE_FRIENDCHAT_CHANNEL_FULL]) return omit()
        when (val update = message.updateType) {
            is UpdateFriendChatChannelFullV1.JoinUpdate -> {
                root.string("owner", update.channelOwner)
                root.string("channelname", update.channelName)
                root.int("kickrank", update.kickRank)
                root.group("MEMBERS") {
                    for (member in update.entries) {
                        group {
                            string("name", member.name)
                            int("world", member.worldId)
                            string("worldname", member.worldName)
                            int("rank", member.rank)
                        }
                    }
                }
            }
            UpdateFriendChatChannelFullV1.LeaveUpdate -> {
            }
        }
    }

    override fun updateFriendChatChannelFullV2(message: UpdateFriendChatChannelFullV2) {
        if (!filters[PropertyFilter.UPDATE_FRIENDCHAT_CHANNEL_FULL]) return omit()
        when (val update = message.updateType) {
            is UpdateFriendChatChannelFullV2.JoinUpdate -> {
                root.string("owner", update.channelOwner)
                root.string("channelname", update.channelName)
                root.int("kickrank", update.kickRank)
                root.group("MEMBERS") {
                    for (member in update.entries) {
                        group {
                            string("name", member.name)
                            int("world", member.worldId)
                            string("worldname", member.worldName)
                            int("rank", member.rank)
                        }
                    }
                }
            }
            UpdateFriendChatChannelFullV2.LeaveUpdate -> {
            }
        }
    }

    override fun updateFriendChatChannelSingleUser(message: UpdateFriendChatChannelSingleUser) {
        if (!filters[PropertyFilter.UPDATE_FRIENDCHAT_CHANNEL_SINGLEUSER]) return omit()
        when (val user = message.user) {
            is UpdateFriendChatChannelSingleUser.AddedFriendChatUser -> {
                root.string("type", "add")
                root.string("name", user.name)
                root.int("world", user.worldId)
                root.string("worldname", user.worldName)
                root.int("rank", user.rank)
            }
            is UpdateFriendChatChannelSingleUser.RemovedFriendChatUser -> {
                root.string("type", "del")
                root.string("name", user.name)
                root.int("world", user.worldId)
                root.int("rank", user.rank)
            }
        }
    }

    override fun setNpcUpdateOrigin(message: SetNpcUpdateOrigin) {
        if (!filters[PropertyFilter.SET_NPC_UPDATE_ORIGIN]) return omit()
        root.coordGrid(buildAreaCoordGrid(message.originX, message.originZ))
    }

    override fun worldEntityInfoV1(message: WorldEntityInfoV1) {
        worldEntityInfo(message)
    }

    override fun worldEntityInfoV2(message: WorldEntityInfoV2) {
        worldEntityInfo(message)
    }

    override fun worldEntityInfoV3(message: WorldEntityInfoV3) {
        worldEntityInfo(message)
    }

    private fun worldEntityInfo(message: WorldEntityInfo) {
        if (!filters[PropertyFilter.WORLDENTITY_INFO]) return omit()
        val group =
            root.group {
                for ((index, update) in message.updates) {
                    when (update) {
                        is WorldEntityUpdateType.ActiveV2 -> {
                            group("ACTIVE") {
                                worldentity(index)
                                int("angle", update.angle)
                                val world = sessionState.getWorld(index)
                                val coordGrid = update.coordFine.toCoordGrid(world.level)
                                coordGrid("newcoord", coordGrid)
                                val coordFine = update.coordFine
                                val coordFineX = coordFine.x and 0x7F
                                val coordFineY = coordFine.y
                                val coordFineZ = coordFine.z and 0x7F
                                int("finex", coordFineX)
                                int("finey", coordFineY)
                                int("finez", coordFineZ)
                            }
                        }
                        WorldEntityUpdateType.HighResolutionToLowResolution -> {
                            group("DEL") {
                                worldentity(index)
                            }
                        }
                        WorldEntityUpdateType.Idle -> {
                            // noop
                        }
                        is WorldEntityUpdateType.LowResolutionToHighResolutionV2 -> {
                            group("ADD") {
                                worldentity(index)
                                int("angle", update.angle)
                                val coordFine = update.coordFine
                                val coordFineX = coordFine.x and 0x7F
                                val coordFineY = coordFine.y
                                val coordFineZ = coordFine.z and 0x7F
                                int("finex", coordFineX)
                                int("finey", coordFineY)
                                int("finez", coordFineZ)
                            }
                        }
                        is WorldEntityUpdateType.ActiveV1 -> {
                            group("ACTIVE") {
                                worldentity(index)
                                int("angle", update.angle)
                                string("movespeed", "${update.moveSpeed.id * 0.5} tiles/gamecycle")
                                coordGrid("newcoord", update.coordGrid)
                            }
                        }
                        is WorldEntityUpdateType.LowResolutionToHighResolutionV1 -> {
                            group("ADD") {
                                worldentity(index)
                                int("angle", update.angle)
                                int("unknown", update.unknownProperty)
                            }
                        }
                    }
                }
            }
        val children = group.children
        // If no children were added to the root group, it means no worldentities are being updated
        // In this case, remove the empty line that the group is generating
        if (children.isEmpty()) {
            root.children.clear()
            return
        }
        // Remove the empty line generated by the wrapper group
        root.children.clear()
        root.children.addAll(children)
    }

    override fun ifClearInv(message: IfClearInv) {
        if (!filters[PropertyFilter.IF_CLEARINV]) return omit()
        root.com(message.interfaceId, message.componentId)
    }

    override fun ifCloseSub(message: IfCloseSub) {
        if (!filters[PropertyFilter.IF_CLOSESUB]) return omit()
        val interfaceId = sessionState.getOpenInterface(message.combinedId)
        root.com(message.interfaceId, message.componentId)
        if (interfaceId != null) {
            root.inter(interfaceId)
        }
    }

    override fun ifMoveSub(message: IfMoveSub) {
        if (!filters[PropertyFilter.IF_MOVESUB]) return omit()
        root.com("sourcecom", message.sourceInterfaceId, message.sourceComponentId)
        root.com("destcom", message.destinationInterfaceId, message.destinationComponentId)
        val interfaceId = sessionState.getOpenInterface(message.sourceCombinedId)
        if (interfaceId != null) {
            root.inter(interfaceId)
        }
    }

    private enum class IfType(
        override val prettyName: String,
    ) : NamedEnum {
        MODAL("modal"),
        OVERLAY("overlay"),
        CLIENT("client"),
    }

    private fun getIfType(id: Int): IfType {
        return when (id) {
            0 -> IfType.MODAL
            1 -> IfType.OVERLAY
            3 -> IfType.CLIENT
            else -> error("Unknown type: $id")
        }
    }

    override fun ifOpenSub(message: IfOpenSub) {
        if (!filters[PropertyFilter.IF_OPENSUB]) return omit()
        root.com(message.destinationInterfaceId, message.destinationComponentId)
        root.inter(message.interfaceId)
        root.namedEnum("type", getIfType(message.type))
    }

    override fun ifOpenTop(message: IfOpenTop) {
        if (!filters[PropertyFilter.IF_OPENTOP]) return omit()
        val existing = sessionState.toplevelInterface
        if (existing != -1) {
            root.inter("previousid", existing)
        }
        root.inter(message.interfaceId)
    }

    private enum class EventMask(
        val mask: Int,
    ) {
        PAUSEBUTTON(1 shl 0),
        OP1(1 shl 1),
        OP2(1 shl 2),
        OP3(1 shl 3),
        OP4(1 shl 4),
        OP5(1 shl 5),
        OP6(1 shl 6),
        OP7(1 shl 7),
        OP8(1 shl 8),
        OP9(1 shl 9),
        OP10(1 shl 10),
        TGTOBJ(1 shl 11),
        TGTNPC(1 shl 12),
        TGTLOC(1 shl 13),
        TGTPLAYER(1 shl 14),
        TGTINV(1 shl 15),
        TGTCOM(1 shl 16),
        DEPTH1(1 shl 17),
        DEPTH2(2 shl 17),
        DEPTH3(3 shl 17),
        DEPTH4(4 shl 17),
        DEPTH5(5 shl 17),
        DEPTH6(6 shl 17),
        DEPTH7(7 shl 17),
        DRAGTARGET(1 shl 20),
        TARGET(1 shl 21),
        CRMTARGET(1 shl 22),
        BIT23(1 shl 23),
        BIT24(1 shl 24),
        BIT25(1 shl 25),
        BIT26(1 shl 26),
        BIT27(1 shl 27),
        BIT28(1 shl 28),
        BIT29(1 shl 29),
        BIT30(1 shl 30),
        BIT31(1 shl 31),
        ;

        companion object {
            private val firstBlockEntries = entries.filter { it < DEPTH1 }
            private val depthEntries = entries.filter { it in DEPTH1..DEPTH7 }.reversed()
            private val lastBlockEntries = entries.filter { it > DEPTH7 }

            fun list(mask: Int): List<EventMask> {
                return buildList {
                    for (entry in firstBlockEntries) {
                        if (mask and entry.mask != entry.mask) {
                            continue
                        }
                        add(entry)
                    }
                    // Depth entries get checked in reverse
                    // Only a single depth entry can be flagged as the bits are overlapping
                    // If we just allow the normal 0..31 bits logic to take place,
                    // we would flag depths 1, 4 and 5 when in reality only depth 5 is flagged
                    for (entry in depthEntries) {
                        if (mask and entry.mask != entry.mask) {
                            continue
                        }
                        add(entry)
                        break
                    }
                    for (entry in lastBlockEntries) {
                        if (mask and entry.mask != entry.mask) {
                            continue
                        }
                        add(entry)
                    }
                }
            }
        }
    }

    override fun ifResync(message: IfResync) {
        if (!filters[PropertyFilter.IF_RESYNC]) return omit()
        root.inter(message.topLevelInterface)
        root.group("SUB_INTERFACES") {
            for (sub in message.subInterfaces) {
                sessionState.openInterface(sub.interfaceId, sub.destinationCombinedId)
                group {
                    com(sub.destinationInterfaceId, sub.destinationComponentId)
                    inter(sub.interfaceId)
                    namedEnum("type", getIfType(sub.type))
                }
            }
        }
        root.group("EVENTS") {
            for (event in message.events) {
                group {
                    com(event.interfaceId, event.componentId)
                    int("start", event.start.maxUShortToMinusOne())
                    int("end", event.end.maxUShortToMinusOne())
                    any("events", EventMask.list(event.events).toString())
                }
            }
        }
    }

    override fun ifSetAngle(message: IfSetAngle) {
        if (!filters[PropertyFilter.IF_SETANGLE]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.int("anglex", message.angleX)
        root.int("angley", message.angleY)
        root.int("zoom", message.zoom)
    }

    override fun ifSetAnim(message: IfSetAnim) {
        if (!filters[PropertyFilter.IF_SETANIM]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.scriptVarType("id", ScriptVarType.SEQ, message.anim)
    }

    override fun ifSetColour(message: IfSetColour) {
        if (!filters[PropertyFilter.IF_SETCOLOUR]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.scriptVarType("colour", ScriptVarType.COLOUR, message.colour15BitPacked)
    }

    override fun ifSetEvents(message: IfSetEvents) {
        if (!filters[PropertyFilter.IF_SETEVENTS]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.int("start", message.start.maxUShortToMinusOne())
        root.int("end", message.end.maxUShortToMinusOne())
        root.any("events", EventMask.list(message.events).toString())
    }

    override fun ifSetHide(message: IfSetHide) {
        if (!filters[PropertyFilter.IF_SETHIDE]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.boolean("hide", message.hidden)
    }

    override fun ifSetModel(message: IfSetModel) {
        if (!filters[PropertyFilter.IF_SETMODEL]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.scriptVarType("id", ScriptVarType.MODEL, message.model)
    }

    override fun ifSetNpcHead(message: IfSetNpcHead) {
        if (!filters[PropertyFilter.IF_SETNPCHEAD]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.scriptVarType("id", ScriptVarType.NPC, message.npc)
    }

    override fun ifSetNpcHeadActive(message: IfSetNpcHeadActive) {
        if (!filters[PropertyFilter.IF_SETNPCHEAD_ACTIVE]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.npc(message.index)
    }

    override fun ifSetObject(message: IfSetObject) {
        if (!filters[PropertyFilter.IF_SETOBJECT]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.scriptVarType("id", ScriptVarType.OBJ, message.obj)
        root.int("zoomorcount", message.count)
    }

    override fun ifSetPlayerHead(message: IfSetPlayerHead) {
        if (!filters[PropertyFilter.IF_SETPLAYERHEAD]) return omit()
        root.com(message.interfaceId, message.componentId)
    }

    override fun ifSetPlayerModelBaseColour(message: IfSetPlayerModelBaseColour) {
        if (!filters[PropertyFilter.IF_SETPLAYERMODEL]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.int("index", message.index)
        root.scriptVarType("colour", ScriptVarType.COLOUR, message.colour)
    }

    override fun ifSetPlayerModelBodyType(message: IfSetPlayerModelBodyType) {
        if (!filters[PropertyFilter.IF_SETPLAYERMODEL]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.int("bodytype", message.bodyType)
    }

    override fun ifSetPlayerModelObj(message: IfSetPlayerModelObj) {
        if (!filters[PropertyFilter.IF_SETPLAYERMODEL]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.scriptVarType("id", ScriptVarType.OBJ, message.obj)
    }

    override fun ifSetPlayerModelSelf(message: IfSetPlayerModelSelf) {
        if (!filters[PropertyFilter.IF_SETPLAYERMODEL]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.boolean("copyobjs", message.copyObjs)
    }

    override fun ifSetPosition(message: IfSetPosition) {
        if (!filters[PropertyFilter.IF_SETPOSITION]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.int("x", message.x)
        root.int("y", message.y)
    }

    override fun ifSetRotateSpeed(message: IfSetRotateSpeed) {
        if (!filters[PropertyFilter.IF_SETROTATESPEED]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.int("xspeed", message.xSpeed)
        root.int("yspeed", message.ySpeed)
    }

    override fun ifSetScrollPos(message: IfSetScrollPos) {
        if (!filters[PropertyFilter.IF_SETSCROLLPOS]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.int("scrollpos", message.scrollPos)
    }

    override fun ifSetText(message: IfSetText) {
        if (!filters[PropertyFilter.IF_SETTEXT]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.string("text", message.text)
    }

    override fun updateInvFull(message: UpdateInvFull) {
        if (!filters[PropertyFilter.UPDATE_INV]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.scriptVarType("id", ScriptVarType.INV, message.inventoryId)
        root.group("OBJS") {
            for (obj in message.objs) {
                group {
                    scriptVarType("id", ScriptVarType.OBJ, obj.id)
                    formattedInt("count", obj.count)
                }
            }
        }
    }

    override fun updateInvPartial(message: UpdateInvPartial) {
        if (!filters[PropertyFilter.UPDATE_INV]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.scriptVarType("id", ScriptVarType.INV, message.inventoryId)
        root.group("OBJS") {
            for (obj in message.objs) {
                group {
                    int("slot", obj.slot)
                    scriptVarType("id", ScriptVarType.OBJ, obj.id)
                    formattedInt("count", obj.count)
                }
            }
        }
    }

    override fun updateInvStopTransmit(message: UpdateInvStopTransmit) {
        if (!filters[PropertyFilter.UPDATE_INV]) return omit()
        root.scriptVarType("id", ScriptVarType.INV, message.inventoryId)
    }

    override fun logout(message: Logout) {
        if (!filters[PropertyFilter.LOGOUT]) return omit()
    }

    private enum class WorldFlag(
        val mask: Int,
    ) {
        MEMBERS(0x1),
        QUICKCHAT(0x2),
        PVPWORLD(0x4),
        LOOTSHARE(0x8),
        DEDICATEDACTIVITY(0x10),
        BOUNTYWORLD(0x20),
        PVPARENA(0x40),
        HIGHLEVELONLY_1500(0x80),
        SPEEDRUN(0x100),
        EXISTINGPLAYERSONLY(0x200),
        EXTRAHARDWILDERNESS(0x400),
        DUNGEONEERING(0x800),
        INSTANCE_SHARD(0x1000),
        RENTABLE(0x2000),
        LASTMANSTANDING(0x4000),
        NEW_PLAYERS(0x8000),
        BETA_WORLD(0x10000),
        STAFF_IP_ONLY(0x20000),
        HIGHLEVELONLY_2000(0x40000),
        HIGHLEVELONLY_2400(0x80000),
        VIPS_ONLY(0x100000),
        HIDDEN_WORLD(0x200000),
        LEGACY_ONLY(0x400000),
        EOC_ONLY(0x800000),
        BEHIND_PROXY(0x1000000),
        NOSAVE_MODE(0x2000000),
        TOURNAMENT_WORLD(0x4000000),
        FRESHSTART(0x8000000),
        HIGHLEVELONLY_1750(0x10000000),
        DEADMAN(0x20000000),
        SEASONAL(0x40000000),
        EXTERNAL_PARTNER_ONLY(-0x80000000),
        ;

        companion object {
            fun list(mask: Int): List<WorldFlag> {
                return buildList {
                    for (entry in WorldFlag.entries) {
                        if (mask and entry.mask != entry.mask) {
                            continue
                        }
                        add(entry)
                    }
                }
            }
        }
    }

    override fun logoutTransfer(message: LogoutTransfer) {
        if (!filters[PropertyFilter.LOGOUT]) return omit()
        root.string("host", message.host)
        root.int("id", message.id)
        root.string("properties", WorldFlag.list(message.properties).toString())
    }

    private enum class LogoutReason(
        override val prettyName: String,
    ) : NamedEnum {
        REQUESTED("requested"),
        KICKED("kicked"),
        UPDATING("updating"),
    }

    private fun getLogoutReason(id: Int): LogoutReason {
        return when (id) {
            0 -> LogoutReason.REQUESTED
            1 -> LogoutReason.KICKED
            2 -> LogoutReason.UPDATING
            else -> error("Unknown logout reason: $id")
        }
    }

    override fun logoutWithReason(message: LogoutWithReason) {
        if (!filters[PropertyFilter.LOGOUT]) return omit()
        root.namedEnum("reason", getLogoutReason(message.reason))
    }

    override fun reconnect(message: Reconnect) {
        root.coordGrid("localplayercoord", message.playerInfoInitBlock.localPlayerCoord)
    }

    override fun rebuildLogin(message: RebuildLogin) {
        if (!filters[PropertyFilter.REBUILD]) return omit()
        root.int("zonex", message.zoneX)
        root.int("zonez", message.zoneZ)
        root.int("worldarea", message.worldArea)
        root.coordGrid("localplayercoord", message.playerInfoInitBlock.localPlayerCoord)
        val minMapsquareX = (message.zoneX - 6) ushr 3
        val maxMapsquareX = (message.zoneX + 6) ushr 3
        val minMapsquareZ = (message.zoneZ - 6) ushr 3
        val maxMapsquareZ = (message.zoneZ + 6) ushr 3
        val iterator = message.keys.listIterator()
        root.group("KEYS") {
            for (mapsquareX in minMapsquareX..maxMapsquareX) {
                for (mapsquareZ in minMapsquareZ..maxMapsquareZ) {
                    val mapsquareId = (mapsquareX shl 8) or mapsquareZ
                    val key = iterator.next()
                    group {
                        int("mapsquareid", mapsquareId)
                        any("key", key.key.contentToString())
                    }
                }
            }
        }
        check(!iterator.hasNext()) {
            "Xtea keys leftover"
        }
    }

    override fun rebuildNormal(message: RebuildNormal) {
        if (!filters[PropertyFilter.REBUILD]) return omit()
        root.int("zonex", message.zoneX)
        root.int("zonez", message.zoneZ)
        root.int("worldarea", message.worldArea)
        val minMapsquareX = (message.zoneX - 6) ushr 3
        val maxMapsquareX = (message.zoneX + 6) ushr 3
        val minMapsquareZ = (message.zoneZ - 6) ushr 3
        val maxMapsquareZ = (message.zoneZ + 6) ushr 3
        val iterator = message.keys.listIterator()
        root.group("KEYS") {
            for (mapsquareX in minMapsquareX..maxMapsquareX) {
                for (mapsquareZ in minMapsquareZ..maxMapsquareZ) {
                    val mapsquareId = (mapsquareX shl 8) or mapsquareZ
                    val key = iterator.next()
                    group {
                        int("mapsquareid", mapsquareId)
                        any("key", key.key.contentToString())
                    }
                }
            }
        }
        check(!iterator.hasNext()) {
            "Xtea keys leftover"
        }
    }

    override fun rebuildRegion(message: RebuildRegion) {
        if (!filters[PropertyFilter.REBUILD]) return omit()
        root.int("zonex", message.zoneX)
        root.int("zonez", message.zoneZ)
        root.boolean("reload", message.reload)
        val mapsquares = mutableSetOf<Int>()
        root.group("BUILD_AREA") {
            val startZoneX = message.zoneX - 6
            val startZoneZ = message.zoneZ - 6
            for (level in 0..<4) {
                for (zoneX in startZoneX..(message.zoneX + 6)) {
                    for (zoneZ in startZoneZ..(message.zoneZ + 6)) {
                        val block = message.buildArea[level, zoneX - startZoneX, zoneZ - startZoneZ]
                        // Invalid zone
                        if (block.mapsquareId == 32767) continue
                        mapsquares += block.mapsquareId
                        group {
                            zoneCoord("source", block.level, block.zoneX, block.zoneZ)
                            zoneCoord("dest", level, zoneX, zoneZ)
                            int("rotation", block.rotation)
                            filteredInt("firstbit", block.packed and 0x1, 0)
                        }
                    }
                }
            }
        }
        val iterator = message.keys.listIterator()
        root.group("KEYS") {
            for (mapsquareId in mapsquares) {
                val key = iterator.next()
                group {
                    int("mapsquareid", mapsquareId)
                    any("key", key.key.contentToString())
                }
            }
        }
        check(!iterator.hasNext()) {
            "Xtea keys leftover"
        }
    }

    private fun rebuildWorldEntity(
        index: Int,
        baseX: Int,
        baseZ: Int,
        buildArea: BuildArea,
        keys: List<XteaKey>,
        playerInfoInitBlock: PlayerInfoInitBlock?,
    ) {
        if (!filters[PropertyFilter.REBUILD]) return omit()
        root.worldentity(index)
        root.int("zonex", baseX)
        root.int("zonez", baseZ)
        if (playerInfoInitBlock != null) {
            root.coordGrid("localplayercoord", playerInfoInitBlock.localPlayerCoord)
        }
        val mapsquares = mutableSetOf<Int>()
        root.group("BUILD_AREA") {
            val startZoneX = baseX - 6
            val startZoneZ = baseZ - 6
            for (level in 0..<4) {
                for (zoneX in startZoneX..(baseX + 6)) {
                    for (zoneZ in startZoneZ..(baseZ + 6)) {
                        val block = buildArea[level, zoneX - startZoneX, zoneZ - startZoneZ]
                        // Invalid zone
                        if (block.mapsquareId == 32767) continue
                        mapsquares += block.mapsquareId
                        group {
                            zoneCoord("source", block.level, block.zoneX, block.zoneZ)
                            zoneCoord("dest", level, zoneX, zoneZ)
                            int("rotation", block.rotation)
                        }
                    }
                }
            }
        }
        val iterator = keys.listIterator()
        root.group("KEYS") {
            for (mapsquareId in mapsquares) {
                val key = iterator.next()
                group {
                    int("mapsquareid", mapsquareId)
                    any("key", key.key.contentToString())
                }
            }
        }
        check(!iterator.hasNext()) {
            "Xtea keys leftover"
        }
    }

    override fun rebuildWorldEntityV1(message: RebuildWorldEntityV1) {
        rebuildWorldEntity(
            message.index,
            message.baseX,
            message.baseZ,
            message.buildArea,
            message.keys,
            message.playerInfoInitBlock,
        )
    }

    override fun rebuildWorldEntityV2(message: RebuildWorldEntityV2) {
        rebuildWorldEntity(
            message.index,
            message.baseX,
            message.baseZ,
            message.buildArea,
            message.keys,
            null,
        )
    }

    override fun hideLocOps(message: HideLocOps) {
        if (!filters[PropertyFilter.HIDEOPS]) return omit()
        root.boolean("hide", message.hidden)
    }

    override fun hideNpcOps(message: HideNpcOps) {
        if (!filters[PropertyFilter.HIDEOPS]) return omit()
        root.boolean("hide", message.hidden)
    }

    override fun hideObjOps(message: HideObjOps) {
        if (!filters[PropertyFilter.HIDEOPS]) return omit()
        root.boolean("hide", message.hidden)
    }

    override fun hintArrow(message: HintArrow) {
        if (!filters[PropertyFilter.HINT_ARROW]) return omit()
        when (val type = message.type) {
            is HintArrow.NpcHintArrow -> {
                root.npc(type.index)
            }
            is HintArrow.PlayerHintArrow -> {
                root.player(type.index)
            }
            HintArrow.ResetHintArrow -> {
                root.string("type", "reset")
            }
            is HintArrow.TileHintArrow -> {
                root.coordGrid(sessionState.level(), type.x, type.z)
                root.int("height", type.height)
                root.string(
                    "position",
                    type.position.name
                        .lowercase()
                        .replaceFirstChar { it.uppercaseChar() },
                )
            }
        }
    }

    override fun hiscoreReply(message: HiscoreReply) {
        if (!filters[PropertyFilter.HISCORE_REPLY]) return omit()
        root.int("requestid", message.requestId)
        when (val type = message.response) {
            is HiscoreReply.FailedHiscoreReply -> {
                root.any("type", "failure")
                root.string("reason", type.reason)
            }
            is HiscoreReply.SuccessfulHiscoreReply -> {
                root.any("type", "success")
                root.int("version", type.version)
                root.group("OVERALL") {
                    group {
                        int("rank", type.overallRank)
                        long("experience", type.overallExperience)
                    }
                }
                root.group("STATS") {
                    for (stat in type.statResults) {
                        group {
                            namedEnum("stat", Stat.entries.first { it.id == stat.id })
                            int("experience", stat.result)
                            int("rank", stat.rank)
                        }
                    }
                }
                root.group("ACTIVITIES") {
                    for (activity in type.statResults) {
                        group {
                            int("id", activity.id)
                            int("result", activity.result)
                            int("rank", activity.rank)
                        }
                    }
                }
            }
        }
    }

    override fun minimapToggle(message: MinimapToggle) {
        if (!filters[PropertyFilter.MINIMAP_TOGGLE]) return omit()
        root.int("state", message.minimapState)
    }

    override fun reflectionChecker(message: ReflectionChecker) {
        if (!filters[PropertyFilter.REFLECTION_CHECKER]) return omit()
        root.formattedInt("id", message.id)
        root.group("CHECKS") {
            for (check in message.checks) {
                when (check) {
                    is ReflectionCheck.GetFieldModifiers -> {
                        group("GET_FIELD_MODIFIERS") {
                            string("classname", check.className)
                            string("fieldname", check.fieldName)
                        }
                    }
                    is ReflectionCheck.GetFieldValue -> {
                        group("GET_FIELD_VALUE") {
                            string("classname", check.className)
                            string("fieldname", check.fieldName)
                        }
                    }
                    is ReflectionCheck.GetMethodModifiers -> {
                        group("GET_METHOD_MODIFIERS") {
                            string("classname", check.className)
                            string("methodname", check.methodName)
                            string("returnclass", check.returnClass)
                            any("parameterclasses", check.parameterClasses.toString())
                        }
                    }
                    is ReflectionCheck.InvokeMethod -> {
                        group("INVOKE_METHOD") {
                            string("classname", check.className)
                            string("methodname", check.methodName)
                            string("returnclass", check.returnClass)
                            any("parameterclasses", check.parameterClasses.toString())
                            any(
                                "parametervalues",
                                check.parameterValues
                                    .map { it.contentToString() }
                                    .toString(),
                            )
                        }
                    }
                    is ReflectionCheck.SetFieldValue -> {
                        group("SET_FIELD_VALUE") {
                            string("classname", check.className)
                            string("fieldname", check.fieldName)
                            int("value", check.value)
                        }
                    }
                }
            }
        }
    }

    override fun resetAnims(message: ResetAnims) {
        if (!filters[PropertyFilter.RESET_ANIMS]) return omit()
    }

    override fun sendPing(message: SendPing) {
        if (!filters[PropertyFilter.SEND_PING]) return omit()
        root.int("value1", message.value1)
        root.int("value2", message.value2)
    }

    override fun serverTickEnd(message: ServerTickEnd) {
        if (!filters[PropertyFilter.SERVER_TICK_END]) return omit()
    }

    override fun setHeatmapEnabled(message: SetHeatmapEnabled) {
        if (!filters[PropertyFilter.SET_HEATMAP_ENABLED]) return omit()
        root.boolean("enabled", message.enabled)
    }

    override fun siteSettings(message: SiteSettings) {
        if (!filters[PropertyFilter.SITE_SETTINGS]) return omit()
        root.string("settings", message.settings)
    }

    override fun updateRebootTimer(message: UpdateRebootTimer) {
        if (!filters[PropertyFilter.UPDATE_REBOOT_TIMER]) return omit()
        root.formattedInt("gamecycles", message.gameCycles)
    }

    override fun updateUid192(message: UpdateUid192) {
        if (!filters[PropertyFilter.UPDATE_UID192]) return omit()
        root.string("uid", message.uid.toString(Charsets.UTF_8))
    }

    override fun urlOpen(message: UrlOpen) {
        if (!filters[PropertyFilter.URL_OPEN]) return omit()
        root.string("url", message.url)
    }

    override fun packetGroupStart(message: PacketGroupStart) {
        if (!filters[PropertyFilter.PACKET_GROUP]) return omit()
        root.int("length", message.length)
    }

    override fun packetGroupEnd(message: PacketGroupEnd) {
        if (!filters[PropertyFilter.PACKET_GROUP]) return omit()
        root.int("bytesconsumed", message.bytesRead)
    }

    private enum class ChatFilter(
        override val prettyName: String,
    ) : NamedEnum {
        ON("on"),
        FRIENDS("friends"),
        OFF("off"),
        HIDE("hide"),
        AUTOCHAT("autochat"),
    }

    private fun getChatFilter(id: Int): ChatFilter {
        return when (id) {
            0 -> ChatFilter.ON
            1 -> ChatFilter.FRIENDS
            2 -> ChatFilter.OFF
            3 -> ChatFilter.HIDE
            4 -> ChatFilter.AUTOCHAT
            else -> error("Unknown chatfilter id: $id")
        }
    }

    override fun chatFilterSettings(message: ChatFilterSettings) {
        if (!filters[PropertyFilter.CHAT_FILTER_SETTINGS]) return omit()
        root.namedEnum("public", getChatFilter(message.publicChatFilter))
        root.namedEnum("trade", getChatFilter(message.tradeChatFilter))
    }

    override fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat) {
        if (!filters[PropertyFilter.CHAT_FILTER_SETTINGS]) return omit()
        root.namedEnum("private", getChatFilter(message.privateChatFilter))
    }

    private enum class MessageType(
        val id: Int,
    ) : NamedEnum {
        GAMEMESSAGE(0),
        MODCHAT(1),
        PUBLICCHAT(2),
        PRIVATECHAT(3),
        ENGINE(4),
        LOGINLOGOUTNOTIFICATION(5),
        PRIVATECHATOUT(6),
        MODPRIVATECHAT(7),
        FRIENDSCHAT(9),
        FRIENDSCHATNOTIFICATION(11),
        BROADCAST(14),
        SNAPSHOTFEEDBACK(26),
        OBJ_EXAMINE(27),
        NPC_EXAMINE(28),
        LOC_EXAMINE(29),
        FRIENDNOTIFICATION(30),
        IGNORENOTIFICATION(31),
        CLANCHAT(41),
        CLANMESSAGE(43),
        CLANGUESTCHAT(44),
        CLANGUESTMESSAGE(46),
        AUTOTYPER(90),
        MODAUTOTYPER(91),
        CONSOLE(99),
        TRADEREQ(101),
        TRADE(102),
        CHALREQ_TRADE(103),
        CHALREQ_FRIENDSCHAT(104),
        SPAM(105),
        PLAYERRELATED(106),
        TENSECTIMEOUT(107),
        CLANCREATIONINVITATION(109),
        CLANREQ_CLANCHAT(110),
        DIALOGUE(114),
        MESBOX(115),
        ;

        override val prettyName: String = name.lowercase()
    }

    override fun messageGame(message: MessageGame) {
        if (!filters[PropertyFilter.MESSAGE_GAME]) return omit()
        val type = MessageType.entries.firstOrNull { it.id == message.type }
        if (type != null) {
            root.namedEnum("type", type)
        } else {
            root.int("type", message.type)
        }
        root.filteredString("name", message.name, null)
        root.string("message", message.message)
    }

    override fun runClientScript(message: RunClientScript) {
        if (!filters[PropertyFilter.RUNCLIENTSCRIPT]) return omit()
        root.script("id", message.id)
        if (message.types.isEmpty() || message.values.isEmpty()) {
            return
        }
        if (settings[Setting.COLLAPSE_CLIENTSCRIPT_PARAMS]) {
            val types =
                message.types.joinToString { char ->
                    val type =
                        ScriptVarType.entries.first { type ->
                            type.char == char
                        }
                    type.fullName
                }
            val values = mutableListOf<String>()
            for (i in message.types.indices) {
                val char = message.types[i]
                val value = message.values[i].toString()
                val type =
                    ScriptVarType.entries.first { type ->
                        type.char == char
                    }
                val property =
                    createScriptVarType(
                        "",
                        type,
                        if (type ==
                            ScriptVarType.STRING
                        ) {
                            value
                        } else {
                            value.toInt()
                        },
                    )
                val formatter = formatterCollection.getTypedFormatter(property.javaClass)
                val result = formatter?.format(property) ?: property.value
                values += result.toString()
            }
            val valuesString = values.toString()
            val length = types.length + valuesString.length
            if (length <= 75) {
                root.any("types", "[$types]")
                root.any("values", valuesString)
                return
            }
            root.list("types") {
                for (i in message.types.indices) {
                    val char = message.types[i]
                    val type =
                        ScriptVarType.entries.first { type ->
                            type.char == char
                        }
                    enum("", type)
                }
            }
            root.list("values") {
                for (i in message.types.indices) {
                    val char = message.types[i]
                    val value = message.values[i].toString()
                    val type =
                        ScriptVarType.entries.first { type ->
                            type.char == char
                        }
                    scriptVarType("", type, if (type == ScriptVarType.STRING) value else value.toInt())
                }
            }
            return
        }
        root.group("PARAMS") {
            for (i in message.types.indices) {
                val char = message.types[i]
                val value = message.values[i].toString()
                val type =
                    ScriptVarType.entries.first { type ->
                        type.char == char
                    }
                group {
                    enum("type", type)
                    scriptVarType("value", type, if (type == ScriptVarType.STRING) value else value.toInt())
                }
            }
        }
    }

    override fun setMapFlag(message: SetMapFlag) {
        if (!filters[PropertyFilter.SET_MAP_FLAG]) return omit()
        if (message.xInBuildArea == 0xFF && message.zInBuildArea == 0xFF) {
            root.any<Any>("coord", null)
        } else {
            root.coordGrid(buildAreaCoordGrid(message.xInBuildArea, message.zInBuildArea))
        }
    }

    override fun setPlayerOp(message: SetPlayerOp) {
        if (!filters[PropertyFilter.SET_PLAYER_OP]) return omit()
        root.int("id", message.id)
        val op = message.op
        if (op != null) {
            root.string("op", op)
        } else {
            root.any<Any>("op", null)
        }
        root.filteredBoolean("priority", message.priority)
    }

    override fun triggerOnDialogAbort(message: TriggerOnDialogAbort) {
        if (!filters[PropertyFilter.TRIGGER_ONDIALOGABORT]) return omit()
    }

    override fun updateRunEnergy(message: UpdateRunEnergy) {
        if (!filters[PropertyFilter.UPDATE_RUNENERGY]) return omit()
        root.int("energy", message.runenergy)
    }

    override fun updateRunWeight(message: UpdateRunWeight) {
        if (!filters[PropertyFilter.UPDATE_RUNWEIGHT]) return omit()
        root.formattedInt("weight", message.runweight, GRAM_NUMBER_FORMAT)
    }

    private enum class Stat(
        val id: Int,
    ) : NamedEnum {
        ATTACK(0),
        DEFENCE(1),
        STRENGTH(2),
        HITPOINTS(3),
        RANGED(4),
        PRAYER(5),
        MAGIC(6),
        COOKING(7),
        WOODCUTTING(8),
        FLETCHING(9),
        FISHING(10),
        FIREMAKING(11),
        CRAFTING(12),
        SMITHING(13),
        MINING(14),
        HERBLORE(15),
        AGILITY(16),
        THIEVING(17),
        SLAYER(18),
        FARMING(19),
        RUNECRAFTING(20),
        HUNTER(21),
        CONSTRUCTION(22),
        SAILING(23),
        UNRELEASED(24),
        ;

        override val prettyName: String = name.lowercase()
    }

    override fun updateStatV2(message: UpdateStatV2) {
        if (!filters[PropertyFilter.UPDATE_STAT]) return omit()
        val oldXp = sessionState.getExperience(message.stat)
        root.namedEnum("stat", Stat.entries.first { it.id == message.stat })
        root.int("level", message.currentLevel)
        root.filteredInt("invisiblelevel", message.invisibleBoostedLevel, message.currentLevel)
        root.formattedInt("experience", message.experience - (oldXp ?: 0))
    }

    override fun updateStatV1(message: UpdateStatV1) {
        if (!filters[PropertyFilter.UPDATE_STAT]) return omit()
        val oldXp = sessionState.getExperience(message.stat)
        root.namedEnum("stat", Stat.entries.first { it.id == message.stat })
        root.int("level", message.currentLevel)
        root.formattedInt("experience", message.experience - (oldXp ?: 0))
    }

    override fun updateStockMarketSlot(message: UpdateStockMarketSlot) {
        if (!filters[PropertyFilter.UPDATE_STOCKMARKET_SLOT]) return omit()
        root.int("slot", message.slot)
        when (val update = message.update) {
            UpdateStockMarketSlot.ResetStockMarketSlot -> {}
            is UpdateStockMarketSlot.SetStockMarketSlot -> {
                root.int("status", update.status)
                root.scriptVarType("id", ScriptVarType.OBJ, update.obj)
                root.formattedInt("price", update.price)
                root.formattedInt("count", update.count)
                root.formattedInt("completedcount", update.completedCount)
                root.formattedInt("completedgold", update.completedGold)
            }
        }
    }

    override fun updateTradingPost(message: UpdateTradingPost) {
        if (!filters[PropertyFilter.DEPRECATED_SERVER]) return omit()
        when (val update = message.updateType) {
            UpdateTradingPost.ResetTradingPost -> {}
            is UpdateTradingPost.SetTradingPostOfferList -> {
                root.long("age", update.age)
                root.scriptVarType("id", ScriptVarType.OBJ, update.obj)
                root.boolean("status", update.status)
                root.group("OFFERS") {
                    for (offer in update.offers) {
                        group {
                            string("name", offer.name)
                            string("previousname", offer.previousName)
                            int("world", offer.world)
                            long("time", offer.time)
                            formattedInt("price", offer.price)
                            formattedInt("count", offer.count)
                        }
                    }
                }
            }
        }
    }

    override fun friendListLoaded(message: FriendListLoaded) {
        if (!filters[PropertyFilter.FRIENDLIST_LOADED]) return omit()
    }

    override fun messagePrivate(message: MessagePrivate) {
        if (!filters[PropertyFilter.MESSAGE_PRIVATE]) return omit()
        root.string("from", message.sender)
        root.int("world", message.worldId)
        root.int("mescount", message.worldMessageCounter)
        root.filteredInt("chatcrown", message.chatCrownType, 0)
        root.string("message", message.message)
    }

    override fun messagePrivateEcho(message: MessagePrivateEcho) {
        if (!filters[PropertyFilter.MESSAGE_PRIVATE]) return omit()
        root.string("to", message.recipient)
        root.string("message", message.message)
    }

    private fun formatPlatform(id: Int): String {
        return when (id) {
            0 -> "RuneScape"
            4 -> "RuneScape Lobby"
            8 -> "Old School RuneScape"
            else -> id.toString()
        }
    }

    override fun updateFriendList(message: UpdateFriendList) {
        if (!filters[PropertyFilter.UPDATE_FRIENDLIST]) return omit()
        for (friend in message.friends) {
            when (friend) {
                is UpdateFriendList.OfflineFriend -> {
                    root.group("OFFLINE_FRIEND") {
                        string("name", friend.name)
                        filteredString("previousname", friend.previousName, "")
                        filteredInt("rank", friend.rank, 0)
                        filteredInt("properties", friend.properties, 0)
                        filteredString("notes", friend.notes, "")
                        filteredBoolean("added", friend.added)
                    }
                }
                is UpdateFriendList.OnlineFriend -> {
                    root.group("ONLINE_FRIEND") {
                        string("name", friend.name)
                        filteredString("previousname", friend.previousName, "")
                        int("world", friend.worldId)
                        filteredInt("rank", friend.rank, 0)
                        filteredInt("properties", friend.properties, 0)
                        filteredString("notes", friend.notes, "")
                        string("worldname", friend.worldName)
                        string("platform", formatPlatform(friend.platform))
                        filteredInt("worldflags", friend.worldFlags, 0)
                        filteredBoolean("added", friend.added)
                    }
                }
            }
        }
    }

    override fun updateIgnoreList(message: UpdateIgnoreList) {
        if (!filters[PropertyFilter.UPDATE_IGNORELIST]) return omit()
        for (ignore in message.ignores) {
            when (ignore) {
                is UpdateIgnoreList.AddedIgnoredEntry -> {
                    root.group("ADDED_IGNORE") {
                        string("name", ignore.name)
                        filteredString("previousname", ignore.previousName, "")
                        filteredString("notes", ignore.note, "")
                        filteredBoolean("added", ignore.added)
                    }
                }
                is UpdateIgnoreList.RemovedIgnoredEntry -> {
                    root.group("REMOVED_IGNORE") {
                        string("name", ignore.name)
                    }
                }
            }
        }
    }

    override fun midiJingle(message: MidiJingle) {
        if (!filters[PropertyFilter.MIDI_JINGLE]) return omit()
        root.scriptVarType("id", ScriptVarType.JINGLE, message.id)
        if (message.lengthInMillis != 0) {
            root.formattedInt("length", message.lengthInMillis, MS_NUMBER_FORMAT)
        }
    }

    override fun midiSongV2(message: MidiSongV2) {
        if (!filters[PropertyFilter.MIDI_SONG]) return omit()
        root.scriptVarType("id", ScriptVarType.MIDI, message.id)
        root.int("fadeoutdelay", message.fadeOutDelay)
        root.int("fadeoutspeed", message.fadeOutSpeed)
        root.int("fadeindelay", message.fadeInDelay)
        root.int("fadeinspeed", message.fadeInSpeed)
    }

    override fun midiSongV1(message: MidiSongV1) {
        if (!filters[PropertyFilter.MIDI_SONG]) return omit()
        root.scriptVarType("id", ScriptVarType.MIDI, message.id)
    }

    override fun midiSongStop(message: MidiSongStop) {
        if (!filters[PropertyFilter.MIDI_SONG_STOP]) return omit()
        root.int("fadeoutdelay", message.fadeOutDelay)
        root.int("fadeoutspeed", message.fadeOutSpeed)
    }

    override fun midiSongWithSecondary(message: MidiSongWithSecondary) {
        if (!filters[PropertyFilter.MIDI_SONG]) return omit()
        root.scriptVarType("primaryid", ScriptVarType.MIDI, message.primaryId)
        root.scriptVarType("secondaryid", ScriptVarType.MIDI, message.secondaryId)
        root.int("fadeoutdelay", message.fadeOutDelay)
        root.int("fadeoutspeed", message.fadeOutSpeed)
        root.int("fadeindelay", message.fadeInDelay)
        root.int("fadeinspeed", message.fadeInSpeed)
    }

    override fun midiSwap(message: MidiSwap) {
        if (!filters[PropertyFilter.MIDI_SWAP]) return omit()
        root.int("fadeoutdelay", message.fadeOutDelay)
        root.int("fadeoutspeed", message.fadeOutSpeed)
        root.int("fadeindelay", message.fadeInDelay)
        root.int("fadeinspeed", message.fadeInSpeed)
    }

    override fun synthSound(message: SynthSound) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.scriptVarType("id", ScriptVarType.SYNTH, message.id.maxUShortToMinusOne())
        root.filteredInt("loops", message.loops, 1)
        root.filteredInt("delay", message.delay, 0)
    }

    override fun locAnimSpecific(message: LocAnimSpecific) {
        if (!filters[PropertyFilter.LOC_ANIM_SPECIFIC]) return omit()
        root.scriptVarType("id", ScriptVarType.SEQ, message.id)
        if (message.coordInBuildArea.invalid()) {
            root.any(
                "outofboundsbuildareacoord",
                "[zoneX=${message.coordInBuildArea.zoneX}, xInZone=${message.coordInBuildArea.xInZone}, " +
                    "zoneZ=${message.coordInBuildArea.zoneZ}, zInZone=${message.coordInBuildArea.zInZone}]",
            )
        } else {
            root.coordGrid(
                buildAreaCoordGrid(message.coordInBuildArea.xInBuildArea, message.coordInBuildArea.zInBuildArea),
            )
        }
        root.scriptVarType("shape", ScriptVarType.LOC_SHAPE, message.shape)
        root.int("rotation", message.rotation)
    }

    override fun mapAnimSpecific(message: MapAnimSpecific) {
        if (!filters[PropertyFilter.MAP_ANIM_SPECIFIC]) return omit()
        root.scriptVarType("id", ScriptVarType.SPOTANIM, message.id)
        root.filteredInt("delay", message.delay, 0)
        root.filteredInt("height", message.height, 0)
        if (message.coordInBuildArea.invalid()) {
            root.any(
                "outofboundsbuildareacoord",
                "[zoneX=${message.coordInBuildArea.zoneX}, xInZone=${message.coordInBuildArea.xInZone}, " +
                    "zoneZ=${message.coordInBuildArea.zoneZ}, zInZone=${message.coordInBuildArea.zInZone}]",
            )
        } else {
            root.coordGrid(
                buildAreaCoordGrid(message.coordInBuildArea.xInBuildArea, message.coordInBuildArea.zInBuildArea),
            )
        }
    }

    override fun npcAnimSpecific(message: NpcAnimSpecific) {
        if (!filters[PropertyFilter.NPC_ANIM_SPECIFIC]) return omit()
        root.npc(message.index)
        root.scriptVarType("anim", ScriptVarType.SEQ, message.id)
        root.filteredInt("delay", message.delay, 0)
    }

    override fun npcHeadIconSpecific(message: NpcHeadIconSpecific) {
        if (!filters[PropertyFilter.NPC_HEADICON_SPECIFIC]) return omit()
        root.npc(message.index)
        root.int("slot", message.headIconSlot)
        root.scriptVarType("graphic", ScriptVarType.GRAPHIC, message.spriteGroup)
        root.filteredInt("graphicindex", message.spriteIndex, 0)
    }

    override fun npcSpotAnimSpecific(message: NpcSpotAnimSpecific) {
        if (!filters[PropertyFilter.NPC_SPOTANIM_SPECIFIC]) return omit()
        root.npc(message.index)
        root.int("slot", message.slot)
        root.scriptVarType("spotanim", ScriptVarType.SPOTANIM, message.id)
        root.filteredInt("height", message.height, 0)
        root.filteredInt("delay", message.delay, 0)
    }

    override fun playerAnimSpecific(message: PlayerAnimSpecific) {
        if (!filters[PropertyFilter.PLAYER_ANIM_SPECIFIC]) return omit()
        root.scriptVarType("anim", ScriptVarType.SEQ, message.id)
        root.filteredInt("delay", message.delay, 0)
    }

    override fun playerSpotAnimSpecific(message: PlayerSpotAnimSpecific) {
        if (!filters[PropertyFilter.PLAYER_SPOTANIM_SPECIFIC]) return omit()
        root.player(message.index)
        root.int("slot", message.slot)
        root.scriptVarType("spotanim", ScriptVarType.SPOTANIM, message.id)
        root.filteredInt("height", message.height, 0)
        root.filteredInt("delay", message.delay, 0)
    }

    override fun projAnimSpecificV2(message: ProjAnimSpecificV2) {
        if (!filters[PropertyFilter.PROJANIM_SPECIFIC]) return omit()
        root.scriptVarType("id", ScriptVarType.SPOTANIM, message.id)
        root.int("starttime", message.startTime)
        root.int("endtime", message.endTime)
        root.int("angle", message.angle)
        root.int("progress", message.progress)
        root.int("startheight", message.startHeight)
        root.int("endheight", message.endHeight)
        root.group("SOURCE") {
            if (message.coordInBuildArea.invalid()) {
                any(
                    "outofboundsbuildareacoord",
                    "[zoneX=${message.coordInBuildArea.zoneX}, xInZone=${message.coordInBuildArea.xInZone}, " +
                        "zoneZ=${message.coordInBuildArea.zoneZ}, zInZone=${message.coordInBuildArea.zInZone}]",
                )
            } else {
                coordGrid(
                    buildAreaCoordGrid(message.coordInBuildArea.xInBuildArea, message.coordInBuildArea.zInBuildArea),
                )
            }
        }
        root.group("TARGET") {
            if (message.coordInBuildArea.invalid()) {
                any(
                    "outofboundsbuildareacoord",
                    "[zoneX=${message.coordInBuildArea.zoneX}, xInZone=${message.coordInBuildArea.xInZone}, " +
                        "zoneZ=${message.coordInBuildArea.zoneZ}, zInZone=${message.coordInBuildArea.zInZone}]",
                )
            } else {
                coordGrid(
                    buildAreaCoordGrid(
                        message.coordInBuildArea.xInBuildArea + message.deltaX,
                        message.coordInBuildArea.zInBuildArea + message.deltaZ,
                    ),
                )
            }
            val ambiguousIndex = message.targetIndex
            if (ambiguousIndex != 0) {
                if (ambiguousIndex > 0) {
                    npc(ambiguousIndex - 1)
                } else {
                    player(-ambiguousIndex - 1)
                }
            }
        }
    }

    override fun projAnimSpecificV3(message: ProjAnimSpecificV3) {
        if (!filters[PropertyFilter.PROJANIM_SPECIFIC]) return omit()
        root.scriptVarType("id", ScriptVarType.SPOTANIM, message.id)
        root.int("starttime", message.startTime)
        root.int("endtime", message.endTime)
        root.int("angle", message.angle)
        root.int("progress", message.progress)
        root.int("startheight", message.startHeight)
        root.int("endheight", message.endHeight)
        root.group("SOURCE") {
            if (message.coordInBuildArea.invalid()) {
                any(
                    "outofboundsbuildareacoord",
                    "[zoneX=${message.coordInBuildArea.zoneX}, xInZone=${message.coordInBuildArea.xInZone}, " +
                        "zoneZ=${message.coordInBuildArea.zoneZ}, zInZone=${message.coordInBuildArea.zInZone}]",
                )
            } else {
                coordGrid(
                    buildAreaCoordGrid(message.coordInBuildArea.xInBuildArea, message.coordInBuildArea.zInBuildArea),
                )
            }
            val ambiguousIndex = message.sourceIndex
            if (ambiguousIndex != 0) {
                if (ambiguousIndex > 0) {
                    npc(ambiguousIndex - 1)
                } else {
                    player(-ambiguousIndex - 1)
                }
            }
        }
        root.group("TARGET") {
            if (message.coordInBuildArea.invalid()) {
                any(
                    "outofboundsbuildareacoord",
                    "[zoneX=${message.coordInBuildArea.zoneX}, xInZone=${message.coordInBuildArea.xInZone}, " +
                        "zoneZ=${message.coordInBuildArea.zoneZ}, zInZone=${message.coordInBuildArea.zInZone}]",
                )
            } else {
                coordGrid(
                    buildAreaCoordGrid(
                        message.coordInBuildArea.xInBuildArea + message.deltaX,
                        message.coordInBuildArea.zInBuildArea + message.deltaZ,
                    ),
                )
            }
            val ambiguousIndex = message.targetIndex
            if (ambiguousIndex != 0) {
                if (ambiguousIndex > 0) {
                    npc(ambiguousIndex - 1)
                } else {
                    player(-ambiguousIndex - 1)
                }
            }
        }
    }

    private fun getImpactedVarbits(
        basevar: Int,
        oldValue: Int,
        newValue: Int,
    ): List<VarBitType> {
        return sessionState.getAssociatedVarbits(basevar).filter { type ->
            val bitcount = (type.endbit - type.startbit) + 1
            val bitmask = type.bitmask(bitcount)
            val oldVarbitValue = oldValue ushr type.startbit and bitmask
            val newVarbitValue = newValue ushr type.startbit and bitmask
            oldVarbitValue != newVarbitValue
        }
    }

    private fun remainingImpactedBits(
        oldValue: Int,
        newValue: Int,
        impactedVarbits: List<VarBitType>,
    ): Int {
        return impactedVarbits.fold(oldValue and newValue.inv()) { remaining, varbit ->
            val bitcount = (varbit.endbit - varbit.startbit) + 1
            val bitmask = varbit.bitmask(bitcount) shl varbit.startbit
            remaining and bitmask.inv()
        }
    }

    override fun varpLarge(message: VarpLarge) {
        logVarp(message.id, message.value)
    }

    override fun varpReset(message: VarpReset) {
        if (!filters[PropertyFilter.VARP_RESET]) return omit()
    }

    override fun varpSmall(message: VarpSmall) {
        logVarp(message.id, message.value)
    }

    private fun logVarp(
        id: Int,
        newValue: Int,
    ) {
        val oldValue = sessionState.getVarp(id)
        val impactedVarbits = getImpactedVarbits(id, oldValue, newValue)
        root.varp("id", id)
        root.int("old", oldValue)
        root.int("new", newValue)
        val varps = filters[PropertyFilter.VARP]
        val varbits = filters[PropertyFilter.VARBITS]
        // If not intereste in either, clear and stop
        if (!varps && !varbits) {
            return omit()
        }
        // If only wanting varps, stop here.
        if (varps && !varbits) {
            return
        }
        val omitVarpsForVarbits = settings[Setting.HIDE_UNNECESSARY_VARPS]
        val remainingBits = remainingImpactedBits(oldValue, newValue, impactedVarbits)
        // If only interested in varbits and varbits exist, create a varbit root
        val printAsVarbits = impactedVarbits.isNotEmpty() && omitVarpsForVarbits && remainingBits == 0
        if (!varps || (settings[Setting.HIDE_SAME_VALUE_VARPS] && oldValue == newValue) || printAsVarbits) {
            omit()
        }
        if (impactedVarbits.isNotEmpty()) {
            for (varbit in impactedVarbits) {
                val bitcount = (varbit.endbit - varbit.startbit) + 1
                val bitmask = varbit.bitmask(bitcount)
                val oldVarbitValue = oldValue ushr varbit.startbit and bitmask
                val newVarbitValue = newValue ushr varbit.startbit and bitmask
                if (printAsVarbits) {
                    sessionState.createFakeServerRoot("VARBIT")
                    root.varbit("id", varbit.id)
                    root.int("old", oldVarbitValue)
                    root.int("new", newVarbitValue)
                } else {
                    root.group("VARBIT") {
                        varbit("id", varbit.id)
                        int("old", oldVarbitValue)
                        int("new", newVarbitValue)
                    }
                }
            }
        }
    }

    override fun varpSync(message: VarpSync) {
        if (!filters[PropertyFilter.VARP_SYNC]) return omit()
    }

    override fun clearEntities(message: ClearEntities) {
        if (!filters[PropertyFilter.CLEAR_ENTITIES]) return omit()
    }

    override fun setActiveWorld(message: SetActiveWorld) {
        if (!filters[PropertyFilter.SET_ACTIVE_WORLD]) return omit()
        when (val type = message.worldType) {
            is SetActiveWorld.DynamicWorldType -> {
                root.worldentity(type.index)
                root.int("level", type.activeLevel)
            }
            is SetActiveWorld.RootWorldType -> {
                root.worldentity(-1)
                root.int("level", type.activeLevel)
            }
        }
    }

    override fun updateZoneFullFollows(message: UpdateZoneFullFollows) {
        if (!filters[PropertyFilter.ZONE_HEADER]) return omit()
        root.coordGrid(buildAreaCoordGrid(message.zoneX, message.zoneZ, message.level))
    }

    override fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed) {
        root.coordGrid(buildAreaCoordGrid(message.zoneX, message.zoneZ, message.level))
        val includeZoneHeader = filters[PropertyFilter.ZONE_HEADER]
        if (!includeZoneHeader) {
            omit()
        }
        if (message.packets.isEmpty()) {
            return
        }
        if (includeZoneHeader) {
            createChildZoneProts(root, message.packets)
        } else {
            createFakeZoneProts(message.packets)
        }
    }

    private fun createFakeZoneProts(packets: List<IncomingZoneProt>) {
        for (event in packets) {
            when (event) {
                is LocAddChange -> {
                    if (!filters[PropertyFilter.LOC_ADD_CHANGE]) continue
                    val root = sessionState.createFakeServerRoot("LOC_ADD_CHANGE")
                    root.buildLocAddChange(event)
                }
                is LocAnim -> {
                    if (!filters[PropertyFilter.LOC_ANIM]) continue
                    val root = sessionState.createFakeServerRoot("LOC_ANIM")
                    root.buildLocAnim(event)
                }
                is LocDel -> {
                    if (!filters[PropertyFilter.LOC_DEL]) continue
                    val root = sessionState.createFakeServerRoot("LOC_DEL")
                    root.buildLocDel(event)
                }
                is LocMerge -> {
                    if (!filters[PropertyFilter.LOC_MERGE]) continue
                    val root = sessionState.createFakeServerRoot("LOC_MERGE")
                    root.buildLocMerge(event)
                }
                is MapAnim -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    val root = sessionState.createFakeServerRoot("MAP_ANIM")
                    root.buildMapAnim(event)
                }
                is MapProjAnim -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    val root = sessionState.createFakeServerRoot("MAP_PROJANIM")
                    root.buildMapProjAnim(event)
                }
                is ObjAdd -> {
                    if (!filters[PropertyFilter.OBJ_ADD]) continue
                    val root = sessionState.createFakeServerRoot("OBJ_ADD")
                    root.buildObjAdd(event)
                }
                is ObjCount -> {
                    if (!filters[PropertyFilter.OBJ_COUNT]) continue
                    val root = sessionState.createFakeServerRoot("OBJ_COUNT")
                    root.buildObjCount(event)
                }
                is ObjDel -> {
                    if (!filters[PropertyFilter.OBJ_DEL]) continue
                    val root = sessionState.createFakeServerRoot("OBJ_DEL")
                    root.buildObjDel(event)
                }
                is ObjEnabledOps -> {
                    if (!filters[PropertyFilter.OBJ_ENABLED_OPS]) continue
                    val root = sessionState.createFakeServerRoot("OBJ_ENABLED_OPS")
                    root.buildObjEnabledOps(event)
                }
                is ObjCustomise -> {
                    if (!filters[PropertyFilter.OBJ_CUSTOMISE]) continue
                    val root = sessionState.createFakeServerRoot("OBJ_ENABLED_OPS")
                    root.buildObjCustomise(event)
                }
                is ObjUncustomise -> {
                    if (!filters[PropertyFilter.OBJ_CUSTOMISE]) continue
                    val root = sessionState.createFakeServerRoot("OBJ_ENABLED_OPS")
                    root.buildObjUncustomise(event)
                }
                is SoundArea -> {
                    if (!filters[PropertyFilter.SOUND_AREA]) continue
                    val root = sessionState.createFakeServerRoot("SOUND_AREA")
                    root.buildSoundArea(event)
                }
            }
        }
    }

    private fun createChildZoneProts(
        root: RootProperty,
        packets: List<IncomingZoneProt>,
    ) {
        root.apply {
            for (event in packets) {
                when (event) {
                    is LocAddChange -> {
                        if (!filters[PropertyFilter.LOC_ADD_CHANGE]) continue
                        group("LOC_ADD_CHANGE") {
                            buildLocAddChange(event)
                        }
                    }
                    is LocAnim -> {
                        if (!filters[PropertyFilter.LOC_ANIM]) continue
                        group("LOC_ANIM") {
                            buildLocAnim(event)
                        }
                    }
                    is LocDel -> {
                        if (!filters[PropertyFilter.LOC_DEL]) continue
                        group("LOC_DEL") {
                            buildLocDel(event)
                        }
                    }
                    is LocMerge -> {
                        if (!filters[PropertyFilter.LOC_MERGE]) continue
                        group("LOC_MERGE") {
                            buildLocMerge(event)
                        }
                    }
                    is MapAnim -> {
                        if (!filters[PropertyFilter.MAP_ANIM]) continue
                        group("MAP_ANIM") {
                            buildMapAnim(event)
                        }
                    }
                    is MapProjAnim -> {
                        if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                        group("MAP_PROJANIM") {
                            buildMapProjAnim(event)
                        }
                    }
                    is ObjAdd -> {
                        if (!filters[PropertyFilter.OBJ_ADD]) continue
                        group("OBJ_ADD") {
                            buildObjAdd(event)
                        }
                    }
                    is ObjCount -> {
                        if (!filters[PropertyFilter.OBJ_COUNT]) continue
                        group("OBJ_COUNT") {
                            buildObjCount(event)
                        }
                    }
                    is ObjDel -> {
                        if (!filters[PropertyFilter.OBJ_DEL]) continue
                        group("OBJ_DEL") {
                            buildObjDel(event)
                        }
                    }
                    is ObjEnabledOps -> {
                        if (!filters[PropertyFilter.OBJ_ENABLED_OPS]) continue
                        group("OBJ_ENABLED_OPS") {
                            buildObjEnabledOps(event)
                        }
                    }
                    is ObjCustomise -> {
                        if (!filters[PropertyFilter.OBJ_CUSTOMISE]) continue
                        group("OBJ_CUSTOMISE") {
                            buildObjCustomise(event)
                        }
                    }
                    is ObjUncustomise -> {
                        if (!filters[PropertyFilter.OBJ_CUSTOMISE]) continue
                        group("OBJ_UNCUSTOMISE") {
                            buildObjUncustomise(event)
                        }
                    }
                    is SoundArea -> {
                        if (!filters[PropertyFilter.SOUND_AREA]) continue
                        group("SOUND_AREA") {
                            buildSoundArea(event)
                        }
                    }
                }
            }
        }
    }

    override fun updateZonePartialFollows(message: UpdateZonePartialFollows) {
        if (!filters[PropertyFilter.ZONE_HEADER]) return omit()
        root.coordGrid(buildAreaCoordGrid(message.zoneX, message.zoneZ, message.level))
    }

    override fun locAddChange(message: LocAddChange) {
        if (!filters[PropertyFilter.LOC_ADD_CHANGE]) return omit()
        root.buildLocAddChange(message)
    }

    override fun locAnim(message: LocAnim) {
        if (!filters[PropertyFilter.LOC_ANIM]) return omit()
        root.buildLocAnim(message)
    }

    override fun locDel(message: LocDel) {
        if (!filters[PropertyFilter.LOC_DEL]) return omit()
        root.buildLocDel(message)
    }

    override fun locMerge(message: LocMerge) {
        if (!filters[PropertyFilter.LOC_MERGE]) return omit()
        root.buildLocMerge(message)
    }

    override fun mapAnim(message: MapAnim) {
        if (!filters[PropertyFilter.MAP_ANIM]) return omit()
        root.buildMapAnim(message)
    }

    override fun mapProjAnim(message: MapProjAnim) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildMapProjAnim(message)
    }

    override fun objAdd(message: ObjAdd) {
        if (!filters[PropertyFilter.OBJ_ADD]) return omit()
        root.buildObjAdd(message)
    }

    override fun objCount(message: ObjCount) {
        if (!filters[PropertyFilter.OBJ_COUNT]) return omit()
        root.buildObjCount(message)
    }

    override fun objDel(message: ObjDel) {
        if (!filters[PropertyFilter.OBJ_DEL]) return omit()
        root.buildObjDel(message)
    }

    override fun objEnabledOps(message: ObjEnabledOps) {
        if (!filters[PropertyFilter.OBJ_ENABLED_OPS]) return omit()
        root.buildObjEnabledOps(message)
    }

    override fun soundArea(message: SoundArea) {
        if (!filters[PropertyFilter.SOUND_AREA]) return omit()
        root.buildSoundArea(message)
    }

    private fun coordInZone(
        xInZone: Int,
        zInZone: Int,
    ): CoordGrid {
        return sessionState.getActiveWorld().relativizeZoneCoord(xInZone, zInZone)
    }

    private fun Property.buildLocAddChange(message: LocAddChange) {
        scriptVarType("id", ScriptVarType.LOC, message.id)
        coordGrid(coordInZone(message.xInZone, message.zInZone))
        scriptVarType("shape", ScriptVarType.LOC_SHAPE, message.shape)
        int("rotation", message.rotation)
    }

    private fun Property.buildLocAnim(message: LocAnim) {
        coordGrid(coordInZone(message.xInZone, message.zInZone))
        scriptVarType("shape", ScriptVarType.LOC_SHAPE, message.shape)
        int("rotation", message.rotation)
        scriptVarType("anim", ScriptVarType.SEQ, message.id)
    }

    private fun Property.buildLocDel(message: LocDel) {
        coordGrid(coordInZone(message.xInZone, message.zInZone))
        scriptVarType("shape", ScriptVarType.LOC_SHAPE, message.shape)
        int("rotation", message.rotation)
    }

    private fun Property.buildLocMerge(message: LocMerge) {
        scriptVarType("id", ScriptVarType.LOC, message.id)
        coordGrid(coordInZone(message.xInZone, message.zInZone))
        scriptVarType("shape", ScriptVarType.LOC_SHAPE, message.shape)
        int("rotation", message.rotation)
        int("start", message.start)
        int("end", message.end)
        int("minx", message.minX)
        int("maxx", message.maxX)
        int("minz", message.minZ)
        int("maxz", message.maxZ)
    }

    private fun Property.buildMapAnim(message: MapAnim) {
        scriptVarType("id", ScriptVarType.SPOTANIM, message.id)
        filteredInt("delay", message.delay, 0)
        filteredInt("height", message.height, 0)
        coordGrid(coordInZone(message.xInZone, message.zInZone))
    }

    private fun Property.buildMapProjAnim(message: MapProjAnim) {
        scriptVarType("id", ScriptVarType.SPOTANIM, message.id)
        int("starttime", message.startTime)
        int("endtime", message.endTime)
        int("angle", message.angle)
        int("progress", message.progress)
        int("startheight", message.startHeight)
        int("endheight", message.endHeight)
        group("SOURCE") {
            coordGrid(coordInZone(message.xInZone, message.zInZone))
            val ambiguousIndex = message.sourceIndex
            if (ambiguousIndex != 0) {
                if (ambiguousIndex > 0) {
                    npc(ambiguousIndex - 1)
                } else {
                    player(-ambiguousIndex - 1)
                }
            }
        }
        group("TARGET") {
            coordGrid(coordInZone(message.xInZone + message.deltaX, message.zInZone + message.deltaZ))
            val ambiguousIndex = message.targetIndex
            if (ambiguousIndex != 0) {
                if (ambiguousIndex > 0) {
                    npc(ambiguousIndex - 1)
                } else {
                    player(-ambiguousIndex - 1)
                }
            }
        }
    }

    private enum class ObjOwnership(
        override val prettyName: String,
    ) : NamedEnum {
        None("none"),
        Self("self"),
        Other("other"),
        GroupIronman("gim"),
    }

    private fun getObjOwnership(id: Int): ObjOwnership {
        return when (id) {
            0 -> ObjOwnership.None
            1 -> ObjOwnership.Self
            2 -> ObjOwnership.Other
            3 -> ObjOwnership.GroupIronman
            else -> error("Unknown obj ownership type: $id")
        }
    }

    private fun Property.buildObjAdd(message: ObjAdd) {
        scriptVarType("id", ScriptVarType.OBJ, message.id)
        formattedInt("count", message.quantity)
        filteredAny("opflags", "0b" + message.opFlags.value.toString(2), "0b11111")
        filteredInt("reveal", message.timeUntilPublic, 0)
        filteredInt("despawn", message.timeUntilDespawn, 0)
        filteredNamedEnum("ownership", getObjOwnership(message.ownershipType), ObjOwnership.None)
        boolean("neverturnpublic", message.neverBecomesPublic)
        coordGrid(coordInZone(message.xInZone, message.zInZone))
    }

    private fun Property.buildObjCount(message: ObjCount) {
        scriptVarType("id", ScriptVarType.OBJ, message.id)
        formattedInt("oldcount", message.oldQuantity)
        formattedInt("newcount", message.newQuantity)
        coordGrid(coordInZone(message.xInZone, message.zInZone))
    }

    private fun Property.buildObjDel(message: ObjDel) {
        scriptVarType("id", ScriptVarType.OBJ, message.id)
        formattedInt("count", message.quantity)
        coordGrid(coordInZone(message.xInZone, message.zInZone))
    }

    private fun Property.buildObjEnabledOps(message: ObjEnabledOps) {
        scriptVarType("id", ScriptVarType.OBJ, message.id)
        any("opflags", "0b" + message.opFlags.value.toString(2))
        coordGrid(coordInZone(message.xInZone, message.zInZone))
    }

    private fun Property.buildObjCustomise(message: ObjCustomise) {
        scriptVarType("id", ScriptVarType.OBJ, message.id.maxUShortToMinusOne())
        int("count", message.quantity)
        int("recolindex", message.recolIndex)
        int("recol", message.recol)
        int("retexindex", message.retexIndex)
        int("retex", message.retex)
        coordGrid(coordInZone(message.xInZone, message.zInZone))
    }

    private fun Property.buildObjUncustomise(message: ObjUncustomise) {
        scriptVarType("id", ScriptVarType.OBJ, message.id.maxUShortToMinusOne())
        int("count", message.quantity)
        coordGrid(coordInZone(message.xInZone, message.zInZone))
    }

    private fun Property.buildSoundArea(message: SoundArea) {
        scriptVarType("id", ScriptVarType.SYNTH, message.id.maxUShortToMinusOne())
        filteredInt("loops", message.loops, 0)
        filteredInt("delay", message.delay, 0)
        int("range", message.radius)
        filteredInt("size", message.size, 0)
        coordGrid(coordInZone(message.xInZone, message.zInZone))
    }

    override fun unknownString(message: UnknownString) {
        if (!filters[PropertyFilter.DEPRECATED_SERVER]) return omit()
        root.string("string", message.string)
    }

    override fun objCustomise(message: ObjCustomise) {
        if (!filters[PropertyFilter.OBJ_CUSTOMISE]) return omit()
        root.buildObjCustomise(message)
    }

    override fun objUncustomise(message: ObjUncustomise) {
        if (!filters[PropertyFilter.OBJ_CUSTOMISE]) return omit()
        root.buildObjUncustomise(message)
    }

    override fun setInteractionMode(message: SetInteractionMode) {
        if (!filters[PropertyFilter.SET_INTERACTION_MODE]) return omit()
        if (message.worldId == -2) {
            root.any("type", "default")
        } else {
            root.worldentity(message.worldId)
        }
        val tileInteractionMode =
            when (message.tileInteractionMode) {
                0 -> "disabled"
                1 -> "walk"
                2 -> "heading"
                else -> "unknown (id: ${message.tileInteractionMode})"
            }
        val entityInteractionMode =
            when (message.entityInteractionMode) {
                0 -> "disabled"
                1 -> "enabled"
                2 -> "examine"
                else -> "unknown (id: ${message.entityInteractionMode})"
            }
        root.any("tileinteractionmode", tileInteractionMode)
        root.any("entityinteractionmode", entityInteractionMode)
    }

    override fun resetInteractionMode(message: ResetInteractionMode) {
        if (!filters[PropertyFilter.SET_INTERACTION_MODE]) return omit()
        if (message.worldId == -2) {
            root.any("type", "default")
        } else {
            root.worldentity(message.worldId)
        }
    }

    private companion object {
        private val MS_NUMBER_FORMAT: NumberFormat = DecimalFormat("###,###,###ms")
        private val GRAM_NUMBER_FORMAT: NumberFormat = DecimalFormat("###,###,###g")
    }
}
