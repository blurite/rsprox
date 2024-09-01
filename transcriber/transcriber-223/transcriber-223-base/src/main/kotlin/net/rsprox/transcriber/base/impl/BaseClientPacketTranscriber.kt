package net.rsprox.transcriber.base.impl

import net.rsprox.cache.api.Cache
import net.rsprox.protocol.game.incoming.model.buttons.If1Button
import net.rsprox.protocol.game.incoming.model.buttons.If3Button
import net.rsprox.protocol.game.incoming.model.buttons.IfButtonD
import net.rsprox.protocol.game.incoming.model.buttons.IfButtonT
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsAddBannedFromChannel
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsSetMutedFromChannel
import net.rsprox.protocol.game.incoming.model.clan.ClanChannelFullRequest
import net.rsprox.protocol.game.incoming.model.clan.ClanChannelKickUser
import net.rsprox.protocol.game.incoming.model.clan.ClanSettingsFullRequest
import net.rsprox.protocol.game.incoming.model.events.EventAppletFocus
import net.rsprox.protocol.game.incoming.model.events.EventCameraPosition
import net.rsprox.protocol.game.incoming.model.events.EventKeyboard
import net.rsprox.protocol.game.incoming.model.events.EventMouseClick
import net.rsprox.protocol.game.incoming.model.events.EventMouseMove
import net.rsprox.protocol.game.incoming.model.events.EventMouseScroll
import net.rsprox.protocol.game.incoming.model.events.EventNativeMouseClick
import net.rsprox.protocol.game.incoming.model.events.EventNativeMouseMove
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatJoinLeave
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatKick
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatSetRank
import net.rsprox.protocol.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.game.incoming.model.locs.OpLoc6
import net.rsprox.protocol.game.incoming.model.locs.OpLocT
import net.rsprox.protocol.game.incoming.model.messaging.MessagePrivate
import net.rsprox.protocol.game.incoming.model.messaging.MessagePublic
import net.rsprox.protocol.game.incoming.model.misc.client.ConnectionTelemetry
import net.rsprox.protocol.game.incoming.model.misc.client.DetectModifiedClient
import net.rsprox.protocol.game.incoming.model.misc.client.Idle
import net.rsprox.protocol.game.incoming.model.misc.client.MapBuildComplete
import net.rsprox.protocol.game.incoming.model.misc.client.MembershipPromotionEligibility
import net.rsprox.protocol.game.incoming.model.misc.client.NoTimeout
import net.rsprox.protocol.game.incoming.model.misc.client.ReflectionCheckReply
import net.rsprox.protocol.game.incoming.model.misc.client.SendPingReply
import net.rsprox.protocol.game.incoming.model.misc.client.SoundJingleEnd
import net.rsprox.protocol.game.incoming.model.misc.client.WindowStatus
import net.rsprox.protocol.game.incoming.model.misc.user.BugReport
import net.rsprox.protocol.game.incoming.model.misc.user.ClickWorldMap
import net.rsprox.protocol.game.incoming.model.misc.user.ClientCheat
import net.rsprox.protocol.game.incoming.model.misc.user.CloseModal
import net.rsprox.protocol.game.incoming.model.misc.user.HiscoreRequest
import net.rsprox.protocol.game.incoming.model.misc.user.IfCrmViewClick
import net.rsprox.protocol.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.game.incoming.model.misc.user.MoveMinimapClick
import net.rsprox.protocol.game.incoming.model.misc.user.OculusLeave
import net.rsprox.protocol.game.incoming.model.misc.user.SendSnapshot
import net.rsprox.protocol.game.incoming.model.misc.user.SetChatFilterSettings
import net.rsprox.protocol.game.incoming.model.misc.user.Teleport
import net.rsprox.protocol.game.incoming.model.misc.user.UpdatePlayerModel
import net.rsprox.protocol.game.incoming.model.npcs.OpNpc
import net.rsprox.protocol.game.incoming.model.npcs.OpNpc6
import net.rsprox.protocol.game.incoming.model.npcs.OpNpcT
import net.rsprox.protocol.game.incoming.model.objs.OpObj
import net.rsprox.protocol.game.incoming.model.objs.OpObj6
import net.rsprox.protocol.game.incoming.model.objs.OpObjT
import net.rsprox.protocol.game.incoming.model.players.OpPlayer
import net.rsprox.protocol.game.incoming.model.players.OpPlayerT
import net.rsprox.protocol.game.incoming.model.resumed.ResumePCountDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePNameDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePObjDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePStringDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePauseButton
import net.rsprox.protocol.game.incoming.model.social.FriendListAdd
import net.rsprox.protocol.game.incoming.model.social.FriendListDel
import net.rsprox.protocol.game.incoming.model.social.IgnoreListAdd
import net.rsprox.protocol.game.incoming.model.social.IgnoreListDel
import net.rsprox.protocol.reflection.ReflectionCheck
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.NamedEnum
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.com
import net.rsprox.shared.property.coordGrid
import net.rsprox.shared.property.filteredBoolean
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.filteredNamedEnum
import net.rsprox.shared.property.filteredScriptVarType
import net.rsprox.shared.property.filteredString
import net.rsprox.shared.property.formattedInt
import net.rsprox.shared.property.group
import net.rsprox.shared.property.identifiedMultinpc
import net.rsprox.shared.property.identifiedNpc
import net.rsprox.shared.property.identifiedPlayer
import net.rsprox.shared.property.int
import net.rsprox.shared.property.long
import net.rsprox.shared.property.namedEnum
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.string
import net.rsprox.shared.property.unidentifiedNpc
import net.rsprox.shared.property.unidentifiedPlayer
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.impl.ClientPacketTranscriber
import net.rsprox.transcriber.state.StateTracker
import java.awt.event.KeyEvent
import java.text.DecimalFormat
import java.text.NumberFormat

@Suppress("SpellCheckingInspection", "DuplicatedCode")
public open class BaseClientPacketTranscriber(
    private val stateTracker: StateTracker,
    private val cache: Cache,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : ClientPacketTranscriber {
    private val root: RootProperty
        get() = checkNotNull(stateTracker.root.last())
    private val filters: PropertyFilterSet
        get() = filterSetStore.getActive()
    private val settings: SettingSet
        get() = settingSetStore.getActive()

    private fun omit() {
        stateTracker.deleteRoot()
    }

    private fun Property.npc(index: Int): ChildProperty<*> {
        val world = stateTracker.getActiveWorld()
        val npc = world.getNpcOrNull(index) ?: return unidentifiedNpc(index)
        val finalIndex =
            if (filters[PropertyFilter.NPC_OMIT_INDEX]) {
                Int.MIN_VALUE
            } else {
                index
            }
        val multinpc = stateTracker.resolveMultinpc(npc.id, cache)
        return if (multinpc != null) {
            identifiedMultinpc(
                finalIndex,
                npc.id,
                multinpc.id,
                multinpc.name,
                npc.coord.level,
                npc.coord.x,
                npc.coord.z,
            )
        } else {
            identifiedNpc(
                finalIndex,
                npc.id,
                npc.name ?: "null",
                npc.coord.level,
                npc.coord.x,
                npc.coord.z,
            )
        }
    }

    private fun Property.player(index: Int): ChildProperty<*> {
        val npc = stateTracker.getPlayerOrNull(index)
        val finalIndex =
            if (settings[Setting.PLAYER_HIDE_INDEX]) {
                Int.MIN_VALUE
            } else {
                index
            }
        return if (npc != null) {
            identifiedPlayer(
                finalIndex,
                npc.name,
                npc.coord.level,
                npc.coord.x,
                npc.coord.z,
            )
        } else {
            unidentifiedPlayer(index)
        }
    }

    override fun if1Button(message: If1Button) {
        if (!filters[PropertyFilter.IF_BUTTON]) return omit()
        root.com(message.interfaceId, message.componentId)
    }

    override fun if3Button(message: If3Button) {
        if (!filters[PropertyFilter.IF_BUTTON]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.filteredInt("sub", message.sub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.obj, -1)
    }

    override fun ifButtonD(message: IfButtonD) {
        if (!filters[PropertyFilter.IF_BUTTOND]) return omit()
        root.com("selectcom", message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("selectedsub", message.selectedSub, -1)
        root.filteredScriptVarType("selectedobj", ScriptVarType.OBJ, message.selectedObj, -1)
        root.com("targetcom", message.targetInterfaceId, message.targetComponentId)
        root.filteredInt("targetsub", message.targetSub, -1)
        root.filteredScriptVarType("targetobj", ScriptVarType.OBJ, message.targetObj, -1)
    }

    override fun ifButtonT(message: IfButtonT) {
        if (!filters[PropertyFilter.IF_BUTTONT]) return omit()
        root.com("selectcom", message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("selectedsub", message.selectedSub, -1)
        root.filteredScriptVarType("selectedobj", ScriptVarType.OBJ, message.selectedObj, -1)
        root.com("targetcom", message.targetInterfaceId, message.targetComponentId)
        root.filteredInt("targetsub", message.targetSub, -1)
        root.filteredScriptVarType("targetobj", ScriptVarType.OBJ, message.targetObj, -1)
    }

    override fun affinedClanSettingsAddBannedFromChannel(message: AffinedClanSettingsAddBannedFromChannel) {
        if (!filters[PropertyFilter.AFFINEDCLANSETTINGS_ADDBANNED_FROMCHANNEL]) return omit()
        root.string("name", message.name)
        root.int("clanid", message.clanId)
        root.int("memberindex", message.memberIndex)
    }

    override fun affinedClanSettingsSetMutedFromChannel(message: AffinedClanSettingsSetMutedFromChannel) {
        if (!filters[PropertyFilter.AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL]) return omit()
        root.string("name", message.name)
        root.int("clanid", message.clanId)
        root.int("memberindex", message.memberIndex)
        root.boolean("muted", message.muted)
    }

    override fun clanChannelFullRequest(message: ClanChannelFullRequest) {
        if (!filters[PropertyFilter.CLANCHANNEL_FULL_REQUEST]) return omit()
        root.int("clanid", message.clanId)
    }

    override fun clanChannelKickUser(message: ClanChannelKickUser) {
        if (!filters[PropertyFilter.CLANCHANNEL_KICKUSER]) return omit()
        root.string("name", message.name)
        root.int("clanid", message.clanId)
        root.int("memberindex", message.memberIndex)
    }

    override fun clanSettingsFullRequest(message: ClanSettingsFullRequest) {
        if (!filters[PropertyFilter.CLANSETTINGS_FULL_REQUEST]) return omit()
        root.int("clanid", message.clanId)
    }

    override fun eventAppletFocus(message: EventAppletFocus) {
        if (!filters[PropertyFilter.EVENT_APPLET_FOCUS]) return omit()
        root.boolean("infocus", message.inFocus)
    }

    override fun eventCameraPosition(message: EventCameraPosition) {
        if (!filters[PropertyFilter.EVENT_CAMERA_POSITION]) return omit()
        root.int("anglex", message.angleX)
        root.int("angley", message.angleY)
    }

    override fun eventKeyboard(message: EventKeyboard) {
        if (!filters[PropertyFilter.EVENT_KEYBOARD]) return omit()
        root.formattedInt("lasttransmitted", message.lastTransmittedKeyPress, MS_NUMBER_FORMAT)
        root.string(
            "keys",
            message
                .keysPressed
                .toAwtKeyCodeIntArray()
                .joinToString(prefix = "[", postfix = "]") { KeyEvent.getKeyText(it) },
        )
    }

    override fun eventMouseClick(message: EventMouseClick) {
        if (!filters[PropertyFilter.EVENT_MOUSE_CLICK]) return omit()
        root.formattedInt("lasttransmitted", message.lastTransmittedMouseClick, MS_NUMBER_FORMAT)
        root.int("x", message.x)
        root.int("y", message.y)
        root.boolean("rightclick", message.rightClick)
    }

    override fun eventMouseMove(message: EventMouseMove) {
        if (!filters[PropertyFilter.EVENT_MOUSE_MOVE]) return omit()
        root.formattedInt("averagetime", message.averageTime, MS_NUMBER_FORMAT)
        root.formattedInt("remainingtime", message.remainingTime, MS_NUMBER_FORMAT)
        root.group("MOVEMENTS") {
            for (index in message.movements.asLongArray().indices) {
                group {
                    val movement = message.movements.getMousePosChange(index)
                    formattedInt("deltatime", movement.timeDelta, MS_NUMBER_FORMAT)
                    int("deltax", movement.xDelta)
                    int("deltay", movement.yDelta)
                }
            }
        }
    }

    override fun eventMouseScroll(message: EventMouseScroll) {
        if (!filters[PropertyFilter.EVENT_MOUSE_SCROLL]) return omit()
        root.int("rotation", message.mouseWheelRotation)
    }

    override fun eventNativeMouseClick(message: EventNativeMouseClick) {
        if (!filters[PropertyFilter.EVENT_NATIVE_MOUSE_CLICK]) return omit()
        root.formattedInt("lasttransmitted", message.lastTransmittedMouseClick, MS_NUMBER_FORMAT)
        root.int("x", message.x)
        root.int("y", message.y)
        root.int("code", message.code)
    }

    override fun eventNativeMouseMove(message: EventNativeMouseMove) {
        if (!filters[PropertyFilter.EVENT_NATIVE_MOUSE_MOVE]) return omit()
        root.formattedInt("averagetime", message.averageTime, MS_NUMBER_FORMAT)
        root.formattedInt("remainingtime", message.remainingTime, MS_NUMBER_FORMAT)
        root.group("MOVEMENTS") {
            for (index in message.movements.asLongArray().indices) {
                group("MOVEMENT") {
                    val movement = message.movements.getMousePosChange(index)
                    formattedInt("deltatime", movement.timeDelta, MS_NUMBER_FORMAT)
                    int("deltax", movement.xDelta)
                    int("deltay", movement.yDelta)
                }
            }
        }
    }

    override fun friendChatJoinLeave(message: FriendChatJoinLeave) {
        if (!filters[PropertyFilter.FRIENDCHAT_JOIN_LEAVE]) return omit()
        root.string("name", message.name)
    }

    override fun friendChatKick(message: FriendChatKick) {
        if (!filters[PropertyFilter.FRIENDCHAT_KICK]) return omit()
        root.string("name", message.name)
    }

    override fun friendChatSetRank(message: FriendChatSetRank) {
        if (!filters[PropertyFilter.FRIENDCHAT_SETRANK]) return omit()
        root.string("name", message.name)
        root.int("rank", message.rank)
    }

    override fun opLoc(message: OpLoc) {
        if (!filters[PropertyFilter.OPLOC]) return omit()
        root.scriptVarType("id", ScriptVarType.LOC, message.id)
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredBoolean("ctrl", message.controlKey)
    }

    override fun opLoc6(message: OpLoc6) {
        if (!filters[PropertyFilter.OPLOC]) return omit()
        root.scriptVarType("id", ScriptVarType.LOC, message.id)
    }

    override fun opLocT(message: OpLocT) {
        if (!filters[PropertyFilter.OPLOCT]) return omit()
        root.scriptVarType("id", ScriptVarType.LOC, message.id)
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredBoolean("ctrl", message.controlKey)
        root.com(message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("sub", message.selectedSub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun messagePrivateClient(message: MessagePrivate) {
        if (!filters[PropertyFilter.MESSAGE_PRIVATE_CLIENT]) return omit()
        root.string("name", message.name)
        root.string("message", message.message)
    }

    override fun messagePublic(message: MessagePublic) {
        if (!filters[PropertyFilter.MESSAGE_PUBLIC]) return omit()
        root.int("type", message.type)
        root.filteredInt("colour", message.colour, 0)
        root.filteredInt("effect", message.effect, 0)
        root.filteredInt("clantype", message.clanType, -1)
        root.string("message", message.message)
        root.filteredString("pattern", message.pattern?.asByteArray()?.contentToString(), null)
    }

    override fun detectModifiedClient(message: DetectModifiedClient) {
        if (!filters[PropertyFilter.DETECT_MODIFIED_CLIENT]) return omit()
        root.formattedInt("code", message.code)
    }

    override fun idle(message: Idle) {
        if (!filters[PropertyFilter.IDLE]) return omit()
    }

    override fun mapBuildComplete(message: MapBuildComplete) {
        if (!filters[PropertyFilter.MAP_BUILD_COMPLETE]) return omit()
    }

    override fun membershipPromotionEligibility(message: MembershipPromotionEligibility) {
        if (!filters[PropertyFilter.MEMBERSHIP_PROMOTION_ELIGIBILITY]) return omit()
        root.int("introductoryprice", message.eligibleForIntroductoryPrice)
        root.int("trialpurchase", message.eligibleForTrialPurchase)
    }

    override fun noTimeout(message: NoTimeout) {
        if (!filters[PropertyFilter.NO_TIMEOUT]) return omit()
    }

    override fun reflectionCheckReply(message: ReflectionCheckReply) {
        if (!filters[PropertyFilter.REFLECTION_CHECK_REPLY]) return omit()
        root.formattedInt("id", message.id)
        for (result in message.result) {
            when (result) {
                is ReflectionCheckReply.ErrorResult<*, *> -> {
                    root.group("Error") {
                        when (val res = result.check) {
                            is ReflectionCheck.GetFieldModifiers -> {
                                group("GET_FIELD_MODIFIERS") {
                                    string("classname", res.className)
                                    string("fieldname", res.fieldName)
                                }
                            }
                            is ReflectionCheck.GetFieldValue -> {
                                group("GET_FIELD_VALUE") {
                                    string("classname", res.className)
                                    string("fieldname", res.fieldName)
                                }
                            }
                            is ReflectionCheck.GetMethodModifiers -> {
                                group("GET_METHOD_MODIFIERS") {
                                    string("classname", res.className)
                                    string("methodname", res.methodName)
                                    string("returnclass", res.returnClass)
                                    string("parameterclasses", res.parameterClasses.toString())
                                }
                            }
                            is ReflectionCheck.InvokeMethod -> {
                                group("INVOKE_METHOD") {
                                    string("classname", res.className)
                                    string("methodname", res.methodName)
                                    string("returnclass", res.returnClass)
                                    string("parameterclasses", res.parameterClasses.toString())
                                    string(
                                        "parametervalues",
                                        res.parameterValues
                                            .map { it.contentToString() }
                                            .toString(),
                                    )
                                }
                            }
                            is ReflectionCheck.SetFieldValue -> {
                                group("SET_FIELD_VALUE") {
                                    string("classname", res.className)
                                    string("fieldname", res.fieldName)
                                    int("value", res.value)
                                }
                            }
                        }
                        string("exceptionclass", result.exceptionClass.toString())
                    }
                }

                is ReflectionCheckReply.GetFieldModifiersResult -> {
                    root.group("GetFieldModifiers") {
                        string("classname", result.check.className)
                        string("fieldname", result.check.fieldName)
                        int("modifiers", result.modifiers)
                    }
                }
                is ReflectionCheckReply.GetFieldValueResult -> {
                    root.group("GetFieldValue") {
                        string("classname", result.check.className)
                        string("fieldname", result.check.fieldName)
                        int("value", result.value)
                    }
                }
                is ReflectionCheckReply.GetMethodModifiersResult -> {
                    root.group("GetMethodModifiers") {
                        string("classname", result.check.className)
                        string("methodname", result.check.methodName)
                        string("returnclass", result.check.returnClass)
                        string("parameterclasses", result.check.parameterClasses.toString())
                        int("modifiers", result.modifiers)
                    }
                }
                is ReflectionCheckReply.InvokeMethodResult<*> -> {
                    root.group("InvokeMethod") {
                        string("classname", result.check.className)
                        string("methodname", result.check.methodName)
                        string("returnclass", result.check.returnClass)
                        string("parameterclasses", result.check.parameterClasses.toString())
                        string(
                            "parametervalues",
                            result.check.parameterValues
                                .map { it.contentToString() }
                                .toString(),
                        )
                        when (val res = result.result) {
                            ReflectionCheckReply.NullReturnValue -> {
                                string("result", null)
                            }
                            is ReflectionCheckReply.NumberReturnValue -> {
                                long("result", res.longValue)
                            }
                            is ReflectionCheckReply.StringReturnValue -> {
                                string("result", res.stringValue)
                            }
                            ReflectionCheckReply.UnknownReturnValue -> {
                                string("result", "unknown")
                            }
                        }
                    }
                }
                is ReflectionCheckReply.SetFieldValueResult -> {
                    root.group("SetFieldValue") {
                        string("classname", result.check.className)
                        string("fieldname", result.check.fieldName)
                        int("value", result.check.value)
                    }
                }
            }
        }
    }

    override fun sendPingReply(message: SendPingReply) {
        if (!filters[PropertyFilter.SEND_PING_REPLY]) return omit()
        root.int("fps", message.fps)
        root.int("gcpercenttime", message.gcPercentTime)
        root.int("value1", message.value1)
        root.int("value2", message.value2)
    }

    override fun soundJingleEnd(message: SoundJingleEnd) {
        if (!filters[PropertyFilter.SOUND_JINGLEEND]) return omit()
        root.scriptVarType("id", ScriptVarType.JINGLE, message.jingleId)
    }

    override fun connectionTelemetry(message: ConnectionTelemetry) {
        if (!filters[PropertyFilter.CONNECTION_TELEMETRY]) return omit()
        root.formattedInt("connectionlostduration", message.connectionLostDuration * 10, MS_NUMBER_FORMAT)
        root.formattedInt("loginduration", message.loginDuration * 10, MS_NUMBER_FORMAT)
        root.int("clientstate", message.clientState)
        root.int("logincount", message.loginCount)
    }

    override fun windowStatus(message: WindowStatus) {
        if (!filters[PropertyFilter.WINDOW_STATUS]) return omit()
        root.int("windowmode", message.windowMode)
        root.int("framewidth", message.frameWidth)
        root.int("frameheight", message.frameHeight)
    }

    override fun bugReport(message: BugReport) {
        if (!filters[PropertyFilter.BUG_REPORT]) return omit()
        root.int("type", message.type)
        root.string("description", message.description)
        root.string("instructions", message.instructions)
    }

    override fun clickWorldMap(message: ClickWorldMap) {
        if (!filters[PropertyFilter.CLICKWORLDMAP]) return omit()
        root.coordGrid(message.level, message.x, message.z)
    }

    override fun clientCheat(message: ClientCheat) {
        if (!filters[PropertyFilter.CLIENT_CHEAT]) return omit()
        root.string("cheat", message.command)
    }

    override fun closeModal(message: CloseModal) {
        if (!filters[PropertyFilter.CLOSE_MODAL]) return omit()
    }

    override fun hiscoreRequest(message: HiscoreRequest) {
        if (!filters[PropertyFilter.HISCORE_REQUEST]) return omit()
        root.int("type", message.type)
        root.int("requestid", message.requestId)
        root.string("name", message.name)
    }

    override fun ifCrmViewClick(message: IfCrmViewClick) {
        if (!filters[PropertyFilter.IF_CRMVIEW_CLICK]) return omit()
        root.int("crmservertarget", message.crmServerTarget)
        root.com(message.interfaceId, message.componentId)
        root.filteredInt("sub", message.sub, -1)
        root.int("payload1", message.behaviour1)
        root.int("payload2", message.behaviour2)
        root.int("payload3", message.behaviour3)
    }

    private enum class MovementKeyCombination(
        override val prettyName: String,
    ) : NamedEnum {
        NONE("none"),
        CTRL("ctrl"),
        CTRLPLUSSHIFT("ctrl+shift"),
    }

    private fun getMovementKeyCombination(id: Int): MovementKeyCombination {
        return when (id) {
            1 -> MovementKeyCombination.CTRL
            2 -> MovementKeyCombination.CTRLPLUSSHIFT
            else -> MovementKeyCombination.NONE
        }
    }

    override fun moveGameClick(message: MoveGameClick) {
        if (!filters[PropertyFilter.MOVE_GAMECLICK]) return omit()
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredNamedEnum(
            "keycombination",
            getMovementKeyCombination(message.keyCombination),
            MovementKeyCombination.NONE,
        )
    }

    override fun moveMinimapClick(message: MoveMinimapClick) {
        if (!filters[PropertyFilter.MOVE_MINIMAPCLICK]) return omit()
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredNamedEnum(
            "keycombination",
            getMovementKeyCombination(message.keyCombination),
            MovementKeyCombination.NONE,
        )
        root.int("minimapwidth", message.minimapWidth)
        root.int("minimapheight", message.minimapHeight)
        root.int("camerangley", message.cameraAngleY)
        root.int("finex", message.fineX)
        root.int("finez", message.fineZ)
    }

    override fun oculusLeave(message: OculusLeave) {
        if (!filters[PropertyFilter.OCULUS_LEAVE]) return omit()
    }

    private fun getRule(id: Int): String {
        return when (id) {
            3 -> "Exploiting a bug"
            4 -> "Staff impersonation"
            5 -> "Buying/selling accounts and services"
            6 -> "Macroing or use of bots"
            7 -> "Boxing in the Deadman Tournament"
            8 -> "Encouraging rule breaking"
            10 -> "Advertising websites"
            11 -> "Muling in the Deadman Tournament"
            12 -> "Asking for or providing contact information"
            14 -> "Scamming"
            15 -> "Seriously offensive language"
            16 -> "Solicitation"
            17 -> "Disruptive behaviour"
            18 -> "Offensive account name"
            19 -> "Real-life threats"
            20 -> "Breaking real-world laws"
            21 -> "Player-run Games of chance"
            else -> id.toString()
        }
    }

    override fun sendSnapshot(message: SendSnapshot) {
        if (!filters[PropertyFilter.SEND_SNAPSHOT]) return omit()
        root.string("name", message.name)
        root.string("rule", getRule(message.ruleId))
        root.filteredBoolean("mute", message.mute)
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

    override fun setChatFilterSettings(message: SetChatFilterSettings) {
        if (!filters[PropertyFilter.CHAT_FILTER_SETTINGS]) return omit()
        root.namedEnum("public", getChatFilter(message.publicChatFilter))
        root.namedEnum("private", getChatFilter(message.privateChatFilter))
        root.namedEnum("trade", getChatFilter(message.tradeChatFilter))
    }

    override fun teleport(message: Teleport) {
        if (!filters[PropertyFilter.TELEPORT]) return omit()
        root.coordGrid(message.level, message.x, message.z)
        root.filteredInt("oculussyncvalue", message.oculusSyncValue, 0)
    }

    override fun updatePlayerModel(message: UpdatePlayerModel) {
        if (!filters[PropertyFilter.DEPRECATED_CLIENT]) return omit()
        // Never used any more so not too worried about the formatting
        root.int("bodytype", message.bodyType)
        root.string("identkit", message.getIdentKitsByteArray().contentToString())
        root.string("colours", message.getColoursByteArray().contentToString())
    }

    override fun opNpc(message: OpNpc) {
        if (!filters[PropertyFilter.OPNPC]) return omit()
        root.npc(message.index)
        root.filteredBoolean("ctrl", message.controlKey)
    }

    override fun opNpc6(message: OpNpc6) {
        if (!filters[PropertyFilter.OPNPC]) return omit()
        root.scriptVarType("id", ScriptVarType.NPC, message.id)
    }

    override fun opNpcT(message: OpNpcT) {
        if (!filters[PropertyFilter.OPNPCT]) return omit()
        root.npc(message.index)
        root.filteredBoolean("ctrl", message.controlKey)
        root.com(message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("sub", message.selectedSub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun opObj(message: OpObj) {
        if (!filters[PropertyFilter.OPOBJ]) return omit()
        root.scriptVarType("id", ScriptVarType.OBJ, message.id)
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredBoolean("ctrl", message.controlKey)
    }

    override fun opObj6(message: OpObj6) {
        if (!filters[PropertyFilter.OPOBJ]) return omit()
        root.scriptVarType("id", ScriptVarType.OBJ, message.id)
        root.coordGrid(stateTracker.level(), message.x, message.z)
    }

    override fun opObjT(message: OpObjT) {
        if (!filters[PropertyFilter.OPOBJT]) return omit()
        root.scriptVarType("id", ScriptVarType.OBJ, message.id)
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredBoolean("ctrl", message.controlKey)
        root.com("selectedcom", message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("selectedsub", message.selectedSub, -1)
        root.filteredScriptVarType("selectedobj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun opPlayer(message: OpPlayer) {
        if (!filters[PropertyFilter.OPPLAYER]) return omit()
        root.player(message.index)
        root.filteredBoolean("ctrl", message.controlKey)
    }

    override fun opPlayerT(message: OpPlayerT) {
        if (!filters[PropertyFilter.OPPLAYERT]) return omit()
        root.player(message.index)
        root.filteredBoolean("ctrl", message.controlKey)
        root.com(message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("sub", message.selectedSub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun resumePauseButton(message: ResumePauseButton) {
        if (!filters[PropertyFilter.RESUME_PAUSEBUTTON]) return omit()
        root.com(message.interfaceId, message.componentId)
        root.filteredInt("sub", message.sub, -1)
    }

    override fun resumePCountDialog(message: ResumePCountDialog) {
        if (!filters[PropertyFilter.RESUME_P_COUNTDIALOG]) return omit()
        root.formattedInt("count", message.count)
    }

    override fun resumePNameDialog(message: ResumePNameDialog) {
        if (!filters[PropertyFilter.RESUME_P_NAMEDIALOG]) return omit()
        root.string("name", message.name)
    }

    override fun resumePObjDialog(message: ResumePObjDialog) {
        if (!filters[PropertyFilter.RESUME_P_OBJDIALOG]) return omit()
        root.scriptVarType("id", ScriptVarType.OBJ, message.obj)
    }

    override fun resumePStringDialog(message: ResumePStringDialog) {
        if (!filters[PropertyFilter.RESUME_P_STRINGDIALOG]) return omit()
        root.string("string", message.string)
    }

    override fun friendListAdd(message: FriendListAdd) {
        if (!filters[PropertyFilter.FRIENDLIST_ADD]) return omit()
        root.string("name", message.name)
    }

    override fun friendListDel(message: FriendListDel) {
        if (!filters[PropertyFilter.FRIENDLIST_DEL]) return omit()
        root.string("name", message.name)
    }

    override fun ignoreListAdd(message: IgnoreListAdd) {
        if (!filters[PropertyFilter.IGNORELIST_ADD]) return omit()
        root.string("name", message.name)
    }

    override fun ignoreListDel(message: IgnoreListDel) {
        if (!filters[PropertyFilter.IGNORELIST_DEL]) return omit()
        root.string("name", message.name)
    }

    private companion object {
        private val MS_NUMBER_FORMAT: NumberFormat = DecimalFormat("###,###,###ms")
    }
}
