package net.rsprox.transcriber.base

import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.type.VarBitType
import net.rsprox.protocol.common.CoordGrid
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
import net.rsprox.protocol.game.outgoing.model.camera.CamTarget
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetOld
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
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.SetNpcUpdateOrigin
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.BaseAnimationSetExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.BodyCustomisationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.CombatLevelChangeExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.EnabledOpsExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.FaceCoordExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.HeadCustomisationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.HeadIconCustomisationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.NameChangeExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.OldSpotanimExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.TransformationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.customisation.ModelCustomisation
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.customisation.ResetCustomisation
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerUpdateType
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.AppearanceExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.ChatExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.FaceAngleExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.MoveSpeedExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.NameExtrasExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.TemporaryMoveSpeedExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExactMoveExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.FacePathingEntityExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.HitExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SayExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SequenceExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SpotanimExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.TintingExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfo
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
import net.rsprox.protocol.game.outgoing.model.map.RebuildWorldEntity
import net.rsprox.protocol.game.outgoing.model.misc.client.HideLocOps
import net.rsprox.protocol.game.outgoing.model.misc.client.HideNpcOps
import net.rsprox.protocol.game.outgoing.model.misc.client.HidePlayerOps
import net.rsprox.protocol.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.game.outgoing.model.misc.client.HiscoreReply
import net.rsprox.protocol.game.outgoing.model.misc.client.MinimapToggle
import net.rsprox.protocol.game.outgoing.model.misc.client.ReflectionChecker
import net.rsprox.protocol.game.outgoing.model.misc.client.ResetAnims
import net.rsprox.protocol.game.outgoing.model.misc.client.SendPing
import net.rsprox.protocol.game.outgoing.model.misc.client.ServerTickEnd
import net.rsprox.protocol.game.outgoing.model.misc.client.SetHeatmapEnabled
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
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStat
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatOld
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStockMarketSlot
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateTradingPost
import net.rsprox.protocol.game.outgoing.model.social.FriendListLoaded
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivate
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivateEcho
import net.rsprox.protocol.game.outgoing.model.social.UpdateFriendList
import net.rsprox.protocol.game.outgoing.model.social.UpdateIgnoreList
import net.rsprox.protocol.game.outgoing.model.sound.MidiJingle
import net.rsprox.protocol.game.outgoing.model.sound.MidiSong
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongOld
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongStop
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
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecific
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
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjDel
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjEnabledOps
import net.rsprox.protocol.game.outgoing.model.zone.payload.SoundArea
import net.rsprox.protocol.reflection.ReflectionCheck
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.ScriptVarType
import net.rsprox.transcriber.ServerPacketTranscriber
import net.rsprox.transcriber.coord
import net.rsprox.transcriber.format
import net.rsprox.transcriber.indent
import net.rsprox.transcriber.properties.Property
import net.rsprox.transcriber.properties.PropertyBuilder
import net.rsprox.transcriber.properties.properties
import net.rsprox.transcriber.quote
import net.rsprox.transcriber.state.Npc
import net.rsprox.transcriber.state.Player
import net.rsprox.transcriber.state.StateTracker
import net.rsprox.transcriber.state.World
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@Suppress("DuplicatedCode", "SameParameterValue", "MemberVisibilityCanBePrivate", "SpellCheckingInspection")
public open class BaseServerPacketTranscriber(
    private val formatter: BaseMessageFormatter,
    private val container: MessageConsumerContainer,
    private val stateTracker: StateTracker,
    private val cache: Cache,
) : ServerPacketTranscriber {
    private fun format(
        indentation: Int,
        name: String,
        properties: List<Property>,
    ): String {
        return formatter.format(
            clientPacket = false,
            name = name,
            properties = properties,
            indentation = indentation,
        )
    }

    private fun format(properties: List<Property>): String {
        return formatter.format(
            clientPacket = false,
            name = stateTracker.currentProt.toString(),
            properties = properties,
            indentation = 1,
        )
    }

    private fun format(
        indentation: Int,
        name: String,
        builderAction: PropertyBuilder.() -> Unit = {},
    ): String {
        val properties = properties(builderAction)
        return formatter.format(
            clientPacket = false,
            name = name,
            properties = properties,
            indentation = indentation,
        )
    }

    private fun publish(builderAction: PropertyBuilder.() -> Unit) {
        container.publish(format(properties(builderAction)))
    }

    private fun npc(
        index: Int,
        specify: Boolean = false,
    ): String {
        val prefix = if (specify) "npcindex" else "index"
        val npc = stateTracker.getActiveWorld().getNpcOrNull(index)
        return if (npc == null) {
            "($prefix=$index)"
        } else {
            formatNpc(prefix, npc.index, npc.name?.quote(), formatter.coord(npc.coord))
        }
    }

    private fun formatNpc(
        prefix: String,
        index: Int,
        name: String?,
        coord: String,
    ): String {
        val builder = StringBuilder()
        builder
            .append('(')
            .append(prefix)
            .append('=')
            .append(index)
            .append(", ")
        if (name != null) {
            builder.append("name=").append(name).append(", ")
        }
        builder.append("coord=").append(coord).append(')')
        return builder.toString()
    }

    private fun player(
        index: Int,
        specify: Boolean = false,
    ): String {
        val prefix = if (specify) "playerindex" else "index"
        val tracked = stateTracker.getPlayerOrNull(index)
        return if (tracked == null) {
            "($prefix=$index)"
        } else {
            "($prefix=$index, name=${tracked.name.quote()}, coord=${formatter.coord(tracked.coord)})"
        }
    }

    private fun worldentity(index: Int): String {
        val entity = stateTracker.getWorldOrNull(index)
        return if (entity == null) {
            "(index=$index)"
        } else {
            "(index=$index, sizeX=${entity.sizeX}, " +
                "sizeZ=${entity.sizeZ}, angle=${entity.angle}, " +
                "coord=${formatter.coord(entity.coord)})"
        }
    }

    public fun loc(
        id: Int,
        coordGrid: CoordGrid,
        shape: Int,
        rotation: Int,
    ): String {
        val builder = StringBuilder()
        builder.append('(')
        builder.append("id=").append(formatter.type(ScriptVarType.LOC, id)).append(", ")
        builder.append("coord=").append(formatter.coord(coordGrid)).append(", ")
        builder.append("shape=").append(formatter.type(ScriptVarType.LOC_SHAPE, shape)).append(", ")
        builder.append("rotation=").append(rotation).append(')')
        return builder.toString()
    }

    public fun loc(
        coordGrid: CoordGrid,
        shape: Int,
        rotation: Int,
    ): String {
        val builder = StringBuilder()
        builder.append('(')
        builder.append("coord=").append(formatter.coord(coordGrid)).append(", ")
        builder.append("shape=").append(formatter.type(ScriptVarType.LOC_SHAPE, shape)).append(", ")
        builder.append("rotation=").append(rotation).append(')')
        return builder.toString()
    }

    private fun formatEpochTimeMinute(num: Int): String {
        val epochTimeMillis = TimeUnit.MINUTES.toMillis(num.toLong())
        return SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(epochTimeMillis))
    }

    private fun actor(ambiguousIndex: Int): List<Property> {
        return properties {
            if (ambiguousIndex == -1 || ambiguousIndex == 0xFFFFFF) {
                property("index", -1)
                return@properties
            }
            if (ambiguousIndex > 0xFFFF) {
                val index = ambiguousIndex - 0xFFFF
                property("playerindex", index)
                val name = stateTracker.getLastKnownPlayerName(index)
                if (name != null) {
                    property("player", name.quote())
                }
            } else {
                property("npcindex", ambiguousIndex)
            }
        }
    }

    private fun formatActor(ambiguousIndex: Int): String {
        if (ambiguousIndex == -1 || ambiguousIndex == 0xFFFFFF) {
            return "(index=-1)"
        }
        return if (ambiguousIndex > 0xFFFF) {
            val index = ambiguousIndex - 0xFFFF
            player(index, specify = true)
        } else {
            npc(ambiguousIndex, specify = true)
        }
    }

    private fun buildAreaCoord(
        xInBuildArea: Int,
        zInBuildArea: Int,
        level: Int = -1,
    ): String {
        return formatter.coord(buildAreaCoordGrid(xInBuildArea, zInBuildArea, level))
    }

    private fun buildAreaCoordGrid(
        xInBuildArea: Int,
        zInBuildArea: Int,
        level: Int = -1,
    ): CoordGrid {
        return stateTracker
            .getActiveWorld()
            .relativizeBuildAreaCoord(
                xInBuildArea,
                zInBuildArea,
                if (level == -1) stateTracker.level() else level,
            )
    }

    override fun camLookAt(message: CamLookAt) {
        publish {
            property("coord", buildAreaCoord(message.destinationXInBuildArea, message.destinationZInBuildArea))
            property("height", message.height)
            property("rate", message.speed)
            property("rate2", message.acceleration)
        }
    }

    override fun camLookAtEasedCoord(message: CamLookAtEasedCoord) {
        publish {
            property("coord", buildAreaCoord(message.destinationXInBuildArea, message.destinationZInBuildArea))
            property("height", message.height)
            property("cycles", message.duration)
            property("easing", message.function.prettyName().quote())
        }
    }

    override fun camMode(message: CamMode) {
        publish {
            property("mode", message.mode)
        }
    }

    override fun camMoveTo(message: CamMoveTo) {
        publish {
            property("coord", buildAreaCoord(message.destinationXInBuildArea, message.destinationZInBuildArea))
            property("height", message.height)
            property("rate", message.speed)
            property("rate2", message.acceleration)
        }
    }

    override fun camMoveToArc(message: CamMoveToArc) {
        publish {
            property("coord", buildAreaCoord(message.destinationXInBuildArea, message.destinationZInBuildArea))
            property("height", message.height)
            property("tertiaryCoord", buildAreaCoord(message.centerXInBuildArea, message.centerZInBuildArea))
            property("cycles", message.duration)
            property("ignoreTerrain", message.maintainFixedAltitude)
            property("easing", message.function.prettyName().quote())
        }
    }

    override fun camMoveToCycles(message: CamMoveToCycles) {
        publish {
            property("coord", buildAreaCoord(message.destinationXInBuildArea, message.destinationZInBuildArea))
            property("height", message.height)
            property("cycles", message.duration)
            property("ignoreTerrain", message.maintainFixedAltitude)
            property("easing", message.function.prettyName().quote())
        }
    }

    override fun camReset(message: CamReset) {
        publishProt()
    }

    override fun camRotateBy(message: CamRotateBy) {
        publish {
            property("pitch", message.xAngle)
            property("yaw", message.yAngle)
            property("cycles", message.duration)
            property("easing", message.function.prettyName().quote())
        }
    }

    override fun camRotateTo(message: CamRotateTo) {
        publish {
            property("pitch", message.xAngle)
            property("yaw", message.yAngle)
            property("cycles", message.duration)
            property("easing", message.function.prettyName().quote())
        }
    }

    override fun camShake(message: CamShake) {
        publish {
            property("axis", message.type)
            property("random", message.randomAmount)
            property("amplitude", message.sineAmount)
            property("rate", message.sineFrequency)
        }
    }

    override fun camSmoothReset(message: CamSmoothReset) {
        publish {
            property("moveConstantSpeed", message.cameraMoveConstantSpeed)
            property("moveProportionalSpeed", message.cameraMoveProportionalSpeed)
            property("lookConstantSpeed", message.cameraLookConstantSpeed)
            property("lookProportionalSpeed", message.cameraLookProportionalSpeed)
        }
    }

    override fun camTarget(message: CamTarget) {
        publish {
            when (val type = message.type) {
                is CamTarget.NpcCamTarget -> {
                    property("npc", npc(type.index))
                }
                is CamTarget.PlayerCamTarget -> {
                    property("player", player(type.index))
                }
                is CamTarget.WorldEntityTarget -> {
                    property("worldentity", worldentity(type.index))
                    if (type.cameraLockedPlayerIndex != -1) {
                        property("cameraLockedPlayerIndex", player(type.index))
                    }
                }
            }
        }
    }

    override fun camTargetOld(message: CamTargetOld) {
        publish {
            when (val type = message.type) {
                is CamTargetOld.NpcCamTarget -> {
                    property("npc", npc(type.index))
                }
                is CamTargetOld.PlayerCamTarget -> {
                    property("player", player(type.index))
                }
                is CamTargetOld.WorldEntityTarget -> {
                    property("worldentity", worldentity(type.index))
                }
            }
        }
    }

    override fun oculusSync(message: OculusSync) {
        publish {
            property("value", message.value)
        }
    }

    override fun clanChannelDelta(message: ClanChannelDelta) {
        publish {
            property("clanType", message.clanType)
            property("clanHash", message.clanHash.format())
            property("updateNum", message.updateNum.format())
        }
        for (event in message.events) {
            when (event) {
                is ClanChannelDelta.ClanChannelDeltaAddUserEvent -> {
                    container.publish(
                        format(2, "AddUser") {
                            property("name", event.name.quote())
                            property("world", event.world)
                            property("rank", event.rank)
                        },
                    )
                }
                is ClanChannelDelta.ClanChannelDeltaDeleteUserEvent -> {
                    container.publish(
                        format(2, "DelUser") {
                            property("memberIndex", event.index)
                        },
                    )
                }
                is ClanChannelDelta.ClanChannelDeltaUpdateBaseSettingsEvent -> {
                    container.publish(
                        format(2, "UpdateBaseSettings") {
                            filteredProperty("clanName", event.clanName?.quote()) { it != null }
                            property("talkRank", event.talkRank)
                            property("kickRank", event.kickRank)
                        },
                    )
                }
                is ClanChannelDelta.ClanChannelDeltaUpdateUserDetailsEvent -> {
                    container.publish(
                        format(2, "UpdateUserDetails") {
                            property("memberIndex", event.index)
                            property("name", event.name.quote())
                            property("rank", event.rank)
                            property("world", event.world)
                        },
                    )
                }
                is ClanChannelDelta.ClanChannelDeltaUpdateUserDetailsV2Event -> {
                    container.publish(
                        format(2, "UpdateUserDetailsV2") {
                            property("memberIndex", event.index)
                            property("name", event.name.quote())
                            property("rank", event.rank)
                            property("world", event.world)
                        },
                    )
                }
            }
        }
    }

    override fun clanChannelFull(message: ClanChannelFull) {
        publish {
            property("clanType", message.clanType)
            property(
                "updateType",
                if (message.update == ClanChannelFull.ClanChannelFullLeaveUpdate) {
                    "Leave"
                } else {
                    "Join"
                },
            )
        }
        when (val update = message.update) {
            is ClanChannelFull.ClanChannelFullJoinUpdate -> {
                container.publish(
                    format(2, "Details") {
                        property("flags", update.flags)
                        property("version", update.version)
                        property("clanHash", update.clanHash.format())
                        property("updateNum", update.updateNum.format())
                        property("clanName", update.clanName.quote())
                        property("discardedBoolean", update.discardedBoolean)
                        property("kickRank", update.kickRank)
                        property("talkRank", update.talkRank)
                    },
                )
                container.publish(format(2, "Members"))
                for (member in update.members) {
                    container.publish(
                        format(3, "Member") {
                            property("name", member.name.quote())
                            property("rank", member.rank)
                            property("world", member.world)
                            property("discardedBoolean", member.discardedBoolean)
                        },
                    )
                }
            }
            ClanChannelFull.ClanChannelFullLeaveUpdate -> {
            }
        }
    }

    override fun clanSettingsDelta(message: ClanSettingsDelta) {
        publish {
            property("clanType", message.clanType)
            property("owner", message.owner.format())
            property("updateNum", message.updateNum)
        }
        for (update in message.updates) {
            when (update) {
                is ClanSettingsDelta.ClanSettingsDeltaSetClanOwnerUpdate -> {
                    container.publish(
                        format(2, "SetClanOwner") {
                            property("memberIndex", update.index)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaAddBannedUpdate -> {
                    container.publish(
                        format(2, "AddBanned") {
                            filteredProperty("hash", update.hash) { it != 0L }
                            filteredProperty("name", update.name?.quote()) { it != null }
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaAddMemberV1Update -> {
                    container.publish(
                        format(2, "AddMemberV1") {
                            filteredProperty("hash", update.hash) { it != 0L }
                            filteredProperty("name", update.name?.quote()) { it != null }
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaAddMemberV2Update -> {
                    container.publish(
                        format(2, "AddMemberV2") {
                            filteredProperty("hash", update.hash) { it != 0L }
                            filteredProperty("name", update.name?.quote()) { it != null }
                            property("joinRuneDay", update.joinRuneDay)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaBaseSettingsUpdate -> {
                    container.publish(
                        format(2, "BaseSettings") {
                            property("allowUnaffined", update.allowUnaffined)
                            property("talkRank", update.talkRank)
                            property("kickRank", update.kickRank)
                            property("lootshareRank", update.lootshareRank)
                            property("coinshareRank", update.coinshareRank)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaDeleteBannedUpdate -> {
                    container.publish(
                        format(2, "DeleteBanned") {
                            property("memberIndex", update.index)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaDeleteMemberUpdate -> {
                    container.publish(
                        format(2, "DeleteMember") {
                            property("memberIndex", update.index)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetClanNameUpdate -> {
                    container.publish(
                        format(2, "SetClanName") {
                            property("name", update.clanName.quote())
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetIntSettingUpdate -> {
                    container.publish(
                        format(2, "SetIntSetting") {
                            property("setting", update.setting)
                            property("value", update.value)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetLongSettingUpdate -> {
                    container.publish(
                        format(2, "SetLongSetting") {
                            property("setting", update.setting)
                            property("value", update.value)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetMemberExtraInfoUpdate -> {
                    container.publish(
                        format(2, "SetMemberExtraInfo") {
                            property("memberIndex", update.index)
                            property("value", update.value)
                            property("startBit", update.startBit)
                            property("endBit", update.endBit)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetMemberMutedUpdate -> {
                    container.publish(
                        format(2, "SetMemberMuted") {
                            property("memberIndex", update.index)
                            property("muted", update.muted)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetMemberRankUpdate -> {
                    container.publish(
                        format(2, "SetMemberRank") {
                            property("memberIndex", update.index)
                            property("rank", update.rank)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetStringSettingUpdate -> {
                    container.publish(
                        format(2, "SetStringSetting") {
                            property("setting", update.setting)
                            property("value", update.value)
                        },
                    )
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetVarbitSettingUpdate -> {
                    container.publish(
                        format(2, "SetVarbitSetting") {
                            property("setting", update.setting)
                            property("value", update.value)
                            property("startBit", update.startBit)
                            property("endBit", update.endBit)
                        },
                    )
                }
            }
        }
    }

    override fun clanSettingsFull(message: ClanSettingsFull) {
        publish {
            property("clanType", message.clanType)
            property(
                "updateType",
                if (message.update == ClanSettingsFull.ClanSettingsFullLeaveUpdate) {
                    "Leave"
                } else {
                    "Join"
                },
            )
        }
        when (val update = message.update) {
            is ClanSettingsFull.ClanSettingsFullJoinUpdate -> {
                container.publish(
                    format(2, "Details") {
                        property("flags", update.flags)
                        property("updateNum", update.updateNum.format())
                        property("creationTime", formatEpochTimeMinute(update.creationTime).quote())
                        property("clanName", update.clanName.quote())
                        property("allowUnaffined", update.allowUnaffined)
                        property("talkRank", update.talkRank)
                        property("kickRank", update.kickRank)
                        filteredProperty("lootshareRank", update.lootshareRank) { it != 0 }
                        filteredProperty("coinshareRank", update.coinshareRank) { it != 0 }
                    },
                )
                if (update.affinedMembers.isNotEmpty()) {
                    container.publish(format(2, "Affined Members"))
                    for (member in update.affinedMembers) {
                        container.publish(
                            format(3, "Member") {
                                filteredProperty("hash", member.hash) { it != 0L }
                                filteredProperty("name", member.name?.quote()) { it != null }
                                property("rank", member.rank)
                                property("extraInfo", member.extraInfo)
                                property("joinRuneDay", member.joinRuneDay)
                                property("muted", member.muted)
                            },
                        )
                    }
                }

                if (update.bannedMembers.isNotEmpty()) {
                    container.publish(format(2, "Banned Members"))
                    for (member in update.bannedMembers) {
                        container.publish(
                            format(3, "Member") {
                                filteredProperty("hash", member.hash) { it != 0L }
                                filteredProperty("name", member.name?.quote()) { it != null }
                            },
                        )
                    }
                }

                if (update.settings.isNotEmpty()) {
                    container.publish(format(2, "Settings"))
                    for (setting in update.settings) {
                        when (setting) {
                            is ClanSettingsFull.IntClanSetting -> {
                                container.publish(
                                    format(3, "IntSetting") {
                                        property("id", setting.id)
                                        property("value", setting.value)
                                    },
                                )
                            }
                            is ClanSettingsFull.LongClanSetting -> {
                                container.publish(
                                    format(3, "LongSetting") {
                                        property("id", setting.id)
                                        property("value", setting.value)
                                    },
                                )
                            }
                            is ClanSettingsFull.StringClanSetting -> {
                                container.publish(
                                    format(3, "StringSetting") {
                                        property("id", setting.id)
                                        property("value", setting.value)
                                    },
                                )
                            }
                        }
                    }
                }
            }
            ClanSettingsFull.ClanSettingsFullLeaveUpdate -> {
            }
        }
    }

    override fun messageClanChannel(message: MessageClanChannel) {
        publish {
            property("clanType", message.clanType)
            property("name", message.name.quote())
            property("worldId", message.worldId)
            property("worldMessageCounter", message.worldMessageCounter)
            filteredProperty("chatCrownType", message.chatCrownType) { it != 0 }
            property("message", message.message.quote())
        }
    }

    override fun messageClanChannelSystem(message: MessageClanChannelSystem) {
        publish {
            property("clanType", message.clanType)
            property("worldId", message.worldId)
            property("worldMessageCounter", message.worldMessageCounter)
            property("message", message.message.quote())
        }
    }

    override fun varClan(message: VarClan) {
        publish {
            property("id", message.id)
            when (val value = message.value) {
                is VarClan.UnknownVarClanData -> {
                    property("unknownValue", value.data.contentToString())
                }
                is VarClan.VarClanIntData -> {
                    property("intValue", value.value)
                }
                is VarClan.VarClanLongData -> {
                    property("longValue", value.value)
                }
                is VarClan.VarClanStringData -> {
                    property("stringValue", value.value.quote())
                }
            }
        }
    }

    override fun varClanDisable(message: VarClanDisable) {
        publishProt()
    }

    override fun varClanEnable(message: VarClanEnable) {
        publishProt()
    }

    override fun messageFriendChannel(message: MessageFriendChannel) {
        publish {
            property("sender", message.sender.quote())
            property("channelName", message.channelName.quote())
            property("worldId", message.worldId)
            property("worldMessageCounter", message.worldMessageCounter)
            property("chatCrownType", message.chatCrownType)
            property("message", message.message.quote())
        }
    }

    override fun updateFriendChatChannelFullV1(message: UpdateFriendChatChannelFullV1) {
        publish {
            when (val update = message.updateType) {
                is UpdateFriendChatChannelFullV1.JoinUpdate -> {
                    property("type", "Join")
                    property("channelOwner", update.channelOwner.quote())
                    property("channelName", update.channelName.quote())
                    property("kickRank", update.kickRank)
                }
                UpdateFriendChatChannelFullV1.LeaveUpdate -> {
                    property("type", "Leave")
                }
            }
        }
        when (val update = message.updateType) {
            is UpdateFriendChatChannelFullV1.JoinUpdate -> {
                for (entry in update.entries) {
                    container.publish(
                        format(2, "Member") {
                            property("name", entry.name.quote())
                            property("worldId", entry.worldId)
                            property("worldName", entry.worldName)
                            property("rank", entry.rank)
                        },
                    )
                }
            }
            UpdateFriendChatChannelFullV1.LeaveUpdate -> {
            }
        }
    }

    override fun updateFriendChatChannelFullV2(message: UpdateFriendChatChannelFullV2) {
        publish {
            when (val update = message.updateType) {
                is UpdateFriendChatChannelFullV2.JoinUpdate -> {
                    property("type", "Join")
                    property("channelOwner", update.channelOwner.quote())
                    property("channelName", update.channelName.quote())
                    property("kickRank", update.kickRank)
                }
                UpdateFriendChatChannelFullV2.LeaveUpdate -> {
                    property("type", "Leave")
                }
            }
        }
        when (val update = message.updateType) {
            is UpdateFriendChatChannelFullV2.JoinUpdate -> {
                for (entry in update.entries) {
                    container.publish(
                        format(2, "Member") {
                            property("name", entry.name.quote())
                            property("worldId", entry.worldId)
                            property("worldName", entry.worldName)
                            property("rank", entry.rank)
                        },
                    )
                }
            }
            UpdateFriendChatChannelFullV2.LeaveUpdate -> {
            }
        }
    }

    override fun updateFriendChatChannelSingleUser(message: UpdateFriendChatChannelSingleUser) {
        publish {
            when (val user = message.user) {
                is UpdateFriendChatChannelSingleUser.AddedFriendChatUser -> {
                    property("type", "Add")
                    property("name", user.name.quote())
                    property("worldId", user.worldId)
                    property("worldName", user.worldName.quote())
                    property("rank", user.rank)
                }
                is UpdateFriendChatChannelSingleUser.RemovedFriendChatUser -> {
                    property("type", "Remove")
                    property("name", user.name.quote())
                    property("worldId", user.worldId)
                }
            }
        }
    }

    private fun formatNpcInfoStepDirection(dir: Int): String {
        return when (dir) {
            0 -> "North-West"
            1 -> "North"
            2 -> "North-East"
            3 -> "West"
            4 -> "East"
            5 -> "South-West"
            6 -> "South"
            7 -> "South-East"
            else -> dir.toString()
        }
    }

    override fun npcInfo(message: NpcInfo) {
        publishProt()
        prenpcinfo(message)
        val world = stateTracker.getActiveWorld()
        for ((index, update) in message.updates) {
            val lines = mutableListOf<String>()
            when (update) {
                is NpcUpdateType.Active -> {
                    val npc = world.getNpc(index)
                    val name = npc.name?.quote()
                    container.publish(
                        format(2, "Npc") {
                            if (update.steps.isNotEmpty()) {
                                property(
                                    "npc",
                                    "(index=$index, id=${formatter.type(ScriptVarType.NPC, npc.id)}, " +
                                        (if (name != null) "name=$name, " else "") +
                                        "lastCoord=${formatter.coord(npc.coord)}, " +
                                        "newCoord=${formatter.coord(update.level, update.x, update.z)})",
                                )
                                property(
                                    "speed",
                                    update.moveSpeed.name
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                        .quote(),
                                )
                                if (update.steps.size == 1) {
                                    property("step", formatNpcInfoStepDirection(update.steps.first()).quote())
                                } else {
                                    val (first, second) = update.steps
                                    property("step1", formatNpcInfoStepDirection(first).quote())
                                    property("step2", formatNpcInfoStepDirection(second).quote())
                                }
                            } else {
                                property(
                                    "npc",
                                    "(index=$index, id=${formatter.type(ScriptVarType.NPC, npc.id)}, " +
                                        (if (name != null) "name=$name, " else "") +
                                        "coord=${formatter.coord(npc.coord)})",
                                )
                            }
                        },
                    )
                    appendExtendedInfo(world, npc, lines, update.extendedInfo)
                }
                NpcUpdateType.HighResolutionToLowResolution -> {
                    val npc = world.getNpc(index)
                    val name = npc.name?.quote()
                    container.publish(
                        format(2, "Npc") {
                            property(
                                "npc",
                                "(index=$index, id=${npc.id}, " +
                                    (if (name != null) "name=$name, " else "") +
                                    "lastCoord=${formatter.coord(npc.coord)})",
                            )
                            property("update", "Removed")
                        },
                    )
                }
                is NpcUpdateType.LowResolutionToHighResolution -> {
                    val npc = world.getNpc(index)
                    container.publish(
                        format(2, "Npc") {
                            property("npc", npc(index))
                            filteredProperty("creationcycle", update.spawnCycle.format()) { it != "0" }
                            property("angle", update.angle)
                            filteredProperty("jump", update.jump) { it }
                            property("update", "Added")
                        },
                    )
                    appendExtendedInfo(world, npc, lines, update.extendedInfo)
                }
                NpcUpdateType.Idle -> {
                    // noop
                }
            }
            container.publish(lines)
        }
        postnpcinfo(message)
    }

    private fun appendExtendedInfo(
        world: World,
        npc: Npc,
        lines: MutableList<String>,
        extendedInfo: List<ExtendedInfo>,
    ) {
        for (info in extendedInfo) {
            when (info) {
                is ExactMoveExtendedInfo -> {
                    val curX = npc.coord.x
                    val curZ = npc.coord.z
                    val level = npc.coord.level
                    lines +=
                        format(3, "ExactMove") {
                            property("to1", CoordGrid(level, curX - info.deltaX2, curZ - info.deltaZ2))
                            property("delay1", info.delay1)
                            property("to2", CoordGrid(level, curX - info.deltaX1, curZ - info.deltaZ1))
                            property("delay2", info.delay2)
                            property("angle", info.direction)
                        }
                }
                is FacePathingEntityExtendedInfo -> {
                    lines += format(3, "FacePathingEntity", actor(info.index))
                }
                is HitExtendedInfo -> {
                    if (info.hits.isNotEmpty()) {
                        lines += format(3, "Hits")
                        for (hit in info.hits) {
                            lines +=
                                format(4, "Hit") {
                                    property("type", hit.type)
                                    property("value", hit.value)
                                    if (hit.soakType != -1) {
                                        property("soakType", hit.soakType)
                                        property("soakValue", hit.soakValue)
                                    }
                                    filteredProperty("delay", hit.delay) { it != 0 }
                                }
                        }
                    }
                    if (info.headbars.isNotEmpty()) {
                        lines += format(3, "Headbars")
                        for (headbar in info.headbars) {
                            lines +=
                                format(4, "Headbar") {
                                    property("type", headbar.type)
                                    property("startFill", headbar.startFill)
                                    property("endFill", headbar.endFill)
                                    property("startTime", headbar.startTime)
                                    property("endTime", headbar.endTime)
                                }
                        }
                    }
                }
                is SayExtendedInfo -> {
                    lines +=
                        format(3, "Say") {
                            property("text", info.text.quote())
                        }
                }
                is SequenceExtendedInfo -> {
                    lines +=
                        format(3, "Sequence") {
                            property("id", formatter.type(ScriptVarType.SEQ, info.id.maxUShortToMinusOne()))
                            filteredProperty("delay", info.delay) { it != 0 }
                        }
                }
                is TintingExtendedInfo -> {
                    lines +=
                        format(3, "Tinting") {
                            property("start", info.start)
                            property("end", info.end)
                            property("hue", info.hue)
                            property("saturation", info.saturation)
                            property("lightness", info.lightness)
                            property("weight", info.weight)
                        }
                }
                is SpotanimExtendedInfo -> {
                    for ((slot, spotanim) in info.spotanims) {
                        lines +=
                            format(3, "Spotanim") {
                                property("slot", slot)
                                property("id", formatter.type(ScriptVarType.SPOTANIM, spotanim.id))
                                filteredProperty("delay", spotanim.delay) { it != 0 }
                                filteredProperty("height", spotanim.height) { it != 0 }
                            }
                    }
                }
                is OldSpotanimExtendedInfo -> {
                    lines +=
                        format(3, "OldSpotanim") {
                            property("id", formatter.type(ScriptVarType.SPOTANIM, info.id))
                            filteredProperty("delay", info.delay) { it != 0 }
                            filteredProperty("height", info.height) { it != 0 }
                        }
                }
                is BaseAnimationSetExtendedInfo -> {
                    lines +=
                        format(3, "Bas") {
                            val turnleft = info.turnLeftAnim
                            val turnright = info.turnRightAnim
                            val walk = info.walkAnim
                            val walkback = info.walkAnimBack
                            val walkleft = info.walkAnimLeft
                            val walkright = info.walkAnimRight
                            val run = info.runAnim
                            val runback = info.runAnimBack
                            val runleft = info.runAnimLeft
                            val runright = info.runAnimRight
                            val crawl = info.crawlAnim
                            val crawlback = info.crawlAnimBack
                            val crawlleft = info.crawlAnimLeft
                            val crawlright = info.crawlAnimRight
                            val ready = info.readyAnim
                            if (turnleft != null) {
                                property("turnleft", formatter.type(ScriptVarType.SEQ, turnleft))
                            }
                            if (turnright != null) {
                                property("turnright", formatter.type(ScriptVarType.SEQ, turnright))
                            }
                            if (walk != null) {
                                property("walk", formatter.type(ScriptVarType.SEQ, walk))
                            }
                            if (walkback != null) {
                                property("walkback", formatter.type(ScriptVarType.SEQ, walkback))
                            }
                            if (walkleft != null) {
                                property("walkleft", formatter.type(ScriptVarType.SEQ, walkleft))
                            }
                            if (walkright != null) {
                                property("walkright", formatter.type(ScriptVarType.SEQ, walkright))
                            }
                            if (run != null) {
                                property("run", formatter.type(ScriptVarType.SEQ, run))
                            }
                            if (runback != null) {
                                property("runback", formatter.type(ScriptVarType.SEQ, runback))
                            }
                            if (runleft != null) {
                                property("runleft", formatter.type(ScriptVarType.SEQ, runleft))
                            }
                            if (runright != null) {
                                property("runright", formatter.type(ScriptVarType.SEQ, runright))
                            }
                            if (crawl != null) {
                                property("crawl", formatter.type(ScriptVarType.SEQ, crawl))
                            }
                            if (crawlback != null) {
                                property("crawlback", formatter.type(ScriptVarType.SEQ, crawlback))
                            }
                            if (crawlleft != null) {
                                property("crawlleft", formatter.type(ScriptVarType.SEQ, crawlleft))
                            }
                            if (crawlright != null) {
                                property("crawlright", formatter.type(ScriptVarType.SEQ, crawlright))
                            }
                            if (ready != null) {
                                property("ready", formatter.type(ScriptVarType.SEQ, ready))
                            }
                        }
                }
                is BodyCustomisationExtendedInfo -> {
                    when (val type = info.type) {
                        is ModelCustomisation -> {
                            lines +=
                                format(3, "BodyCustomisation") {
                                    val models = type.models
                                    if (models != null) {
                                        val joined =
                                            models.joinToString(prefix = "[", postfix = "]") {
                                                formatter.type(
                                                    ScriptVarType.MODEL,
                                                    it,
                                                )
                                            }
                                        property("models", joined)
                                    }
                                    val recol = type.recolours
                                    if (recol != null) {
                                        property("recolours", recol)
                                    }
                                    val retex = type.retextures
                                    if (retex != null) {
                                        val joined =
                                            retex.joinToString(prefix = "[", postfix = "]") {
                                                formatter.type(
                                                    ScriptVarType.TEXTURE,
                                                    it,
                                                )
                                            }
                                        property("retextures", joined)
                                    }
                                    val mirror = type.mirror
                                    if (mirror != null) {
                                        property("mirror", mirror)
                                    }
                                }
                        }
                        ResetCustomisation -> {
                            lines +=
                                format(3, "BodyCustomisation") {
                                    property("type", "Reset")
                                }
                        }
                    }
                }
                is HeadCustomisationExtendedInfo -> {
                    when (val type = info.type) {
                        is ModelCustomisation -> {
                            lines +=
                                format(3, "HeadCustomisation") {
                                    val models = type.models
                                    if (models != null) {
                                        val joined =
                                            models.joinToString(prefix = "[", postfix = "]") {
                                                formatter.type(
                                                    ScriptVarType.MODEL,
                                                    it,
                                                )
                                            }
                                        property("models", joined)
                                    }
                                    val recol = type.recolours
                                    if (recol != null) {
                                        property("recolours", recol)
                                    }
                                    val retex = type.retextures
                                    if (retex != null) {
                                        val joined =
                                            retex.joinToString(prefix = "[", postfix = "]") {
                                                formatter.type(
                                                    ScriptVarType.TEXTURE,
                                                    it,
                                                )
                                            }
                                        property("retextures", joined)
                                    }
                                    val mirror = type.mirror
                                    if (mirror != null) {
                                        property("mirror", mirror)
                                    }
                                }
                        }
                        ResetCustomisation -> {
                            lines +=
                                format(3, "HeadCustomisation") {
                                    property("type", "Reset")
                                }
                        }
                    }
                }
                is CombatLevelChangeExtendedInfo -> {
                    lines +=
                        format(3, "LevelChange") {
                            property("level", info.level.format())
                        }
                }
                is EnabledOpsExtendedInfo -> {
                    lines +=
                        format(3, "EnabledOps") {
                            property("opflags", "0b" + info.value.toString(2))
                        }
                }
                is FaceCoordExtendedInfo -> {
                    lines +=
                        format(3, "FaceCoord") {
                            property("coord", formatter.coord(npc.coord.level, info.x, info.z))
                            filteredProperty("instant", info.instant) { it }
                        }
                }
                is NameChangeExtendedInfo -> {
                    lines +=
                        format(3, "NameChange") {
                            val oldName = npc.name
                            if (oldName != null) {
                                property("oldName", oldName.quote())
                                property("newName", info.name.quote())
                            } else {
                                property("name", info.name.quote())
                            }
                        }
                }
                is TransformationExtendedInfo -> {
                    world.updateNpcName(npc.index, cache.getNpcType(info.id)?.name)
                    lines +=
                        format(3, "Transformation") {
                            property("oldId", formatter.type(ScriptVarType.NPC, npc.id))
                            property("newId", formatter.type(ScriptVarType.NPC, info.id))
                        }
                }
                is HeadIconCustomisationExtendedInfo -> {
                    for (i in info.groups.indices) {
                        val group = info.groups[i]
                        val index = info.indices[i]
                        if (group == -1 && index == -1) {
                            continue
                        }
                        lines +=
                            format(3, "HeadIconCustomisation") {
                                property("graphic", formatter.type(ScriptVarType.GRAPHIC, group))
                                property("index", index)
                            }
                    }
                }
                else -> throw IllegalStateException("Unknown extended info: $info")
            }
        }
    }

    private fun prenpcinfo(message: NpcInfo) {
        val world = stateTracker.getActiveWorld()
        for ((index, update) in message.updates) {
            when (update) {
                is NpcUpdateType.Active -> {
                }
                NpcUpdateType.HighResolutionToLowResolution -> {
                }
                is NpcUpdateType.LowResolutionToHighResolution -> {
                    val name = cache.getNpcType(update.id)?.name
                    world.createNpc(
                        index,
                        update.id,
                        name,
                        update.spawnCycle,
                        CoordGrid(update.level, update.x, update.z),
                    )
                }
                NpcUpdateType.Idle -> {
                    // noop
                }
            }
        }
    }

    private fun postnpcinfo(message: NpcInfo) {
        val world = stateTracker.getActiveWorld()
        for ((index, update) in message.updates) {
            when (update) {
                is NpcUpdateType.Active -> {
                    world.updateNpc(index, CoordGrid(update.level, update.x, update.z))
                }
                NpcUpdateType.HighResolutionToLowResolution -> {
                    world.removeNpc(index)
                }
                is NpcUpdateType.LowResolutionToHighResolution -> {
                }
                NpcUpdateType.Idle -> {
                    // noop
                }
            }
        }
    }

    override fun setNpcUpdateOrigin(message: SetNpcUpdateOrigin) {
        publish {
            property("coord", buildAreaCoord(message.originX, message.originZ))
        }
    }

    private fun publishProt() {
        container.publish("[${stateTracker.currentProt}]".indent(1))
    }

    override fun playerInfo(message: PlayerInfo) {
        // Assign the coord and name of each player that is being added
        preloadPlayerInfo(message)
        // Log any activities that happened for all the players
        logPlayerInfo(message)
        // Update the last known coord and name of each player being processed
        postPlayerInfo(message)
    }

    private fun logPlayerInfo(message: PlayerInfo) {
        publishProt()
        val lines = mutableListOf<String>()
        for ((index, update) in message.updates) {
            when (update) {
                is PlayerUpdateType.LowResolutionMovement,
                PlayerUpdateType.LowResolutionIdle,
                -> {
                    // no-op
                }
                is PlayerUpdateType.HighResolutionIdle -> {
                    // No need to spam if the player isn't actually updating
                    if (update.extendedInfo.isEmpty()) {
                        continue
                    }
                    val player = stateTracker.getPlayer(index)
                    lines +=
                        format(2, "Player") {
                            property("index", index)
                            property("name", player.name.quote())
                            property("coord", player.coord)
                            property("update", "idle")
                        }
                    appendExtendedInfo(player, lines, update.extendedInfo)
                }
                is PlayerUpdateType.HighResolutionMovement -> {
                    val player = stateTracker.getPlayer(index)
                    lines +=
                        format(2, "Player") {
                            property("index", index)
                            property("name", player.name.quote())
                            property("oldCoord", player.coord)
                            property("newCoord", update.coord)
                            property("update", "moving")
                        }
                    appendExtendedInfo(player, lines, update.extendedInfo)
                }
                is PlayerUpdateType.HighResolutionToLowResolution -> {
                    val player = stateTracker.getPlayer(index)
                    lines +=
                        format(2, "Player") {
                            property("index", index)
                            property("name", player.name.quote())
                            property("lastCoord", player.coord)
                            property("update", "delete")
                        }
                }
                is PlayerUpdateType.LowResolutionToHighResolution -> {
                    val player = stateTracker.getPlayer(index)
                    lines +=
                        format(2, "Player") {
                            property("index", index)
                            property("name", player.name.quote())
                            property("coord", player.coord)
                            property("update", "add")
                        }
                    appendExtendedInfo(player, lines, update.extendedInfo)
                }
            }
        }
        container.publish(lines)
    }

    private fun appendExtendedInfo(
        player: Player,
        lines: MutableList<String>,
        extendedInfo: List<ExtendedInfo>,
    ) {
        for (info in extendedInfo) {
            when (info) {
                is ChatExtendedInfo -> {
                    lines +=
                        format(3, "Chat") {
                            property("text", info.text.quote())
                            filteredProperty("autotyper", info.autotyper) { it }
                            filteredProperty("colour", info.colour) { it != 0 }
                            filteredProperty("effects", info.effects) { it != 0 }
                            filteredProperty("modicon", info.modIcon) { it != 0 }
                            filteredProperty("pattern", info.pattern) { it != null }
                        }
                }
                is FaceAngleExtendedInfo -> {
                    lines +=
                        format(3, "FaceAngle") {
                            property("angle", info.angle)
                        }
                }
                is MoveSpeedExtendedInfo -> {
                    lines +=
                        format(3, "MoveSpeed") {
                            property("speed", formatMoveSpeed(info.speed))
                        }
                }
                is TemporaryMoveSpeedExtendedInfo -> {
                    lines +=
                        format(3, "TempMoveSpeed") {
                            property("speed", formatMoveSpeed(info.speed))
                        }
                }
                is NameExtrasExtendedInfo -> {
                    lines +=
                        format(3, "NameExtras") {
                            property("beforeName", info.beforeName.quote())
                            property("afterName", info.afterName.quote())
                            property("afterCombatLevel", info.afterCombatLevel.quote())
                        }
                }
                is SayExtendedInfo -> {
                    lines +=
                        format(3, "Say") {
                            property("text", info.text.quote())
                        }
                }
                is SequenceExtendedInfo -> {
                    lines +=
                        format(3, "Sequence") {
                            property("id", formatter.type(ScriptVarType.SEQ, info.id.maxUShortToMinusOne()))
                            filteredProperty("delay", info.delay) { it != 0 }
                        }
                }
                is ExactMoveExtendedInfo -> {
                    val curX = player.coord.x
                    val curZ = player.coord.z
                    val level = player.coord.level
                    lines +=
                        format(3, "ExactMove") {
                            property("to1", CoordGrid(level, curX - info.deltaX2, curZ - info.deltaZ2))
                            property("delay1", info.delay1)
                            property("to2", CoordGrid(level, curX - info.deltaX1, curZ - info.deltaZ1))
                            property("delay2", info.delay2)
                            property("angle", info.direction)
                        }
                }
                is HitExtendedInfo -> {
                    if (info.hits.isNotEmpty()) {
                        lines += format(3, "Hits")
                        for (hit in info.hits) {
                            lines +=
                                format(4, "Hit") {
                                    property("type", hit.type)
                                    property("value", hit.value)
                                    if (hit.soakType != -1) {
                                        property("soakType", hit.soakType)
                                        property("soakValue", hit.soakValue)
                                    }
                                    filteredProperty("delay", hit.delay) { it != 0 }
                                }
                        }
                    }
                    if (info.headbars.isNotEmpty()) {
                        lines += format(3, "Headbars")
                        for (headbar in info.headbars) {
                            lines +=
                                format(4, "Headbar") {
                                    property("type", headbar.type)
                                    property("startFill", headbar.startFill)
                                    property("endFill", headbar.endFill)
                                    property("startTime", headbar.startTime)
                                    property("endTime", headbar.endTime)
                                }
                        }
                    }
                }
                is TintingExtendedInfo -> {
                    lines +=
                        format(3, "Tinting") {
                            property("start", info.start)
                            property("end", info.end)
                            property("hue", info.hue)
                            property("saturation", info.saturation)
                            property("lightness", info.lightness)
                            property("weight", info.weight)
                        }
                }
                is SpotanimExtendedInfo -> {
                    for ((slot, spotanim) in info.spotanims) {
                        lines +=
                            format(3, "Spotanim") {
                                property("slot", slot)
                                property("id", formatter.type(ScriptVarType.SPOTANIM, spotanim.id))
                                filteredProperty("delay", spotanim.delay) { it != 0 }
                                filteredProperty("height", spotanim.height) { it != 0 }
                            }
                    }
                }
                is FacePathingEntityExtendedInfo -> {
                    lines += format(3, "FacePathingEntity", actor(info.index))
                }
                is AppearanceExtendedInfo -> {
                    lines += format(3, "Appearance")
                    lines +=
                        format(4, "Details") {
                            property("name", info.name.quote())
                            property("combatLevel", info.combatLevel)
                            property("skillLevel", info.skillLevel)
                            property("gender", info.gender)
                            property("textGender", info.textGender)
                        }
                    lines +=
                        format(4, "Status") {
                            property("hidden", info.hidden)
                            property("skullIcon", info.skullIcon)
                            property("overheadIcon", info.overheadIcon)
                            if (info.transformedNpcId != -1) {
                                property("npc", formatter.type(ScriptVarType.NPC, info.transformedNpcId))
                            }
                        }
                    if (info.transformedNpcId == -1) {
                        lines +=
                            format(4, "Equipment") {
                                for ((index, value) in info.identKit.withIndex()) {
                                    if (value >= 512) {
                                        property(
                                            formatWearPos(index),
                                            formatter.type(ScriptVarType.OBJ, value - 512),
                                        )
                                    }
                                }
                            }
                        val identKit = IntArray(info.identKit.size)
                        lines +=
                            format(4, "IdentKit") {
                                for ((index, value) in info.identKit.withIndex()) {
                                    if (value in 256..<512) {
                                        identKit[index] = value
                                        property(
                                            formatWearPos(index),
                                            formatter.type(ScriptVarType.IDKIT, value - 256),
                                        )
                                    }
                                }
                            }
                        if (!identKit.contentEquals(info.interfaceIdentKit)) {
                            lines +=
                                format(4, "InterfaceIdentKit") {
                                    for ((index, value) in info.interfaceIdentKit.withIndex()) {
                                        if (value in 256..<512) {
                                            identKit[index] = value
                                            property(
                                                formatWearPos(index),
                                                formatter.type(ScriptVarType.IDKIT, value - 256),
                                            )
                                        }
                                    }
                                }
                        }
                    }
                    lines +=
                        format(4, "Colours") {
                            for ((index, value) in info.colours.withIndex()) {
                                property("col$index", value)
                            }
                        }
                    lines +=
                        format(4, "Bas") {
                            property("ready", formatter.type(ScriptVarType.SEQ, info.readyAnim.maxUShortToMinusOne()))
                            property("turn", formatter.type(ScriptVarType.SEQ, info.turnAnim.maxUShortToMinusOne()))
                            property("walk", formatter.type(ScriptVarType.SEQ, info.walkAnim.maxUShortToMinusOne()))
                            property(
                                "walkback",
                                formatter.type(ScriptVarType.SEQ, info.walkAnimBack.maxUShortToMinusOne()),
                            )
                            property(
                                "walkleft",
                                formatter.type(ScriptVarType.SEQ, info.walkAnimLeft.maxUShortToMinusOne()),
                            )
                            property(
                                "walkright",
                                formatter.type(ScriptVarType.SEQ, info.walkAnimRight.maxUShortToMinusOne()),
                            )
                            property("run", formatter.type(ScriptVarType.SEQ, info.runAnim.maxUShortToMinusOne()))
                        }
                    lines +=
                        format(4, "NameExtras") {
                            property("beforeName", info.beforeName.quote())
                            property("afterName", info.afterName.quote())
                            property("afterCombatLevel", info.afterCombatLevel.quote())
                        }
                    lines +=
                        format(4, "ObjTypeCustomisation") {
                            property("forceModelRefresh", info.forceModelRefresh)
                            val customisation = info.objTypeCustomisation
                            if (customisation != null) {
                                for ((index, cus) in customisation.withIndex()) {
                                    if (cus == null) {
                                        continue
                                    }
                                    property("wearpos", formatWearPos(index))
                                    val recolIndex1 = cus.recolIndices and 0xF
                                    val recolIndex2 = cus.recolIndices ushr 4 and 0xF
                                    if (recolIndex1 != 0xF) {
                                        property("recol$recolIndex1", cus.recol1)
                                    }
                                    if (recolIndex2 != 0xF) {
                                        property("recol$recolIndex2", cus.recol2)
                                    }

                                    val retexIndex1 = cus.retexIndices and 0xF
                                    val retexIndex2 = cus.retexIndices ushr 4 and 0xF
                                    if (retexIndex1 != 0xF) {
                                        property("retex$retexIndex1", formatter.type(ScriptVarType.TEXTURE, cus.retex1))
                                    }
                                    if (retexIndex2 != 0xF) {
                                        property("retex$retexIndex2", formatter.type(ScriptVarType.TEXTURE, cus.retex2))
                                    }
                                }
                            }
                        }
                }
                else -> throw IllegalStateException("Unknown extended info: $info")
            }
        }
    }

    private fun formatWearPos(id: Int): String {
        return when (id) {
            0 -> "hat"
            1 -> "back"
            2 -> "front"
            3 -> "righthand"
            4 -> "torso"
            5 -> "lefthand"
            6 -> "arms"
            7 -> "legs"
            8 -> "head"
            9 -> "hands"
            10 -> "feet"
            11 -> "jaw"
            12 -> "ring"
            13 -> "quiver"
            else -> error("Unknown wearpos $id")
        }
    }

    private fun Int.maxUShortToMinusOne(): Int {
        return if (this == 0xFFFF) {
            -1
        } else {
            this
        }
    }

    private fun formatMoveSpeed(value: Int): String {
        return when (value) {
            0 -> "crawl"
            1 -> "walk"
            2 -> "run"
            -1, 127, 255 -> "teleport"
            else -> "unknown($value)"
        }
    }

    private fun loadPlayerName(
        index: Int,
        extendedInfo: List<ExtendedInfo>,
    ): String {
        val appearance =
            extendedInfo
                .filterIsInstance<AppearanceExtendedInfo>()
                .singleOrNull()
        return appearance?.name
            ?: stateTracker.getLastKnownPlayerName(index)
            ?: "null"
    }

    private fun preloadPlayerInfo(message: PlayerInfo) {
        for ((index, update) in message.updates) {
            when (update) {
                is PlayerUpdateType.LowResolutionToHighResolution -> {
                    val name = loadPlayerName(index, update.extendedInfo)
                    stateTracker.overridePlayer(Player(index, name, update.coord))
                }
                is PlayerUpdateType.HighResolutionIdle -> {
                    val name = loadPlayerName(index, update.extendedInfo)
                    val player = stateTracker.getPlayer(index)
                    stateTracker.overridePlayer(Player(index, name, player.coord))
                }
                is PlayerUpdateType.HighResolutionMovement -> {
                    val name = loadPlayerName(index, update.extendedInfo)
                    val player = stateTracker.getPlayer(index)
                    stateTracker.overridePlayer(Player(index, name, player.coord))
                }
                else -> {
                    // No-op, no info to preload
                }
            }
        }
    }

    private fun postPlayerInfo(message: PlayerInfo) {
        for ((index, update) in message.updates) {
            when (update) {
                is PlayerUpdateType.LowResolutionToHighResolution -> {
                    val name = loadPlayerName(index, update.extendedInfo)
                    stateTracker.overridePlayer(Player(index, name, update.coord))
                }
                is PlayerUpdateType.HighResolutionIdle -> {
                    val oldPlayer = stateTracker.getPlayerOrNull(index) ?: return
                    val name = loadPlayerName(index, update.extendedInfo)
                    stateTracker.overridePlayer(Player(index, name, oldPlayer.coord))
                }
                is PlayerUpdateType.HighResolutionMovement -> {
                    val name = loadPlayerName(index, update.extendedInfo)
                    stateTracker.overridePlayer(Player(index, name, update.coord))
                }
                else -> {
                    // No-op, no info to preload
                }
            }
        }
    }

    override fun worldEntityInfo(message: WorldEntityInfo) {
        publishProt()
        for ((index, update) in message.updates) {
            when (update) {
                WorldEntityUpdateType.Idle -> {
                    // no-op, too spammy
                }
                is WorldEntityUpdateType.LowResolutionToHighResolution -> {
                    container.publish(
                        format(2, "AddWorldEntity") {
                            property("index", index)
                            property("width", update.sizeX)
                            property("length", update.sizeZ)
                            property("angle", update.angle)
                            property("unknown", update.unknownProperty)
                            property("coord", formatter.coord(update.coordGrid))
                        },
                    )
                }
                is WorldEntityUpdateType.Active -> {
                    container.publish(
                        format(2, "UpdateWorldEntity") {
                            property("worldentity", worldentity(index))
                            property("angle", update.angle)
                            property("movespeed", "${update.moveSpeed.id * 0.5} tiles/gamecycle")
                            property("coord", formatter.coord(update.coordGrid))
                        },
                    )
                }
                WorldEntityUpdateType.HighResolutionToLowResolution -> {
                    container.publish(
                        format(3, "RemoveWorldEntity") {
                            property("worldentity", worldentity(index))
                        },
                    )
                }
            }
        }

        for ((index, update) in message.updates) {
            when (update) {
                is WorldEntityUpdateType.Active -> {
                    val world = stateTracker.getWorld(index)
                    world.angle = update.angle
                    world.coord = update.coordGrid
                    world.moveSpeed = update.moveSpeed
                }
                WorldEntityUpdateType.HighResolutionToLowResolution -> {
                    stateTracker.destroyWorld(index)
                }
                is WorldEntityUpdateType.LowResolutionToHighResolution -> {
                    val world = stateTracker.createWorld(index)
                    world.sizeX = update.sizeX
                    world.sizeZ = update.sizeZ
                    world.angle = update.angle
                    world.unknownProperty = update.unknownProperty
                    world.coord = update.coordGrid
                }
                WorldEntityUpdateType.Idle -> {
                    // noop
                }
            }
        }
    }

    override fun ifClearInv(message: IfClearInv) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
        }
    }

    override fun ifCloseSub(message: IfCloseSub) {
        val interfaceId = stateTracker.getOpenInterface(message.combinedId)
        stateTracker.closeInterface(message.combinedId)
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            if (interfaceId != null) {
                property("id", formatter.com(interfaceId, -1))
            }
        }
    }

    override fun ifMoveSub(message: IfMoveSub) {
        val interfaceId = stateTracker.getOpenInterface(message.sourceCombinedId)
        stateTracker.moveInterface(message.sourceCombinedId, message.destinationCombinedId)
        publish {
            property("sourceCom", formatter.com(message.sourceInterfaceId, message.sourceComponentId))
            property("destinationCom", formatter.com(message.destinationInterfaceId, message.destinationComponentId))
            if (interfaceId != null) {
                property("id", formatter.com(interfaceId, -1))
            }
        }
    }

    private fun formatIfType(id: Int): String {
        return when (id) {
            0 -> "Modal"
            1 -> "Overlay"
            3 -> "Client"
            else -> id.toString()
        }
    }

    override fun ifOpenSub(message: IfOpenSub) {
        stateTracker.openInterface(message.interfaceId, message.destinationCombinedId)
        publish {
            property("com", formatter.com(message.destinationInterfaceId, message.destinationComponentId))
            property("id", formatter.com(message.interfaceId, -1))
            property("type", formatIfType(message.type))
        }
    }

    override fun ifOpenTop(message: IfOpenTop) {
        val existing = stateTracker.toplevelInterface
        stateTracker.toplevelInterface = message.interfaceId
        publish {
            property("id", formatter.com(message.interfaceId, -1))
            if (existing != -1) {
                property("previousId", formatter.com(existing, -1))
            }
        }
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
            fun list(mask: Int): List<EventMask> {
                return buildList {
                    for (entry in entries) {
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
        stateTracker.toplevelInterface = message.topLevelInterface
        publish {
            property("id", formatter.com(message.topLevelInterface, -1))
        }
        container.publish(format(2, "Sub Interfaces"))
        for (sub in message.subInterfaces) {
            stateTracker.openInterface(sub.interfaceId, sub.destinationCombinedId)
            container.publish(
                format(3, "IfOpenSub") {
                    property("com", formatter.com(sub.destinationInterfaceId, sub.destinationComponentId))
                    property("id", formatter.com(sub.interfaceId, -1))
                    property("type", formatIfType(sub.type))
                },
            )
        }

        container.publish(format(2, "Events"))
        for (events in message.events) {
            container.publish(
                format(3, "IfSetEvents") {
                    property("com", formatter.com(events.interfaceId, events.componentId))
                    property("start", events.start.maxUShortToMinusOne())
                    property("end", events.end.maxUShortToMinusOne())
                    property("events", EventMask.list(events.events))
                },
            )
        }
    }

    override fun ifSetAngle(message: IfSetAngle) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("angleX", message.angleX)
            property("angleY", message.angleY)
            property("zoom", message.zoom)
        }
    }

    override fun ifSetAnim(message: IfSetAnim) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("anim", formatter.type(ScriptVarType.SEQ, message.anim))
        }
    }

    override fun ifSetColour(message: IfSetColour) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("colour", formatter.type(ScriptVarType.COLOUR, message.colour15BitPacked))
        }
    }

    override fun ifSetEvents(message: IfSetEvents) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("start", message.start.maxUShortToMinusOne())
            property("end", message.end.maxUShortToMinusOne())
            property("events", EventMask.list(message.events))
        }
    }

    override fun ifSetHide(message: IfSetHide) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("hide", message.hidden)
        }
    }

    override fun ifSetModel(message: IfSetModel) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("model", formatter.type(ScriptVarType.MODEL, message.model))
        }
    }

    override fun ifSetNpcHead(message: IfSetNpcHead) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("npc", formatter.type(ScriptVarType.NPC, message.npc))
        }
    }

    override fun ifSetNpcHeadActive(message: IfSetNpcHeadActive) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("npc", npc(message.index))
        }
    }

    override fun ifSetObject(message: IfSetObject) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("obj", formatter.type(ScriptVarType.OBJ, message.obj))
            property("zoomorcount", message.count)
        }
    }

    override fun ifSetPlayerHead(message: IfSetPlayerHead) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
        }
    }

    override fun ifSetPlayerModelBaseColour(message: IfSetPlayerModelBaseColour) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("index", message.index)
            property("colour", message.colour)
        }
    }

    override fun ifSetPlayerModelBodyType(message: IfSetPlayerModelBodyType) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("bodyType", message.bodyType)
        }
    }

    override fun ifSetPlayerModelObj(message: IfSetPlayerModelObj) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("obj", formatter.type(ScriptVarType.OBJ, message.obj))
        }
    }

    override fun ifSetPlayerModelSelf(message: IfSetPlayerModelSelf) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("copyobjs", message.copyObjs)
        }
    }

    override fun ifSetPosition(message: IfSetPosition) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("x", message.x)
            property("y", message.y)
        }
    }

    override fun ifSetRotateSpeed(message: IfSetRotateSpeed) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("xspeed", message.xSpeed)
            property("yspeed", message.ySpeed)
        }
    }

    override fun ifSetScrollPos(message: IfSetScrollPos) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("scrollpos", message.scrollPos)
        }
    }

    override fun ifSetText(message: IfSetText) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("text", message.text.quote())
        }
    }

    override fun updateInvFull(message: UpdateInvFull) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("inv", formatter.type(ScriptVarType.INV, message.inventoryId))
        }
        for (obj in message.objs) {
            container.publish(
                format(2, "Obj") {
                    property("id", formatter.type(ScriptVarType.OBJ, obj.id))
                    property("count", obj.count.format())
                },
            )
        }
    }

    override fun updateInvPartial(message: UpdateInvPartial) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            property("inv", formatter.type(ScriptVarType.INV, message.inventoryId))
        }
        for (obj in message.objs) {
            container.publish(
                format(2, "Obj") {
                    property("slot", obj.slot)
                    property("id", formatter.type(ScriptVarType.OBJ, obj.id))
                    property("count", obj.count)
                },
            )
        }
    }

    override fun updateInvStopTransmit(message: UpdateInvStopTransmit) {
        publish {
            property("inv", formatter.type(ScriptVarType.INV, message.inventoryId))
        }
    }

    override fun logout(message: Logout) {
        publishProt()
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
        publish {
            property("host", message.host.quote())
            property("id", message.id)
            property("properties", WorldFlag.list(message.properties))
        }
    }

    private fun formatLogoutReason(num: Int): String {
        return when (num) {
            0 -> "Requested"
            1 -> "Kicked"
            2 -> "Updating"
            else -> num.toString()
        }
    }

    override fun logoutWithReason(message: LogoutWithReason) {
        publish {
            property("reason", formatLogoutReason(message.reason))
        }
    }

    override fun rebuildLogin(message: RebuildLogin) {
        container.publish("[${stateTracker.cycle}]".indent(0))
        stateTracker.overridePlayer(
            Player(
                message.playerInfoInitBlock.localPlayerIndex,
                "uninitialized",
                message.playerInfoInitBlock.localPlayerCoord,
            ),
        )
        stateTracker.localPlayerIndex = message.playerInfoInitBlock.localPlayerIndex
        val world = stateTracker.createWorld(-1)
        world.rebuild(CoordGrid(0, (message.zoneX - 6) shl 3, (message.zoneZ - 6) shl 3))
        publish {
            property("zoneX", message.zoneX)
            property("zoneZ", message.zoneZ)
            property("worldArea", message.worldArea)
            property("localPlayerCoord", message.playerInfoInitBlock.localPlayerCoord)
        }
        val minMapsquareX = (message.zoneX - 6) ushr 3
        val maxMapsquareX = (message.zoneX + 6) ushr 3
        val minMapsquareZ = (message.zoneZ - 6) ushr 3
        val maxMapsquareZ = (message.zoneZ + 6) ushr 3
        val iterator = message.keys.listIterator()
        for (mapsquareX in minMapsquareX..maxMapsquareX) {
            for (mapsquareZ in minMapsquareZ..maxMapsquareZ) {
                val mapsquareId = (mapsquareX shl 8) or mapsquareZ
                val key = iterator.next()
                container.publish(
                    format(2, "XteaKey") {
                        property("mapsquareId", mapsquareId)
                        property("key", key.key.contentToString())
                    },
                )
            }
        }
        check(!iterator.hasNext()) {
            "Xtea keys leftover"
        }
    }

    override fun rebuildNormal(message: RebuildNormal) {
        val world = stateTracker.getWorld(-1)
        world.rebuild(CoordGrid(0, (message.zoneX - 6) shl 3, (message.zoneZ - 6) shl 3))
        publish {
            property("zoneX", message.zoneX)
            property("zoneZ", message.zoneZ)
            property("worldArea", message.worldArea)
        }
        val minMapsquareX = (message.zoneX - 6) ushr 3
        val maxMapsquareX = (message.zoneX + 6) ushr 3
        val minMapsquareZ = (message.zoneZ - 6) ushr 3
        val maxMapsquareZ = (message.zoneZ + 6) ushr 3
        val iterator = message.keys.listIterator()
        for (mapsquareX in minMapsquareX..maxMapsquareX) {
            for (mapsquareZ in minMapsquareZ..maxMapsquareZ) {
                val mapsquareId = (mapsquareX shl 8) or mapsquareZ
                val key = iterator.next()
                container.publish(
                    format(2, "XteaKey") {
                        property("mapsquareId", mapsquareId)
                        property("key", key.key.contentToString())
                    },
                )
            }
        }
        check(!iterator.hasNext()) {
            "Xtea keys leftover"
        }
    }

    override fun rebuildRegion(message: RebuildRegion) {
        val world = stateTracker.getWorld(-1)
        world.rebuild(CoordGrid(0, (message.zoneX - 6) shl 3, (message.zoneZ - 6) shl 3))
        publish {
            property("zoneX", message.zoneX)
            property("zoneZ", message.zoneZ)
            property("reload", message.reload)
        }
        val mapsquares = mutableSetOf<Int>()
        container.publish(format(2, "BuildArea"))
        val startZoneX = message.zoneX - 6
        val startZoneZ = message.zoneZ - 6
        for (level in 0..<4) {
            for (zoneX in startZoneX..(message.zoneX + 6)) {
                for (zoneZ in startZoneZ..(message.zoneZ + 6)) {
                    val block = message.buildArea[level, zoneX - startZoneX, zoneZ - startZoneZ]
                    // Invalid zone
                    if (block.mapsquareId == 32767) continue
                    mapsquares += block.mapsquareId
                    container.publish(
                        format(3, "Zone") {
                            property("src", formatter.zoneCoord(block.level, block.zoneX, block.zoneZ))
                            property("dest", formatter.zoneCoord(level, zoneX, zoneZ))
                            property("rotation", block.rotation)
                        },
                    )
                }
            }
        }
        container.publish(format(2, "Keys"))
        val iterator = message.keys.listIterator()
        for (mapsquareId in mapsquares) {
            val key = iterator.next()
            container.publish(
                format(3, "XteaKey") {
                    property("mapsquareId", mapsquareId)
                    property("key", key.key.contentToString())
                },
            )
        }
        check(!iterator.hasNext()) {
            "Xtea keys leftover"
        }
    }

    override fun rebuildWorldEntity(message: RebuildWorldEntity) {
        val world = stateTracker.getWorld(-1)
        world.rebuild(CoordGrid(0, (message.baseX - 6) shl 3, (message.baseZ - 6) shl 3))
        publish {
            property("worldentity", worldentity(message.index))
            property("zoneX", message.baseX)
            property("zoneZ", message.baseZ)
            property("localPlayerCoord", formatter.coord(message.playerInfoInitBlock.localPlayerCoord))
        }
        val mapsquares = mutableSetOf<Int>()
        container.publish(format(2, "BuildArea"))
        val startZoneX = message.baseX - 6
        val startZoneZ = message.baseZ - 6
        for (level in 0..<4) {
            for (zoneX in startZoneX..(message.baseX + 6)) {
                for (zoneZ in startZoneZ..(message.baseZ + 6)) {
                    val block = message.buildArea[level, zoneX - startZoneX, zoneZ - startZoneZ]
                    // Invalid zone
                    if (block.mapsquareId == 32767) continue
                    mapsquares += block.mapsquareId
                    container.publish(
                        format(3, "Zone") {
                            property("src", formatter.zoneCoord(block.level, block.zoneX, block.zoneZ))
                            property("dest", formatter.zoneCoord(level, zoneX, zoneZ))
                            property("rotation", block.rotation)
                        },
                    )
                }
            }
        }
        container.publish(format(2, "Keys"))
        val iterator = message.keys.listIterator()
        for (mapsquareId in mapsquares) {
            val key = iterator.next()
            container.publish(
                format(3, "XteaKey") {
                    property("mapsquareId", mapsquareId)
                    property("key", key.key.contentToString())
                },
            )
        }
        check(!iterator.hasNext()) {
            "Xtea keys leftover"
        }
    }

    override fun hideLocOps(message: HideLocOps) {
        publish {
            property("hide", message.hidden)
        }
    }

    override fun hideNpcOps(message: HideNpcOps) {
        publish {
            property("hide", message.hidden)
        }
    }

    override fun hidePlayerOps(message: HidePlayerOps) {
        publish {
            property("hide", message.hidden)
        }
    }

    override fun hintArrow(message: HintArrow) {
        publish {
            when (val type = message.type) {
                is HintArrow.NpcHintArrow -> {
                    property("npc", npc(type.index))
                }
                is HintArrow.PlayerHintArrow -> {
                    property("player", player(type.index))
                }
                HintArrow.ResetHintArrow -> {
                    property("type", "Reset")
                }
                is HintArrow.TileHintArrow -> {
                    property("coord", formatter.coord(stateTracker.level(), type.x, type.z))
                    property("height", type.height)
                    property(
                        "position",
                        type.position.name
                            .lowercase()
                            .replaceFirstChar { it.uppercaseChar() },
                    )
                }
            }
        }
    }

    override fun hiscoreReply(message: HiscoreReply) {
        publish {
            property("requestId", message.requestId.format())
        }
        when (val type = message.response) {
            is HiscoreReply.FailedHiscoreReply -> {
                container.publish(
                    format(2, "Failure") {
                        property("reason", type.reason.quote())
                    },
                )
            }
            is HiscoreReply.SuccessfulHiscoreReply -> {
                container.publish(format(2, "Success"))
                container.publish(
                    format(3, "Overall") {
                        property("overallRank", type.overallRank.format())
                        property("overallExperience", type.overallExperience.format())
                    },
                )
                container.publish(format(3, "Stats"))
                for (stat in type.statResults) {
                    container.publish(
                        format(4, "Stat") {
                            property("id", stat.id)
                            property("experience", stat.result.format())
                            property("rank", stat.rank.format())
                        },
                    )
                }
                container.publish(format(3, "Activities"))
                for (stat in type.statResults) {
                    container.publish(
                        format(4, "Activity") {
                            property("id", stat.id)
                            property("result", stat.result.format())
                            property("rank", stat.rank.format())
                        },
                    )
                }
            }
        }
    }

    override fun minimapToggle(message: MinimapToggle) {
        publish {
            property("state", message.minimapState)
        }
    }

    override fun reflectionChecker(message: ReflectionChecker) {
        publish {
            property("id", message.id.format())
        }
        for (check in message.checks) {
            when (check) {
                is ReflectionCheck.GetFieldModifiers -> {
                    container.publish(
                        format(2, "GetFieldModifiers") {
                            property("className", check.className.quote())
                            property("fieldName", check.fieldName.quote())
                        },
                    )
                }
                is ReflectionCheck.GetFieldValue -> {
                    container.publish(
                        format(2, "GetFieldValue") {
                            property("className", check.className.quote())
                            property("fieldName", check.fieldName.quote())
                        },
                    )
                }
                is ReflectionCheck.GetMethodModifiers -> {
                    container.publish(
                        format(2, "GetMethodModifiers") {
                            property("className", check.className.quote())
                            property("methodName", check.methodName.quote())
                            property("returnClass", check.returnClass.quote())
                            property("paramClasses", check.parameterClasses)
                        },
                    )
                }
                is ReflectionCheck.InvokeMethod -> {
                    container.publish(
                        format(2, "InvokeMethod") {
                            property("className", check.className.quote())
                            property("methodName", check.methodName.quote())
                            property("returnClass", check.returnClass.quote())
                            property("paramClasses", check.parameterClasses)
                            val mapped =
                                check.parameterValues
                                    .joinToString(prefix = "[", postfix = "]") {
                                        it.contentToString()
                                    }
                            property("paramValues", mapped)
                        },
                    )
                }
                is ReflectionCheck.SetFieldValue -> {
                    container.publish(
                        format(2, "SetFieldValue") {
                            property("className", check.className.quote())
                            property("fieldName", check.fieldName.quote())
                            property("value", check.value)
                        },
                    )
                }
            }
        }
    }

    override fun resetAnims(message: ResetAnims) {
        publishProt()
    }

    override fun sendPing(message: SendPing) {
        publish {
            property("value1", message.value1.format())
            property("value2", message.value2.format())
        }
    }

    override fun serverTickEnd(message: ServerTickEnd) {
        publishProt()
        stateTracker.incrementCycle()
        container.publish("")
        container.publish("[${stateTracker.cycle}]".indent(0))
    }

    override fun setHeatmapEnabled(message: SetHeatmapEnabled) {
        publish {
            property("enabled", message.enabled)
        }
    }

    override fun siteSettings(message: SiteSettings) {
        publish {
            property("settings", "erased")
        }
    }

    override fun updateRebootTimer(message: UpdateRebootTimer) {
        publish {
            property("gamecycles", message.gameCycles.format())
        }
    }

    override fun updateUid192(message: UpdateUid192) {
        publish {
            property("uid", "erased")
        }
    }

    override fun urlOpen(message: UrlOpen) {
        publish {
            property("url", message.url.quote())
        }
    }

    private fun getChatFilter(id: Int): String {
        return when (id) {
            0 -> "On"
            1 -> "Friends"
            2 -> "Off"
            3 -> "Hide"
            4 -> "Autochat"
            else -> id.toString()
        }
    }

    override fun chatFilterSettings(message: ChatFilterSettings) {
        publish {
            property("public", getChatFilter(message.publicChatFilter))
            property("trade", getChatFilter(message.tradeChatFilter))
        }
    }

    override fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat) {
        publish {
            property("private", getChatFilter(message.privateChatFilter))
        }
    }

    private fun getMessageType(id: Int): String {
        return when (id) {
            0 -> "gamemessage"
            1 -> "modchat"
            2 -> "publicchat"
            3 -> "privatechat"
            4 -> "engine"
            5 -> "loginlogoutnotification"
            6 -> "privatechatout"
            7 -> "modprivatechat"
            9 -> "friendschat"
            11 -> "friendschatnotification"
            14 -> "broadcast"
            26 -> "snapshotfeedback"
            27 -> "obj_examine"
            28 -> "npc_examine"
            29 -> "loc_examine"
            30 -> "friendnotification"
            31 -> "ignorenotification"
            41 -> "clanchat"
            43 -> "clanmessage"
            44 -> "clanguestchat"
            46 -> "clanguestmessage"
            90 -> "autotyper"
            91 -> "modautotyper"
            99 -> "console"
            101 -> "tradereq"
            102 -> "trade"
            103 -> "chalreq_trade"
            104 -> "chalreq_friendschat"
            105 -> "spam"
            106 -> "playerrelated"
            107 -> "10sectimeout"
            109 -> "clancreationinvitation"
            110 -> "clanreq_clanchat"
            114 -> "dialogue"
            115 -> "mesbox"
            else -> id.toString()
        }
    }

    override fun messageGame(message: MessageGame) {
        publish {
            property("type", getMessageType(message.type).quote())
            filteredProperty("name", message.name?.quote()) { it != null }
            property("message", message.message.quote())
        }
    }

    override fun runClientScript(message: RunClientScript) {
        publish {
            property("id", formatter.script(message.id))
        }
        if (message.types.isNotEmpty() || message.values.isNotEmpty()) {
            for (i in message.types.indices) {
                val char = message.types[i]
                val value = message.values[i].toString()
                container.publish(
                    format(2, "Param") {
                        val type =
                            ScriptVarType.entries.firstOrNull { type ->
                                type.char == char
                            }
                        property("type", type?.fullName ?: "'$char'")
                        val result =
                            if (type == ScriptVarType.STRING) {
                                value.quote()
                            } else if (type != null) {
                                formatter.type(type, value.toInt())
                            } else {
                                value
                            }
                        property("value", result)
                    },
                )
            }
        }
    }

    override fun setMapFlag(message: SetMapFlag) {
        publish {
            if (message.xInBuildArea == 0xFF && message.zInBuildArea == 0xFF) {
                property("coord", "null")
            } else {
                property("coord", buildAreaCoord(message.xInBuildArea, message.zInBuildArea))
            }
        }
    }

    override fun setPlayerOp(message: SetPlayerOp) {
        publish {
            property("id", message.id)
            property("op", message.op?.quote() ?: "null")
            filteredProperty("priority", message.priority) { it }
        }
    }

    override fun triggerOnDialogAbort(message: TriggerOnDialogAbort) {
        publishProt()
    }

    override fun updateRunEnergy(message: UpdateRunEnergy) {
        publish {
            property("energy", message.runenergy.format())
        }
    }

    override fun updateRunWeight(message: UpdateRunWeight) {
        publish {
            property("runweight", message.runweight.format() + "g")
        }
    }

    private enum class Stat(
        val id: Int,
    ) {
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
    }

    override fun updateStat(message: UpdateStat) {
        publish {
            val stat = Stat.entries.first { it.id == message.stat }
            property("stat", stat.name.lowercase().replaceFirstChar { it.uppercaseChar() })
            property("level", message.currentLevel)
            filteredProperty("invisibleLevel", message.invisibleBoostedLevel) { it != message.currentLevel }
            property("experience", message.experience.format())
        }
    }

    override fun updateStatOld(message: UpdateStatOld) {
        publish {
            val stat = Stat.entries.first { it.id == message.stat }
            property("stat", stat.name.lowercase().replaceFirstChar { it.uppercaseChar() })
            property("level", message.currentLevel)
            property("experience", message.experience.format())
        }
    }

    override fun updateStockMarketSlot(message: UpdateStockMarketSlot) {
        publish {
            property("slot", message.slot)
            when (val update = message.update) {
                UpdateStockMarketSlot.ResetStockMarketSlot -> {
                    property("update", "Reset")
                }
                is UpdateStockMarketSlot.SetStockMarketSlot -> {
                    property("status", update.status)
                    property("obj", formatter.type(ScriptVarType.OBJ, update.obj))
                    property("price", update.price.format())
                    property("count", update.count.format())
                    property("completedCount", update.completedCount.format())
                    property("completedGold", update.completedGold.format())
                }
            }
        }
    }

    override fun updateTradingPost(message: UpdateTradingPost) {
        publish {
            when (val update = message.updateType) {
                UpdateTradingPost.ResetTradingPost -> {
                    property("update", "Reset")
                }
                is UpdateTradingPost.SetTradingPostOfferList -> {
                    property("age", update.age)
                    property("obj", formatter.type(ScriptVarType.OBJ, update.obj))
                    property("status", update.status)
                }
            }
        }
        when (val update = message.updateType) {
            is UpdateTradingPost.SetTradingPostOfferList -> {
                for (offer in update.offers) {
                    container.publish(
                        format(2, "Offer") {
                            property("name", offer.name.quote())
                            property("previousName", offer.name.quote())
                            property("world", offer.world)
                            property("time", offer.time.format())
                            property("price", offer.price.format())
                            property("count", offer.count.format())
                        },
                    )
                }
            }
            UpdateTradingPost.ResetTradingPost -> {
                // noop
            }
        }
    }

    override fun friendListLoaded(message: FriendListLoaded) {
        publishProt()
    }

    override fun messagePrivate(message: MessagePrivate) {
        publish {
            property("sender", message.sender.quote())
            property("worldId", message.worldId)
            property("worldMessageCounter", message.worldMessageCounter)
            property("chatCrownType", message.chatCrownType)
            property("message", message.message.quote())
        }
    }

    override fun messagePrivateEcho(message: MessagePrivateEcho) {
        publish {
            property("recipient", message.recipient.quote())
            property("message", message.message.quote())
        }
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
        publishProt()
        for (friend in message.friends) {
            when (friend) {
                is UpdateFriendList.OfflineFriend -> {
                    container.publish(
                        format(2, "OfflineFriend") {
                            property("name", friend.name.quote())
                            filteredProperty("previousName", friend.previousName?.quote()) {
                                it != null && it.length > 2
                            }
                            filteredProperty("rank", friend.rank) { it != 0 }
                            filteredProperty("properties", friend.properties) { it != 0 }
                            filteredProperty("notes", friend.notes.quote()) { it.length > 2 }
                            filteredProperty("added", friend.added) { it }
                        },
                    )
                }
                is UpdateFriendList.OnlineFriend -> {
                    container.publish(
                        format(2, "OnlineFriend") {
                            property("name", friend.name.quote())
                            filteredProperty("previousName", friend.previousName?.quote()) {
                                it != null && it.length > 2
                            }
                            property("worldId", friend.worldId)
                            filteredProperty("rank", friend.rank) { it != 0 }
                            filteredProperty("properties", friend.properties) { it != 0 }
                            filteredProperty("notes", friend.notes.quote()) { it.length > 2 }
                            property("worldName", friend.worldName.quote())
                            property("platform", formatPlatform(friend.platform).quote())
                            filteredProperty("worldFlags", friend.worldFlags) { it != 0 }
                            filteredProperty("added", friend.added) { it }
                        },
                    )
                }
            }
        }
    }

    override fun updateIgnoreList(message: UpdateIgnoreList) {
        publishProt()
        for (ignore in message.ignores) {
            when (ignore) {
                is UpdateIgnoreList.AddedIgnoredEntry -> {
                    container.publish(
                        format(2, "AddedIgnore") {
                            property("name", ignore.name.quote())
                            filteredProperty("previousName", ignore.previousName?.quote()) {
                                it != null && it.length > 2
                            }
                            filteredProperty("notes", ignore.note.quote()) { it.length > 2 }
                            filteredProperty("added", ignore.added) { it }
                        },
                    )
                }
                is UpdateIgnoreList.RemovedIgnoredEntry -> {
                    container.publish(
                        format(2, "RemovedIgnore") {
                            property("name", ignore.name.quote())
                        },
                    )
                }
            }
        }
    }

    override fun midiJingle(message: MidiJingle) {
        publish {
            property("id", formatter.type(ScriptVarType.JINGLE, message.id))
            filteredProperty("length", message.lengthInMillis.format() + "ms") { it != "0ms" }
        }
    }

    override fun midiSong(message: MidiSong) {
        publish {
            property("id", formatter.type(ScriptVarType.MIDI, message.id))
            property("fadeOutDelay", message.fadeOutDelay)
            property("fadeOutSpeed", message.fadeOutSpeed)
            property("fadeInDelay", message.fadeInDelay)
            property("fadeInSpeed", message.fadeInSpeed)
        }
    }

    override fun midiSongOld(message: MidiSongOld) {
        publish {
            property("id", formatter.type(ScriptVarType.MIDI, message.id))
        }
    }

    override fun midiSongStop(message: MidiSongStop) {
        publish {
            property("fadeOutDelay", message.fadeOutDelay)
            property("fadeOutSpeed", message.fadeOutSpeed)
        }
    }

    override fun midiSongWithSecondary(message: MidiSongWithSecondary) {
        publish {
            property("primaryId", formatter.type(ScriptVarType.MIDI, message.primaryId))
            property("secondaryId", formatter.type(ScriptVarType.MIDI, message.secondaryId))
            property("fadeOutDelay", message.fadeOutDelay)
            property("fadeOutSpeed", message.fadeOutSpeed)
            property("fadeInDelay", message.fadeInDelay)
            property("fadeInSpeed", message.fadeInSpeed)
        }
    }

    override fun midiSwap(message: MidiSwap) {
        publish {
            property("fadeOutDelay", message.fadeOutDelay)
            property("fadeOutSpeed", message.fadeOutSpeed)
            property("fadeInDelay", message.fadeInDelay)
            property("fadeInSpeed", message.fadeInSpeed)
        }
    }

    override fun synthSound(message: SynthSound) {
        publish {
            property("id", formatter.type(ScriptVarType.SYNTH, message.id))
            filteredProperty("loops", message.loops) { it != 1 }
            filteredProperty("delay", message.delay) { it != 0 }
        }
    }

    override fun locAnimSpecific(message: LocAnimSpecific) {
        val coord =
            buildAreaCoordGrid(
                message.coordInBuildArea.xInBuildArea,
                message.coordInBuildArea.zInBuildArea,
            )
        publish {
            property("loc", loc(message.id, coord, message.shape, message.rotation))
        }
    }

    override fun mapAnimSpecific(message: MapAnimSpecific) {
        publish {
            property("id", formatter.type(ScriptVarType.SPOTANIM, message.id))
            filteredProperty("delay", message.delay) { it != 0 }
            filteredProperty("height", message.height) { it != 0 }
            property(
                "coord",
                buildAreaCoord(
                    message.coordInBuildArea.xInBuildArea,
                    message.coordInBuildArea.zInBuildArea,
                ),
            )
        }
    }

    override fun npcAnimSpecific(message: NpcAnimSpecific) {
        publish {
            property("npc", npc(message.index))
            property("anim", formatter.type(ScriptVarType.SEQ, message.id))
            filteredProperty("delay", message.delay) { it != 0 }
        }
    }

    override fun npcHeadIconSpecific(message: NpcHeadIconSpecific) {
        publish {
            property("npc", npc(message.index))
            property("slot", message.headIconSlot)
            property("graphic", formatter.type(ScriptVarType.GRAPHIC, message.spriteGroup))
            filteredProperty("graphicIndex", message.spriteIndex) { it != 0 }
        }
    }

    override fun npcSpotAnimSpecific(message: NpcSpotAnimSpecific) {
        publish {
            property("npc", npc(message.index))
            property("slot", message.slot)
            property("spotanim", formatter.type(ScriptVarType.SPOTANIM, message.id))
            filteredProperty("height", message.height) { it != 0 }
            filteredProperty("delay", message.delay) { it != 0 }
        }
    }

    override fun playerAnimSpecific(message: PlayerAnimSpecific) {
        publish {
            property("anim", formatter.type(ScriptVarType.SEQ, message.id))
            filteredProperty("delay", message.delay) { it != 0 }
        }
    }

    override fun playerSpotAnimSpecific(message: PlayerSpotAnimSpecific) {
        publish {
            property("npc", player(message.index))
            property("slot", message.slot)
            property("spotanim", formatter.type(ScriptVarType.SPOTANIM, message.id))
            filteredProperty("height", message.height) { it != 0 }
            filteredProperty("delay", message.delay) { it != 0 }
        }
    }

    override fun projAnimSpecific(message: ProjAnimSpecific) {
        publish {
            property("id", formatter.type(ScriptVarType.SPOTANIM, message.id))
            property("start", message.startTime)
            property("end", message.endTime)
            property("angle", message.angle)
            property("progress", message.progress)
            property("startheight", message.startHeight)
            property("endheight", message.endHeight)
        }
        // TODO: Multiplier
        container.publish(
            format(3, "Source") {
                property(
                    "coord",
                    buildAreaCoord(
                        message.coordInBuildArea.xInBuildArea,
                        message.coordInBuildArea.zInBuildArea,
                    ),
                )
            },
        )
        container.publish(
            format(3, "Target") {
                property(
                    "coord",
                    buildAreaCoord(
                        message.coordInBuildArea.xInBuildArea + message.deltaX,
                        message.coordInBuildArea.zInBuildArea + message.deltaZ,
                    ),
                )
                if (message.targetIndex != 0xFFFFFF) {
                    property("target", actor(message.targetIndex))
                }
            },
        )
    }

    private fun getImpactedVarbits(
        basevar: Int,
        oldValue: Int,
        newValue: Int,
    ): List<VarBitType> {
        return cache
            .listVarBitTypes()
            .asSequence()
            .filter { it.basevar == basevar }
            .filter { type ->
                val bitcount = (type.endbit - type.startbit) + 1
                val bitmask = type.bitmask(bitcount)
                val oldVarbitValue = oldValue ushr type.startbit and bitmask
                val newVarbitValue = newValue ushr type.startbit and bitmask
                oldVarbitValue != newVarbitValue
            }.toList()
    }

    private fun publishVarbits(
        oldValue: Int,
        newValue: Int,
        impactedVarbits: List<VarBitType>,
        indentation: Int,
    ) {
        for (varbit in impactedVarbits) {
            container.publish(
                format(indentation, if (indentation == 1) "VARBIT" else "Varbit") {
                    if (indentation == 1) {
                        property("basevar", formatter.varp(varbit.basevar))
                    }
                    property("id", formatter.varbit(varbit.id))
                    val bitcount = (varbit.endbit - varbit.startbit) + 1
                    val bitmask = varbit.bitmask(bitcount)
                    val oldVarbitValue = oldValue ushr varbit.startbit and bitmask
                    val newVarbitValue = newValue ushr varbit.startbit and bitmask
                    property("oldValue", oldVarbitValue)
                    property("newValue", newVarbitValue)
                },
            )
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
        val oldValue = stateTracker.getVarp(message.id)
        val impactedVarbits = getImpactedVarbits(message.id, oldValue, message.value)
        stateTracker.setVarp(message.id, message.value)
        if (remainingImpactedBits(oldValue, message.value, impactedVarbits) == 0) {
            publishVarbits(oldValue, message.value, impactedVarbits, 1)
        } else {
            publish {
                property("id", formatter.varp(message.id))
                property("oldValue", oldValue)
                property("newValue", message.value)
            }
            publishVarbits(oldValue, message.value, impactedVarbits, 2)
        }
    }

    override fun varpReset(message: VarpReset) {
        publishProt()
    }

    override fun varpSmall(message: VarpSmall) {
        val oldValue = stateTracker.getVarp(message.id)
        val impactedVarbits = getImpactedVarbits(message.id, oldValue, message.value)
        stateTracker.setVarp(message.id, message.value)
        if (remainingImpactedBits(oldValue, message.value, impactedVarbits) == 0) {
            publishVarbits(oldValue, message.value, impactedVarbits, 1)
        } else {
            publish {
                property("id", formatter.varp(message.id))
                property("oldValue", oldValue)
                property("newValue", message.value)
            }
            publishVarbits(oldValue, message.value, impactedVarbits, 2)
        }
    }

    override fun varpSync(message: VarpSync) {
        publishProt()
    }

    override fun clearEntities(message: ClearEntities) {
        publishProt()
        stateTracker.destroyDynamicWorlds()
    }

    override fun setActiveWorld(message: SetActiveWorld) {
        publish {
            when (val type = message.worldType) {
                is SetActiveWorld.DynamicWorldType -> {
                    property("type", "Dynamic")
                    property("worldentity", worldentity(type.index))
                    property("level", type.activeLevel)
                }
                is SetActiveWorld.RootWorldType -> {
                    property("type", "Root")
                    property("level", type.activeLevel)
                }
            }
        }
    }

    override fun updateZoneFullFollows(message: UpdateZoneFullFollows) {
        stateTracker.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
        publish {
            property("coord", buildAreaCoord(message.zoneX, message.zoneZ, message.level))
        }
    }

    override fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed) {
        stateTracker.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
        publish {
            property("coord", buildAreaCoord(message.zoneX, message.zoneZ, message.level))
        }
        for (event in message.packets) {
            when (event) {
                is LocAddChange -> {
                    container.publish(
                        format(2, "LocAddChange") {
                            buildLocAddChange(event)
                        },
                    )
                }
                is LocAnim -> {
                    container.publish(
                        format(2, "LocAnim") {
                            buildLocAnim(event)
                        },
                    )
                }
                is LocDel -> {
                    container.publish(
                        format(2, "LocDel") {
                            buildLocDel(event)
                        },
                    )
                }
                is LocMerge -> {
                    container.publish(
                        format(2, "LocMerge") {
                            buildLocMerge(event)
                        },
                    )
                    container.publish(
                        format(3, "Target") {
                            property("player", player(event.index))
                        },
                    )
                }
                is MapAnim -> {
                    container.publish(
                        format(2, "MapAnim") {
                            buildMapAnim(event)
                        },
                    )
                }
                is MapProjAnim -> {
                    container.publish(
                        format(2, "MapProjAnim") {
                            buildMapProjAnim(event)
                        },
                    )
                    container.publish(
                        format(3, "Source") {
                            if (event.sourceIndex != 0) {
                                property("source", formatActor(event.sourceIndex))
                            }
                            property("coord", coordInZone(event.xInZone, event.zInZone))
                        },
                    )
                    container.publish(
                        format(3, "Target") {
                            property(
                                "coord",
                                coordInZone(
                                    event.xInZone + event.deltaX,
                                    event.zInZone + event.deltaZ,
                                ),
                            )
                            if (event.targetIndex != 0) {
                                property("target", formatActor(event.targetIndex))
                            }
                        },
                    )
                }
                is ObjAdd -> {
                    container.publish(
                        format(2, "ObjAdd") {
                            buildObjAdd(event)
                        },
                    )
                }
                is ObjCount -> {
                    container.publish(
                        format(2, "ObjCount") {
                            buildObjCount(event)
                        },
                    )
                }
                is ObjDel -> {
                    container.publish(
                        format(2, "ObjDel") {
                            buildObjDel(event)
                        },
                    )
                }
                is ObjEnabledOps -> {
                    container.publish(
                        format(2, "ObjEnabledOps") {
                            buildObjEnabledOps(event)
                        },
                    )
                }
                is SoundArea -> {
                    container.publish(
                        format(2, "SoundArea") {
                            buildSoundArea(event)
                        },
                    )
                }
            }
        }
    }

    override fun updateZonePartialFollows(message: UpdateZonePartialFollows) {
        stateTracker.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
        publish {
            property("coord", buildAreaCoord(message.zoneX, message.zoneZ, message.level))
        }
    }

    override fun locAddChange(message: LocAddChange) {
        publish {
            buildLocAddChange(message)
        }
    }

    override fun locAnim(message: LocAnim) {
        publish {
            buildLocAnim(message)
        }
    }

    override fun locDel(message: LocDel) {
        publish {
            buildLocDel(message)
        }
    }

    override fun locMerge(message: LocMerge) {
        publish {
            buildLocMerge(message)
        }
        container.publish(
            format(3, "Target") {
                property("player", player(message.index))
            },
        )
    }

    override fun mapAnim(message: MapAnim) {
        publish {
            buildMapAnim(message)
        }
    }

    override fun mapProjAnim(message: MapProjAnim) {
        publish {
            buildMapProjAnim(message)
        }
        // TODO: multiplier
        container.publish(
            format(3, "Source") {
                if (message.sourceIndex != 0) {
                    property("source", formatActor(message.sourceIndex))
                }
                property("coord", coordInZone(message.xInZone, message.zInZone))
            },
        )
        container.publish(
            format(3, "Target") {
                property(
                    "coord",
                    coordInZone(
                        message.xInZone + message.deltaX,
                        message.zInZone + message.deltaZ,
                    ),
                )
                if (message.targetIndex != 0) {
                    property("target", formatActor(message.targetIndex))
                }
            },
        )
    }

    override fun objAdd(message: ObjAdd) {
        publish {
            buildObjAdd(message)
        }
    }

    override fun objCount(message: ObjCount) {
        publish {
            buildObjCount(message)
        }
    }

    override fun objDel(message: ObjDel) {
        publish {
            buildObjDel(message)
        }
    }

    override fun objEnabledOps(message: ObjEnabledOps) {
        publish {
            buildObjEnabledOps(message)
        }
    }

    override fun soundArea(message: SoundArea) {
        publish {
            buildSoundArea(message)
        }
    }

    private fun coordInZone(
        xInZone: Int,
        zInZone: Int,
    ): CoordGrid {
        return stateTracker.getActiveWorld().relativizeZoneCoord(xInZone, zInZone)
    }

    public fun PropertyBuilder.buildLocAddChange(message: LocAddChange) {
        property(
            "loc",
            loc(
                message.id,
                coordInZone(message.xInZone, message.zInZone),
                message.shape,
                message.rotation,
            ),
        )
    }

    public fun PropertyBuilder.buildLocAnim(message: LocAnim) {
        property("loc", loc(coordInZone(message.xInZone, message.zInZone), message.shape, message.rotation))
        property("anim", formatter.type(ScriptVarType.SEQ, message.id))
    }

    public fun PropertyBuilder.buildLocDel(message: LocDel) {
        property("loc", loc(coordInZone(message.xInZone, message.zInZone), message.shape, message.rotation))
    }

    public fun PropertyBuilder.buildLocMerge(message: LocMerge) {
        property(
            "loc",
            loc(
                message.id,
                coordInZone(message.xInZone, message.zInZone),
                message.shape,
                message.rotation,
            ),
        )
        property("start", message.start)
        property("end", message.end)
        property("minx", message.minX)
        property("maxx", message.maxX)
        property("minz", message.minZ)
        property("maxz", message.maxZ)
    }

    public fun PropertyBuilder.buildMapAnim(message: MapAnim) {
        property("id", formatter.type(ScriptVarType.SPOTANIM, message.id))
        filteredProperty("delay", message.delay) { it != 0 }
        filteredProperty("height", message.height) { it != 0 }
        property("coord", coordInZone(message.xInZone, message.zInZone))
    }

    public fun PropertyBuilder.buildMapProjAnim(message: MapProjAnim) {
        property("id", formatter.type(ScriptVarType.SPOTANIM, message.id))
        property("start", message.startTime)
        property("end", message.endTime)
        property("angle", message.angle)
        property("progress", message.progress)
        property("startheight", message.startHeight)
        property("endheight", message.endHeight)
    }

    private fun getOwnershipType(id: Int): String {
        return when (id) {
            0 -> "None"
            1 -> "Self"
            2 -> "Other"
            3 -> "GIM"
            else -> id.toString()
        }
    }

    public fun PropertyBuilder.buildObjAdd(message: ObjAdd) {
        property("id", formatter.type(ScriptVarType.OBJ, message.id))
        property("count", message.quantity.format())
        filteredProperty("opflags", "0b" + message.opFlags.value.toString(2)) { it != "0b11111" }
        filteredProperty("reveal", message.timeUntilPublic) { it != 0 }
        filteredProperty("despawn", message.timeUntilDespawn) { it != 0 }
        filteredProperty("ownership", getOwnershipType(message.ownershipType)) { it != "None" }
        property("neverturnpublic", message.neverBecomesPublic)
        property("coord", coordInZone(message.xInZone, message.zInZone))
    }

    public fun PropertyBuilder.buildObjCount(message: ObjCount) {
        property("id", formatter.type(ScriptVarType.OBJ, message.id))
        property("oldcount", message.oldQuantity.format())
        property("newcount", message.newQuantity.format())
        property("coord", coordInZone(message.xInZone, message.zInZone))
    }

    public fun PropertyBuilder.buildObjDel(message: ObjDel) {
        property("id", formatter.type(ScriptVarType.OBJ, message.id))
        property("count", message.quantity.format())
        property("coord", coordInZone(message.xInZone, message.zInZone))
    }

    public fun PropertyBuilder.buildObjEnabledOps(message: ObjEnabledOps) {
        property("id", formatter.type(ScriptVarType.OBJ, message.id))
        property("opflags", "0b" + message.opFlags.value.toString(2))
        property("coord", coordInZone(message.xInZone, message.zInZone))
    }

    public fun PropertyBuilder.buildSoundArea(message: SoundArea) {
        property("id", formatter.type(ScriptVarType.SYNTH, message.id))
        filteredProperty("loops", message.loops) { it != 0 }
        filteredProperty("delay", message.delay) { it != 0 }
        property("range", message.radius)
        filteredProperty("size", message.size) { it != 0 }
        property("coord", coordInZone(message.xInZone, message.zInZone))
    }
}
