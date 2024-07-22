package net.rsprox.transcriber.base

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
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivate
import net.rsprox.transcriber.ClientPacketTranscriber
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.ScriptVarType
import net.rsprox.transcriber.coord
import net.rsprox.transcriber.format
import net.rsprox.transcriber.indent
import net.rsprox.transcriber.properties.Property
import net.rsprox.transcriber.properties.PropertyBuilder
import net.rsprox.transcriber.properties.properties
import net.rsprox.transcriber.quote
import net.rsprox.transcriber.state.StateTracker
import java.awt.event.KeyEvent

@Suppress("DuplicatedCode")
public open class BaseClientPacketTranscriber(
    private val formatter: BaseMessageFormatter,
    private val container: MessageConsumerContainer,
    private val stateTracker: StateTracker,
) : ClientPacketTranscriber {
    private fun format(properties: List<Property>): String {
        return formatter.format(
            clientPacket = true,
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
            clientPacket = true,
            name = name,
            properties = properties,
            indentation = indentation,
        )
    }

    private fun publish(builderAction: PropertyBuilder.() -> Unit) {
        container.publish(format(properties(builderAction)))
    }

    private fun publishProt() {
        container.publish("[${stateTracker.currentProt}]".indent(1))
    }

    private fun npc(index: Int): String {
        // TODO: Format this properly later when NPC info is added
        return "(index=$index)"
    }

    private fun player(index: Int): String {
        val tracked = stateTracker.getPlayerOrNull(index)
        return if (tracked == null) {
            "(index=$index)"
        } else {
            "(index=$index, name=${tracked.name}, coord=${formatter.coord(tracked.coord)})"
        }
    }

    override fun if1Button(message: If1Button) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
        }
    }

    override fun if3Button(message: If3Button) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            filteredProperty("sub", message.sub) { it != -1 }
            filteredProperty("obj", formatter.type(ScriptVarType.OBJ, message.obj)) { it != "-1" }
        }
    }

    override fun ifButtonD(message: IfButtonD) {
        publish {
            property("selectedCom", formatter.com(message.selectedInterfaceId, message.selectedComponentId))
            filteredProperty("selectedSub", message.selectedSub) { it != -1 }
            filteredProperty("selectedObj", formatter.type(ScriptVarType.OBJ, message.selectedObj)) { it != "-1" }
            property("targetCom", formatter.com(message.targetInterfaceId, message.targetComponentId))
            filteredProperty("targetSub", message.targetSub) { it != -1 }
            filteredProperty("targetObj", formatter.type(ScriptVarType.OBJ, message.targetObj)) { it != "-1" }
        }
    }

    override fun ifButtonT(message: IfButtonT) {
        publish {
            property("selectedCom", formatter.com(message.selectedInterfaceId, message.selectedComponentId))
            filteredProperty("selectedSub", message.selectedSub) { it != -1 }
            filteredProperty("selectedObj", formatter.type(ScriptVarType.OBJ, message.selectedObj)) { it != "-1" }
            property("targetCom", formatter.com(message.targetInterfaceId, message.targetComponentId))
            filteredProperty("targetSub", message.targetSub) { it != -1 }
            filteredProperty("targetObj", formatter.type(ScriptVarType.OBJ, message.targetObj)) { it != "-1" }
        }
    }

    override fun affinedClanSettingsAddBannedFromChannel(message: AffinedClanSettingsAddBannedFromChannel) {
        publish {
            property("name", message.name.quote())
            property("clanId", message.clanId)
            property("memberIndex", message.memberIndex)
        }
    }

    override fun affinedClanSettingsSetMutedFromChannel(message: AffinedClanSettingsSetMutedFromChannel) {
        publish {
            property("name", message.name.quote())
            property("clanId", message.clanId)
            property("memberIndex", message.memberIndex)
            property("muted", message.muted)
        }
    }

    override fun clanChannelFullRequest(message: ClanChannelFullRequest) {
        publish {
            property("clanId", message.clanId)
        }
    }

    override fun clanChannelKickUser(message: ClanChannelKickUser) {
        publish {
            property("name", message.name.quote())
            property("clanId", message.clanId)
            property("memberIndex", message.memberIndex)
        }
    }

    override fun clanSettingsFullRequest(message: ClanSettingsFullRequest) {
        publish {
            property("clanId", message.clanId)
        }
    }

    override fun eventAppletFocus(message: EventAppletFocus) {
        publish {
            property("inFocus", message.inFocus)
        }
    }

    override fun eventCameraPosition(message: EventCameraPosition) {
        publish {
            property("angleX", message.angleX)
            property("angleY", message.angleY)
        }
    }

    override fun eventKeyboard(message: EventKeyboard) {
        publish {
            property("lastTransmitted", message.lastTransmittedKeyPress.format() + "ms")
            property(
                "keys",
                message
                    .keysPressed
                    .toAwtKeyCodeIntArray()
                    .map { KeyEvent.getKeyText(it) },
            )
        }
    }

    override fun eventMouseClick(message: EventMouseClick) {
        publish {
            property("lastTransmitted", message.lastTransmittedMouseClick.format() + "ms")
            property("x", message.x)
            property("y", message.y)
            property("rightClick", message.rightClick)
        }
    }

    override fun eventMouseMove(message: EventMouseMove) {
        publish {
            property("averageTime", message.averageTime.format() + "ms")
            property("remainingTime", message.remainingTime.format() + "ms")
        }
        for (index in message.movements.asLongArray().indices) {
            val movement = message.movements.getMousePosChange(index)
            container.publish(
                format(2, "Movement") {
                    property("deltaTime", movement.timeDelta.format() + "ms")
                    property("deltaX", movement.xDelta)
                    property("deltaY", movement.yDelta)
                },
            )
        }
    }

    override fun eventMouseScroll(message: EventMouseScroll) {
        publish {
            property("rotation", message.mouseWheelRotation)
        }
    }

    override fun eventNativeMouseClick(message: EventNativeMouseClick) {
        publish {
            property("lastTransmitted", message.lastTransmittedMouseClick.format() + "ms")
            property("x", message.x)
            property("y", message.y)
            property("code", message.code)
        }
    }

    override fun eventNativeMouseMove(message: EventNativeMouseMove) {
        publish {
            property("averageTime", message.averageTime.format() + "ms")
            property("remainingTime", message.remainingTime.format() + "ms")
        }
        for (index in message.movements.asLongArray().indices) {
            val movement = message.movements.getMousePosChange(index)
            container.publish(
                format(2, "Movement") {
                    property("deltaTime", movement.timeDelta.format() + "ms")
                    property("deltaX", movement.xDelta)
                    property("deltaY", movement.yDelta)
                },
            )
        }
    }

    override fun friendChatJoinLeave(message: FriendChatJoinLeave) {
        publish {
            property("name", message.name?.quote() ?: "null")
        }
    }

    override fun friendChatKick(message: FriendChatKick) {
        publish {
            property("name", message.name.quote())
        }
    }

    override fun friendChatSetRank(message: FriendChatSetRank) {
        publish {
            property("name", message.name.quote())
            property("rank", message.rank)
        }
    }

    override fun opLoc(message: OpLoc) {
        publish {
            property("id", formatter.type(ScriptVarType.LOC, message.id))
            property("coord", formatter.coord(stateTracker.level(), message.x, message.z))
            filteredProperty("controlKey", message.controlKey) { it }
        }
    }

    override fun opLoc6(message: OpLoc6) {
        publish {
            property("id", formatter.type(ScriptVarType.LOC, message.id))
        }
    }

    override fun opLocT(message: OpLocT) {
        publish {
            property("id", formatter.type(ScriptVarType.LOC, message.id))
            property("coord", formatter.coord(stateTracker.level(), message.x, message.z))
            filteredProperty("controlKey", message.controlKey) { it }
            property("com", formatter.com(message.selectedInterfaceId, message.selectedComponentId))
            filteredProperty("sub", message.selectedSub) { it != -1 }
            filteredProperty("obj", formatter.type(ScriptVarType.OBJ, message.selectedObj)) { it != "-1" }
        }
    }

    override fun messagePrivateClient(message: MessagePrivate) {
        publish {
            property("sender", message.sender.quote())
            property("worldId", message.worldId)
            property("worldMessageCounter", message.worldMessageCounter)
            property("chatCrownType", message.chatCrownType)
            property("message", message.message.quote())
        }
    }

    override fun messagePublic(message: MessagePublic) {
        publish {
            property("type", message.type)
            property("message", message.message.quote())
            filteredProperty("colour", message.colour) { it != 0 }
            filteredProperty("effect", message.effect) { it != 0 }
            filteredProperty("clanType", message.clanType) { it != -1 }
            filteredProperty("pattern", message.pattern?.asByteArray()?.contentToString()) { it != null }
        }
    }

    override fun detectModifiedClient(message: DetectModifiedClient) {
        publish {
            property("code", message.code.format())
        }
    }

    override fun idle(message: Idle) {
        publishProt()
    }

    override fun mapBuildComplete(message: MapBuildComplete) {
        publishProt()
    }

    override fun membershipPromotionEligibility(message: MembershipPromotionEligibility) {
        publish {
            property("introductoryPrice", message.eligibleForIntroductoryPrice)
            property("trialPurchase", message.eligibleForTrialPurchase)
        }
    }

    override fun noTimeout(message: NoTimeout) {
        publishProt()
    }

    override fun reflectionCheckReply(message: ReflectionCheckReply) {
        publish {
            property("id", message.id.format())
        }
        // This may need prettier logging, but there's no way to test right now without
        // adding RuneLite support, so leaving it be as is
        for (result in message.result) {
            container.publish(
                format(2, "") {
                    property("result", result)
                },
            )
        }
    }

    override fun sendPingReply(message: SendPingReply) {
        publish {
            property("fps", message.fps)
            property("gcPercentTime", message.gcPercentTime)
            property("value1", message.value1)
            property("value2", message.value2)
        }
    }

    override fun soundJingleEnd(message: SoundJingleEnd) {
        publish {
            property("id", formatter.type(ScriptVarType.JINGLE, message.jingleId))
        }
    }

    override fun connectionTelemetry(message: ConnectionTelemetry) {
        publish {
            property("connectionLostDuration", (message.connectionLostDuration * 10).format() + "ms")
            property("loginDuration", (message.loginDuration * 10).format() + "ms")
            property("clientState", message.clientState)
            property("loginCount", message.loginCount)
        }
    }

    override fun windowStatus(message: WindowStatus) {
        publish {
            property("windowMode", message.windowMode)
            property("frameWidth", message.frameWidth)
            property("frameHeight", message.frameHeight)
        }
    }

    override fun bugReport(message: BugReport) {
        publish {
            property("type", message.type)
            property("description", message.description.quote())
            property("instructions", message.instructions.quote())
        }
    }

    override fun clickWorldMap(message: ClickWorldMap) {
        publish {
            property("coord", formatter.coord(message.level, message.x, message.z))
        }
    }

    override fun clientCheat(message: ClientCheat) {
        publish {
            property("cheat", message.command)
        }
    }

    override fun closeModal(message: CloseModal) {
        publishProt()
    }

    override fun hiscoreRequest(message: HiscoreRequest) {
        publish {
            property("type", message.type)
            property("requestId", message.requestId)
            property("name", message.name.quote())
        }
    }

    override fun ifCrmViewClick(message: IfCrmViewClick) {
        publish {
            property("crmServerTarget", message.crmServerTarget)
            property("com", formatter.com(message.interfaceId, message.componentId))
            filteredProperty("sub", message.sub) { it != -1 }
            property("payload", "[${message.behaviour1}, ${message.behaviour2}, ${message.behaviour3}]")
        }
    }

    override fun moveGameClick(message: MoveGameClick) {
        publish {
            property("coord", formatter.coord(stateTracker.level(), message.x, message.z))
            val keyLabel =
                when (message.keyCombination) {
                    1 -> "Ctrl"
                    2 -> "Ctrl+Shift"
                    else -> "None"
                }
            filteredProperty("keyCombination", keyLabel) {
                it != "None"
            }
        }
    }

    override fun moveMinimapClick(message: MoveMinimapClick) {
        publish {
            property("coord", formatter.coord(stateTracker.level(), message.x, message.z))
            val keyLabel =
                when (message.keyCombination) {
                    1 -> "Ctrl"
                    2 -> "Ctrl+Shift"
                    else -> "None"
                }
            filteredProperty("keyCombination", keyLabel) {
                it != "None"
            }
            property("minimapWidth", message.minimapWidth)
            property("minimapHeight", message.minimapHeight)
            property("cameraAngleY", message.cameraAngleY)
            property("fineX", message.fineX)
            property("fineZ", message.fineZ)
        }
    }

    override fun oculusLeave(message: OculusLeave) {
        publishProt()
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
        publish {
            property("name", message.name.quote())
            property("rule", getRule(message.ruleId).quote())
            filteredProperty("mute", message.mute) { it }
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

    override fun setChatFilterSettings(message: SetChatFilterSettings) {
        publish {
            property("public", getChatFilter(message.publicChatFilter).quote())
            property("private", getChatFilter(message.privateChatFilter).quote())
            property("trade", getChatFilter(message.tradeChatFilter).quote())
        }
    }

    override fun teleport(message: Teleport) {
        publish {
            property("coord", formatter.coord(message.level, message.x, message.z))
            filteredProperty("oculusSyncValue", message.oculusSyncValue) { it != 0 }
        }
    }

    override fun updatePlayerModel(message: UpdatePlayerModel) {
        // Never used any more so not too worried about the formatting
        publish {
            property("bodyType", message.bodyType)
            property("identKits", message.getIdentKitsByteArray().contentToString())
            property("colours", message.getColoursByteArray().contentToString())
        }
    }

    override fun opNpc(message: OpNpc) {
        publish {
            property("npc", npc(message.index))
            filteredProperty("controlKey", message.controlKey) { it }
        }
    }

    override fun opNpc6(message: OpNpc6) {
        publish {
            property("id", formatter.type(ScriptVarType.NPC, message.id))
        }
    }

    override fun opNpcT(message: OpNpcT) {
        publish {
            property("npc", npc(message.index))
            filteredProperty("controlKey", message.controlKey) { it }
            property("com", formatter.com(message.selectedInterfaceId, message.selectedComponentId))
            filteredProperty("sub", message.selectedSub) { it != -1 }
            filteredProperty("obj", formatter.type(ScriptVarType.OBJ, message.selectedObj)) { it != "-1" }
        }
    }

    override fun opObj(message: OpObj) {
        publish {
            property("id", formatter.type(ScriptVarType.OBJ, message.id))
            property("coord", formatter.coord(stateTracker.level(), message.x, message.z))
            filteredProperty("controlKey", message.controlKey) { it }
        }
    }

    override fun opObj6(message: OpObj6) {
        publish {
            property("id", formatter.type(ScriptVarType.OBJ, message.id))
            property("coord", formatter.coord(stateTracker.level(), message.x, message.z))
        }
    }

    override fun opObjT(message: OpObjT) {
        publish {
            property("id", formatter.type(ScriptVarType.OBJ, message.id))
            property("coord", formatter.coord(stateTracker.level(), message.x, message.z))
            filteredProperty("controlKey", message.controlKey) { it }
            property("selectedCom", formatter.com(message.selectedInterfaceId, message.selectedComponentId))
            filteredProperty("selectedSub", message.selectedSub) { it != -1 }
            filteredProperty("selectedObj", formatter.type(ScriptVarType.OBJ, message.selectedObj)) { it != "-1" }
        }
    }

    override fun opPlayer(message: OpPlayer) {
        publish {
            property("player", player(message.index))
            filteredProperty("controlKey", message.controlKey) { it }
        }
    }

    override fun opPlayerT(message: OpPlayerT) {
        publish {
            property("player", player(message.index))
            filteredProperty("controlKey", message.controlKey) { it }
            property("com", formatter.com(message.selectedInterfaceId, message.selectedComponentId))
            filteredProperty("sub", message.selectedSub) { it != -1 }
            filteredProperty("obj", formatter.type(ScriptVarType.OBJ, message.selectedObj)) { it != "-1" }
        }
    }

    override fun resumePauseButton(message: ResumePauseButton) {
        publish {
            property("com", formatter.com(message.interfaceId, message.componentId))
            filteredProperty("sub", message.sub) { it != -1 }
        }
    }

    override fun resumePCountDialog(message: ResumePCountDialog) {
        publish {
            property("count", message.count.format())
        }
    }

    override fun resumePNameDialog(message: ResumePNameDialog) {
        publish {
            property("name", message.name.quote())
        }
    }

    override fun resumePObjDialog(message: ResumePObjDialog) {
        publish {
            property("id", formatter.type(ScriptVarType.OBJ, message.obj))
        }
    }

    override fun resumePStringDialog(message: ResumePStringDialog) {
        publish {
            property("string", message.string.quote())
        }
    }

    override fun friendListAdd(message: FriendListAdd) {
        publish {
            property("name", message.name.quote())
        }
    }

    override fun friendListDel(message: FriendListDel) {
        publish {
            property("name", message.name.quote())
        }
    }

    override fun ignoreListAdd(message: IgnoreListAdd) {
        publish {
            property("name", message.name.quote())
        }
    }

    override fun ignoreListDel(message: IgnoreListDel) {
        publish {
            property("name", message.name.quote())
        }
    }
}
