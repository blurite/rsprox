package net.rsprox.transcriber.base.impl

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
import net.rsprox.shared.property.identifiedNpc
import net.rsprox.shared.property.identifiedPlayer
import net.rsprox.shared.property.int
import net.rsprox.shared.property.long
import net.rsprox.shared.property.namedEnum
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.string
import net.rsprox.shared.property.unidentifiedNpc
import net.rsprox.shared.property.unidentifiedPlayer
import net.rsprox.transcriber.impl.ClientPacketTranscriber
import net.rsprox.transcriber.state.StateTracker
import java.awt.event.KeyEvent
import java.text.DecimalFormat
import java.text.NumberFormat

@Suppress("SpellCheckingInspection", "DuplicatedCode")
public open class BaseClientPacketTranscriber(
    private val stateTracker: StateTracker,
) : ClientPacketTranscriber {
    private val root: RootProperty<*>
        get() = stateTracker.root

    private fun Property.npc(index: Int): ChildProperty<*> {
        val npc = stateTracker.getActiveWorld().getNpcOrNull(index)
        return if (npc != null) {
            identifiedNpc(
                index,
                npc.name ?: "null",
                npc.coord.level,
                npc.coord.x,
                npc.coord.z,
            )
        } else {
            unidentifiedNpc(index)
        }
    }

    private fun Property.player(index: Int): ChildProperty<*> {
        val npc = stateTracker.getPlayerOrNull(index)
        return if (npc != null) {
            identifiedPlayer(
                index,
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
        root.com(message.interfaceId, message.componentId)
    }

    override fun if3Button(message: If3Button) {
        root.com(message.interfaceId, message.componentId)
        root.filteredInt("sub", message.sub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.obj, -1)
    }

    override fun ifButtonD(message: IfButtonD) {
        root.com("selectcom", message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("selectedsub", message.selectedSub, -1)
        root.filteredScriptVarType("selectedobj", ScriptVarType.OBJ, message.selectedObj, -1)
        root.com("targetcom", message.targetInterfaceId, message.targetComponentId)
        root.filteredInt("targetsub", message.targetSub, -1)
        root.filteredScriptVarType("targetobj", ScriptVarType.OBJ, message.targetObj, -1)
    }

    override fun ifButtonT(message: IfButtonT) {
        root.com("selectcom", message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("selectedsub", message.selectedSub, -1)
        root.filteredScriptVarType("selectedobj", ScriptVarType.OBJ, message.selectedObj, -1)
        root.com("targetcom", message.targetInterfaceId, message.targetComponentId)
        root.filteredInt("targetsub", message.targetSub, -1)
        root.filteredScriptVarType("targetobj", ScriptVarType.OBJ, message.targetObj, -1)
    }

    override fun affinedClanSettingsAddBannedFromChannel(message: AffinedClanSettingsAddBannedFromChannel) {
        root.string("name", message.name)
        root.int("clanid", message.clanId)
        root.int("memberindex", message.memberIndex)
    }

    override fun affinedClanSettingsSetMutedFromChannel(message: AffinedClanSettingsSetMutedFromChannel) {
        root.string("name", message.name)
        root.int("clanid", message.clanId)
        root.int("memberindex", message.memberIndex)
        root.boolean("muted", message.muted)
    }

    override fun clanChannelFullRequest(message: ClanChannelFullRequest) {
        root.int("clanid", message.clanId)
    }

    override fun clanChannelKickUser(message: ClanChannelKickUser) {
        root.string("name", message.name)
        root.int("clanid", message.clanId)
        root.int("memberindex", message.memberIndex)
    }

    override fun clanSettingsFullRequest(message: ClanSettingsFullRequest) {
        root.int("clanid", message.clanId)
    }

    override fun eventAppletFocus(message: EventAppletFocus) {
        root.boolean("infocus", message.inFocus)
    }

    override fun eventCameraPosition(message: EventCameraPosition) {
        root.int("anglex", message.angleX)
        root.int("angley", message.angleY)
    }

    override fun eventKeyboard(message: EventKeyboard) {
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
        root.formattedInt("lasttransmitted", message.lastTransmittedMouseClick, MS_NUMBER_FORMAT)
        root.int("x", message.x)
        root.int("y", message.y)
        root.boolean("rightclick", message.rightClick)
    }

    override fun eventMouseMove(message: EventMouseMove) {
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
        root.int("rotation", message.mouseWheelRotation)
    }

    override fun eventNativeMouseClick(message: EventNativeMouseClick) {
        root.formattedInt("lasttransmitted", message.lastTransmittedMouseClick, MS_NUMBER_FORMAT)
        root.int("x", message.x)
        root.int("y", message.y)
        root.int("code", message.code)
    }

    override fun eventNativeMouseMove(message: EventNativeMouseMove) {
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
        root.string("name", message.name)
    }

    override fun friendChatKick(message: FriendChatKick) {
        root.string("name", message.name)
    }

    override fun friendChatSetRank(message: FriendChatSetRank) {
        root.string("name", message.name)
        root.int("rank", message.rank)
    }

    override fun opLoc(message: OpLoc) {
        root.scriptVarType("id", ScriptVarType.LOC, message.id)
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredBoolean("ctrl", message.controlKey)
    }

    override fun opLoc6(message: OpLoc6) {
        root.scriptVarType("id", ScriptVarType.LOC, message.id)
    }

    override fun opLocT(message: OpLocT) {
        root.scriptVarType("id", ScriptVarType.LOC, message.id)
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredBoolean("ctrl", message.controlKey)
        root.com(message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("sub", message.selectedSub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun messagePrivateClient(message: MessagePrivate) {
        root.string("name", message.name)
        root.string("message", message.message)
    }

    override fun messagePublic(message: MessagePublic) {
        root.int("type", message.type)
        root.filteredInt("colour", message.colour, 0)
        root.filteredInt("effect", message.effect, 0)
        root.filteredInt("clantype", message.clanType, -1)
        root.string("message", message.message)
        root.filteredString("pattern", message.pattern?.asByteArray()?.contentToString(), null)
    }

    override fun detectModifiedClient(message: DetectModifiedClient) {
        root.formattedInt("code", message.code)
    }

    override fun idle(message: Idle) {
    }

    override fun mapBuildComplete(message: MapBuildComplete) {
    }

    override fun membershipPromotionEligibility(message: MembershipPromotionEligibility) {
        root.int("introductoryprice", message.eligibleForIntroductoryPrice)
        root.int("trialpurchase", message.eligibleForTrialPurchase)
    }

    override fun noTimeout(message: NoTimeout) {
    }

    override fun reflectionCheckReply(message: ReflectionCheckReply) {
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
        root.int("fps", message.fps)
        root.int("gcpercenttime", message.gcPercentTime)
        root.int("value1", message.value1)
        root.int("value2", message.value2)
    }

    override fun soundJingleEnd(message: SoundJingleEnd) {
        root.scriptVarType("id", ScriptVarType.JINGLE, message.jingleId)
    }

    override fun connectionTelemetry(message: ConnectionTelemetry) {
        root.formattedInt("connectionlostduration", message.connectionLostDuration * 10, MS_NUMBER_FORMAT)
        root.formattedInt("loginduration", message.loginDuration * 10, MS_NUMBER_FORMAT)
        root.int("clientstate", message.clientState)
        root.int("logincount", message.loginCount)
    }

    override fun windowStatus(message: WindowStatus) {
        root.int("windowmode", message.windowMode)
        root.int("framewidth", message.frameWidth)
        root.int("frameheight", message.frameHeight)
    }

    override fun bugReport(message: BugReport) {
        root.int("type", message.type)
        root.string("description", message.description)
        root.string("instructions", message.instructions)
    }

    override fun clickWorldMap(message: ClickWorldMap) {
        root.coordGrid(message.level, message.x, message.z)
    }

    override fun clientCheat(message: ClientCheat) {
        root.string("cheat", message.command)
    }

    override fun closeModal(message: CloseModal) {
    }

    override fun hiscoreRequest(message: HiscoreRequest) {
        root.int("type", message.type)
        root.int("requestid", message.requestId)
        root.string("name", message.name)
    }

    override fun ifCrmViewClick(message: IfCrmViewClick) {
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
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredNamedEnum(
            "keycombination",
            getMovementKeyCombination(message.keyCombination),
            MovementKeyCombination.NONE,
        )
    }

    override fun moveMinimapClick(message: MoveMinimapClick) {
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
        root.namedEnum("public", getChatFilter(message.publicChatFilter))
        root.namedEnum("private", getChatFilter(message.privateChatFilter))
        root.namedEnum("trade", getChatFilter(message.tradeChatFilter))
    }

    override fun teleport(message: Teleport) {
        root.coordGrid(message.level, message.x, message.z)
        root.filteredInt("oculussyncvalue", message.oculusSyncValue, 0)
    }

    override fun updatePlayerModel(message: UpdatePlayerModel) {
        // Never used any more so not too worried about the formatting
        root.int("bodytype", message.bodyType)
        root.string("identkit", message.getIdentKitsByteArray().contentToString())
        root.string("colours", message.getColoursByteArray().contentToString())
    }

    override fun opNpc(message: OpNpc) {
        root.npc(message.index)
        root.filteredBoolean("ctrl", message.controlKey)
    }

    override fun opNpc6(message: OpNpc6) {
        root.scriptVarType("id", ScriptVarType.NPC, message.id)
    }

    override fun opNpcT(message: OpNpcT) {
        root.npc(message.index)
        root.filteredBoolean("ctrl", message.controlKey)
        root.com(message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("sub", message.selectedSub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun opObj(message: OpObj) {
        root.scriptVarType("id", ScriptVarType.OBJ, message.id)
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredBoolean("ctrl", message.controlKey)
    }

    override fun opObj6(message: OpObj6) {
        root.scriptVarType("id", ScriptVarType.OBJ, message.id)
        root.coordGrid(stateTracker.level(), message.x, message.z)
    }

    override fun opObjT(message: OpObjT) {
        root.scriptVarType("id", ScriptVarType.OBJ, message.id)
        root.coordGrid(stateTracker.level(), message.x, message.z)
        root.filteredBoolean("ctrl", message.controlKey)
        root.com("selectedcom", message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("selectedsub", message.selectedSub, -1)
        root.filteredScriptVarType("selectedobj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun opPlayer(message: OpPlayer) {
        root.player(message.index)
        root.filteredBoolean("ctrl", message.controlKey)
    }

    override fun opPlayerT(message: OpPlayerT) {
        root.player(message.index)
        root.filteredBoolean("ctrl", message.controlKey)
        root.com(message.selectedInterfaceId, message.selectedComponentId)
        root.filteredInt("sub", message.selectedSub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun resumePauseButton(message: ResumePauseButton) {
        root.com(message.interfaceId, message.componentId)
        root.filteredInt("sub", message.sub, -1)
    }

    override fun resumePCountDialog(message: ResumePCountDialog) {
        root.formattedInt("count", message.count)
    }

    override fun resumePNameDialog(message: ResumePNameDialog) {
        root.string("name", message.name)
    }

    override fun resumePObjDialog(message: ResumePObjDialog) {
        root.scriptVarType("id", ScriptVarType.OBJ, message.obj)
    }

    override fun resumePStringDialog(message: ResumePStringDialog) {
        root.string("string", message.string)
    }

    override fun friendListAdd(message: FriendListAdd) {
        root.string("name", message.name)
    }

    override fun friendListDel(message: FriendListDel) {
        root.string("name", message.name)
    }

    override fun ignoreListAdd(message: IgnoreListAdd) {
        root.string("name", message.name)
    }

    override fun ignoreListDel(message: IgnoreListDel) {
        root.string("name", message.name)
    }

    private companion object {
        private val MS_NUMBER_FORMAT: NumberFormat = DecimalFormat("###,###,###ms")
    }
}
