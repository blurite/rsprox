package net.rsprox.transcriber.base

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
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.SetNpcUpdateOrigin
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
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.ScriptVarType
import net.rsprox.transcriber.ScriptVarType.SEQ
import net.rsprox.transcriber.ScriptVarType.TEXTURE
import net.rsprox.transcriber.ServerPacketTranscriber
import net.rsprox.transcriber.indent
import net.rsprox.transcriber.properties.Property
import net.rsprox.transcriber.properties.PropertyBuilder
import net.rsprox.transcriber.properties.properties
import net.rsprox.transcriber.quote
import net.rsprox.transcriber.state.Player
import net.rsprox.transcriber.state.StateTracker

public open class BaseServerPacketTranscriber(
    private val formatter: BaseMessageFormatter,
    private val container: MessageConsumerContainer,
    private val stateTracker: StateTracker,
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

    private fun format(
        indentation: Int,
        properties: List<Property>,
    ): String {
        return format(
            indentation,
            "",
            properties,
        )
    }

    private fun actor(ambiguousIndex: Int): List<Property> {
        return properties {
            if (ambiguousIndex == -1 || ambiguousIndex == 0xFFFFFF) {
                property("index", -1)
                return@properties
            }
            if (ambiguousIndex > 0xFFFF) {
                val index = ambiguousIndex - 0xFFFF
                property("index", index)
                val name = stateTracker.getLastKnownPlayerName(index)
                if (name != null) {
                    property("player", name.quote())
                }
            } else {
                property("index", ambiguousIndex)
            }
        }
    }

    override fun camLookAt(message: CamLookAt) {
        TODO("Not yet implemented")
    }

    override fun camLookAtEasedCoord(message: CamLookAtEasedCoord) {
        TODO("Not yet implemented")
    }

    override fun camMode(message: CamMode) {
        TODO("Not yet implemented")
    }

    override fun camMoveTo(message: CamMoveTo) {
        TODO("Not yet implemented")
    }

    override fun camMoveToArc(message: CamMoveToArc) {
        TODO("Not yet implemented")
    }

    override fun camMoveToCycles(message: CamMoveToCycles) {
        TODO("Not yet implemented")
    }

    override fun camReset(message: CamReset) {
        TODO("Not yet implemented")
    }

    override fun camRotateBy(message: CamRotateBy) {
        TODO("Not yet implemented")
    }

    override fun camRotateTo(message: CamRotateTo) {
        TODO("Not yet implemented")
    }

    override fun camShake(message: CamShake) {
        TODO("Not yet implemented")
    }

    override fun camSmoothReset(message: CamSmoothReset) {
        TODO("Not yet implemented")
    }

    override fun camTarget(message: CamTarget) {
        TODO("Not yet implemented")
    }

    override fun camTargetOld(message: CamTargetOld) {
        TODO("Not yet implemented")
    }

    override fun oculusSync(message: OculusSync) {
        TODO("Not yet implemented")
    }

    override fun clanChannelDelta(message: ClanChannelDelta) {
        TODO("Not yet implemented")
    }

    override fun clanChannelFull(message: ClanChannelFull) {
        TODO("Not yet implemented")
    }

    override fun clanSettingsDelta(message: ClanSettingsDelta) {
        TODO("Not yet implemented")
    }

    override fun clanSettingsFull(message: ClanSettingsFull) {
        TODO("Not yet implemented")
    }

    override fun messageClanChannel(message: MessageClanChannel) {
        TODO("Not yet implemented")
    }

    override fun messageClanChannelSystem(message: MessageClanChannelSystem) {
        TODO("Not yet implemented")
    }

    override fun varClan(message: VarClan) {
        TODO("Not yet implemented")
    }

    override fun varClanDisable(message: VarClanDisable) {
        TODO("Not yet implemented")
    }

    override fun varClanEnable(message: VarClanEnable) {
        TODO("Not yet implemented")
    }

    override fun messageFriendChannel(message: MessageFriendChannel) {
        TODO("Not yet implemented")
    }

    override fun updateFriendChatChannelFullV1(message: UpdateFriendChatChannelFullV1) {
        TODO("Not yet implemented")
    }

    override fun updateFriendChatChannelFullV2(message: UpdateFriendChatChannelFullV2) {
        TODO("Not yet implemented")
    }

    override fun updateFriendChatChannelSingleUser(message: UpdateFriendChatChannelSingleUser) {
        TODO("Not yet implemented")
    }

    override fun npcInfo(message: NpcInfo) {
        TODO("Not yet implemented")
    }

    override fun setNpcUpdateOrigin(message: SetNpcUpdateOrigin) {
        TODO("Not yet implemented")
    }

    private fun publishProt() {
        container.publish("[${stateTracker.currentProt}]".indent(1))
    }

    override fun playerInfo(message: PlayerInfo) {
        TODO("Disabled")
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
                            property("id", formatter.type(SEQ, info.id.maxUShortToMinusOne()))
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
                    lines += format(3, "Spotanims")
                    for ((slot, spotanim) in info.spotanims) {
                        lines +=
                            format(4, "Spotanim") {
                                property("slot", slot)
                                property("id", formatter.type(ScriptVarType.SPOTANIM, spotanim.id))
                                property("delay", spotanim.delay)
                                property("height", spotanim.height)
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
                            property("ready", formatter.type(SEQ, info.readyAnim.maxUShortToMinusOne()))
                            property("turn", formatter.type(SEQ, info.turnAnim.maxUShortToMinusOne()))
                            property("walk", formatter.type(SEQ, info.walkAnim.maxUShortToMinusOne()))
                            property("walkback", formatter.type(SEQ, info.walkAnimBack.maxUShortToMinusOne()))
                            property("walkleft", formatter.type(SEQ, info.walkAnimLeft.maxUShortToMinusOne()))
                            property("walkright", formatter.type(SEQ, info.walkAnimRight.maxUShortToMinusOne()))
                            property("run", formatter.type(SEQ, info.runAnim.maxUShortToMinusOne()))
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
                                        property("retex$retexIndex1", formatter.type(TEXTURE, cus.retex1))
                                    }
                                    if (retexIndex2 != 0xF) {
                                        property("retex$retexIndex2", formatter.type(TEXTURE, cus.retex2))
                                    }
                                }
                            }
                        }
                }
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
        TODO("Not yet implemented")
    }

    override fun ifClearInv(message: IfClearInv) {
        TODO("Not yet implemented")
    }

    override fun ifCloseSub(message: IfCloseSub) {
        TODO("Not yet implemented")
    }

    override fun ifMoveSub(message: IfMoveSub) {
        TODO("Not yet implemented")
    }

    override fun ifOpenSub(message: IfOpenSub) {
        TODO("Not yet implemented")
    }

    override fun ifOpenTop(message: IfOpenTop) {
        TODO("Not yet implemented")
    }

    override fun ifResync(message: IfResync) {
        TODO("Not yet implemented")
    }

    override fun ifSetAngle(message: IfSetAngle) {
        TODO("Not yet implemented")
    }

    override fun ifSetAnim(message: IfSetAnim) {
        TODO("Not yet implemented")
    }

    override fun ifSetColour(message: IfSetColour) {
        TODO("Not yet implemented")
    }

    override fun ifSetEvents(message: IfSetEvents) {
        TODO("Not yet implemented")
    }

    override fun ifSetHide(message: IfSetHide) {
        TODO("Not yet implemented")
    }

    override fun ifSetModel(message: IfSetModel) {
        TODO("Not yet implemented")
    }

    override fun ifSetNpcHead(message: IfSetNpcHead) {
        TODO("Not yet implemented")
    }

    override fun ifSetNpcHeadActive(message: IfSetNpcHeadActive) {
        TODO("Not yet implemented")
    }

    override fun ifSetObject(message: IfSetObject) {
        TODO("Not yet implemented")
    }

    override fun ifSetPlayerHead(message: IfSetPlayerHead) {
        TODO("Not yet implemented")
    }

    override fun ifSetPlayerModelBaseColour(message: IfSetPlayerModelBaseColour) {
        TODO("Not yet implemented")
    }

    override fun ifSetPlayerModelBodyType(message: IfSetPlayerModelBodyType) {
        TODO("Not yet implemented")
    }

    override fun ifSetPlayerModelObj(message: IfSetPlayerModelObj) {
        TODO("Not yet implemented")
    }

    override fun ifSetPlayerModelSelf(message: IfSetPlayerModelSelf) {
        TODO("Not yet implemented")
    }

    override fun ifSetPosition(message: IfSetPosition) {
        TODO("Not yet implemented")
    }

    override fun ifSetRotateSpeed(message: IfSetRotateSpeed) {
        TODO("Not yet implemented")
    }

    override fun ifSetScrollPos(message: IfSetScrollPos) {
        TODO("Not yet implemented")
    }

    override fun ifSetText(message: IfSetText) {
        TODO("Not yet implemented")
    }

    override fun updateInvFull(message: UpdateInvFull) {
        TODO("Not yet implemented")
    }

    override fun updateInvPartial(message: UpdateInvPartial) {
        TODO("Not yet implemented")
    }

    override fun updateInvStopTransmit(message: UpdateInvStopTransmit) {
        TODO("Not yet implemented")
    }

    override fun logout(message: Logout) {
        TODO("Not yet implemented")
    }

    override fun logoutTransfer(message: LogoutTransfer) {
        TODO("Not yet implemented")
    }

    override fun logoutWithReason(message: LogoutWithReason) {
        TODO("Not yet implemented")
    }

    override fun rebuildLogin(message: RebuildLogin) {
        stateTracker.overridePlayer(
            Player(
                message.playerInfoInitBlock.localPlayerIndex,
                "uninitialized",
                message.playerInfoInitBlock.localPlayerCoord,
            ),
        )
        stateTracker.localPlayerIndex = message.playerInfoInitBlock.localPlayerIndex
        TODO("Not yet implemented")
    }

    override fun rebuildNormal(message: RebuildNormal) {
        TODO("Not yet implemented")
    }

    override fun rebuildRegion(message: RebuildRegion) {
        TODO("Not yet implemented")
    }

    override fun rebuildWorldEntity(message: RebuildWorldEntity) {
        TODO("Not yet implemented")
    }

    override fun hideLocOps(message: HideLocOps) {
        TODO("Not yet implemented")
    }

    override fun hideNpcOps(message: HideNpcOps) {
        TODO("Not yet implemented")
    }

    override fun hidePlayerOps(message: HidePlayerOps) {
        TODO("Not yet implemented")
    }

    override fun hintArrow(message: HintArrow) {
        TODO("Not yet implemented")
    }

    override fun hiscoreReply(message: HiscoreReply) {
        TODO("Not yet implemented")
    }

    override fun minimapToggle(message: MinimapToggle) {
        TODO("Not yet implemented")
    }

    override fun reflectionChecker(message: ReflectionChecker) {
        TODO("Not yet implemented")
    }

    override fun resetAnims(message: ResetAnims) {
        TODO("Not yet implemented")
    }

    override fun sendPing(message: SendPing) {
        TODO("Not yet implemented")
    }

    override fun serverTickEnd(message: ServerTickEnd) {
        stateTracker.incrementCycle()
        container.publish("[${stateTracker.cycle}]".indent(0))
        TODO("Not yet implemented")
    }

    override fun setHeatmapEnabled(message: SetHeatmapEnabled) {
        TODO("Not yet implemented")
    }

    override fun siteSettings(message: SiteSettings) {
        TODO("Not yet implemented")
    }

    override fun updateRebootTimer(message: UpdateRebootTimer) {
        TODO("Not yet implemented")
    }

    override fun updateUid192(message: UpdateUid192) {
        TODO("Not yet implemented")
    }

    override fun urlOpen(message: UrlOpen) {
        TODO("Not yet implemented")
    }

    override fun chatFilterSettings(message: ChatFilterSettings) {
        TODO("Not yet implemented")
    }

    override fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat) {
        TODO("Not yet implemented")
    }

    override fun messageGame(message: MessageGame) {
        TODO("Not yet implemented")
    }

    override fun runClientScript(message: RunClientScript) {
        TODO("Not yet implemented")
    }

    override fun setMapFlag(message: SetMapFlag) {
        TODO("Not yet implemented")
    }

    override fun setPlayerOp(message: SetPlayerOp) {
        TODO("Not yet implemented")
    }

    override fun triggerOnDialogAbort(message: TriggerOnDialogAbort) {
        TODO("Not yet implemented")
    }

    override fun updateRunEnergy(message: UpdateRunEnergy) {
        TODO("Not yet implemented")
    }

    override fun updateRunWeight(message: UpdateRunWeight) {
        TODO("Not yet implemented")
    }

    override fun updateStat(message: UpdateStat) {
        TODO("Not yet implemented")
    }

    override fun updateStatOld(message: UpdateStatOld) {
        TODO("Not yet implemented")
    }

    override fun updateStockMarketSlot(message: UpdateStockMarketSlot) {
        TODO("Not yet implemented")
    }

    override fun updateTradingPost(message: UpdateTradingPost) {
        TODO("Not yet implemented")
    }

    override fun friendListLoaded(message: FriendListLoaded) {
        TODO("Not yet implemented")
    }

    override fun messagePrivate(message: MessagePrivate) {
        TODO("Not yet implemented")
    }

    override fun messagePrivateEcho(message: MessagePrivateEcho) {
        TODO("Not yet implemented")
    }

    override fun updateFriendList(message: UpdateFriendList) {
        TODO("Not yet implemented")
    }

    override fun updateIgnoreList(message: UpdateIgnoreList) {
        TODO("Not yet implemented")
    }

    override fun midiJingle(message: MidiJingle) {
        TODO("Not yet implemented")
    }

    override fun midiSong(message: MidiSong) {
        TODO("Not yet implemented")
    }

    override fun midiSongOld(message: MidiSongOld) {
        TODO("Not yet implemented")
    }

    override fun midiSongStop(message: MidiSongStop) {
        TODO("Not yet implemented")
    }

    override fun midiSongWithSecondary(message: MidiSongWithSecondary) {
        TODO("Not yet implemented")
    }

    override fun midiSwap(message: MidiSwap) {
        TODO("Not yet implemented")
    }

    override fun synthSound(message: SynthSound) {
        TODO("Not yet implemented")
    }

    override fun locAnimSpecific(message: LocAnimSpecific) {
        TODO("Not yet implemented")
    }

    override fun mapAnimSpecific(message: MapAnimSpecific) {
        TODO("Not yet implemented")
    }

    override fun npcAnimSpecific(message: NpcAnimSpecific) {
        TODO("Not yet implemented")
    }

    override fun npcHeadIconSpecific(message: NpcHeadIconSpecific) {
        TODO("Not yet implemented")
    }

    override fun npcSpotAnimSpecific(message: NpcSpotAnimSpecific) {
        TODO("Not yet implemented")
    }

    override fun playerAnimSpecific(message: PlayerAnimSpecific) {
        TODO("Not yet implemented")
    }

    override fun playerSpotAnimSpecific(message: PlayerSpotAnimSpecific) {
        TODO("Not yet implemented")
    }

    override fun projAnimSpecific(message: ProjAnimSpecific) {
        TODO("Not yet implemented")
    }

    override fun varpLarge(message: VarpLarge) {
        TODO("Not yet implemented")
    }

    override fun varpReset(message: VarpReset) {
        TODO("Not yet implemented")
    }

    override fun varpSmall(message: VarpSmall) {
        TODO("Not yet implemented")
    }

    override fun varpSync(message: VarpSync) {
        TODO("Not yet implemented")
    }

    override fun clearEntities(message: ClearEntities) {
        TODO("Not yet implemented")
    }

    override fun setActiveWorld(message: SetActiveWorld) {
        TODO("Not yet implemented")
    }

    override fun updateZoneFullFollows(message: UpdateZoneFullFollows) {
        TODO("Not yet implemented")
    }

    override fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed) {
        TODO("Not yet implemented")
    }

    override fun updateZonePartialFollows(message: UpdateZonePartialFollows) {
        TODO("Not yet implemented")
    }

    override fun locAddChange(message: LocAddChange) {
        TODO("Not yet implemented")
    }

    override fun locAnim(message: LocAnim) {
        TODO("Not yet implemented")
    }

    override fun locDel(message: LocDel) {
        TODO("Not yet implemented")
    }

    override fun locMerge(message: LocMerge) {
        TODO("Not yet implemented")
    }

    override fun mapAnim(message: MapAnim) {
        TODO("Not yet implemented")
    }

    override fun mapProjAnim(message: MapProjAnim) {
        TODO("Not yet implemented")
    }

    override fun objAdd(message: ObjAdd) {
        TODO("Not yet implemented")
    }

    override fun objCount(message: ObjCount) {
        TODO("Not yet implemented")
    }

    override fun objDel(message: ObjDel) {
        TODO("Not yet implemented")
    }

    override fun objEnabledOps(message: ObjEnabledOps) {
        TODO("Not yet implemented")
    }

    override fun soundArea(message: SoundArea) {
        TODO("Not yet implemented")
    }
}
