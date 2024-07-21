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
import net.rsprox.protocol.game.incoming.model.misc.client.DetectModifiedClient
import net.rsprox.protocol.game.incoming.model.misc.client.Idle
import net.rsprox.protocol.game.incoming.model.misc.client.MapBuildComplete
import net.rsprox.protocol.game.incoming.model.misc.client.MembershipPromotionEligibility
import net.rsprox.protocol.game.incoming.model.misc.client.NoTimeout
import net.rsprox.protocol.game.incoming.model.misc.client.ReflectionCheckReply
import net.rsprox.protocol.game.incoming.model.misc.client.SendPingReply
import net.rsprox.protocol.game.incoming.model.misc.client.SoundJingleEnd
import net.rsprox.protocol.game.incoming.model.misc.client.Timings
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
import net.rsprox.transcriber.properties.Property
import net.rsprox.transcriber.properties.properties
import net.rsprox.transcriber.state.StateTracker

public open class BaseClientPacketTranscriber(
    private val formatter: BaseMessageFormatter,
    private val container: MessageConsumerContainer,
    private val stateTracker: StateTracker,
) : ClientPacketTranscriber {
    private fun format(
        name: String,
        properties: List<Property>,
    ): String {
        return formatter.format(
            clientPacket = true,
            cycle = stateTracker.cycle,
            name = name,
            properties = properties,
        )
    }

    override fun if1Button(message: If1Button) {
        TODO("Not yet implemented")
    }

    override fun if3Button(message: If3Button) {
        val packet = "IF_BUTTON${message.op}"
        val properties =
            properties {
                property("interfaceId", message.interfaceId)
                property("componentId", message.componentId)
                filteredProperty("sub", message.sub) { it != -1 }
                filteredProperty("obj", message.obj) { it != -1 }
            }
        container.publish(format(packet, properties))
    }

    override fun ifButtonD(message: IfButtonD) {
        TODO("Not yet implemented")
    }

    override fun ifButtonT(message: IfButtonT) {
        TODO("Not yet implemented")
    }

    override fun affinedClanSettingsAddBannedFromChannel(message: AffinedClanSettingsAddBannedFromChannel) {
        TODO("Not yet implemented")
    }

    override fun affinedClanSettingsSetMutedFromChannel(message: AffinedClanSettingsSetMutedFromChannel) {
        TODO("Not yet implemented")
    }

    override fun clanChannelFullRequest(message: ClanChannelFullRequest) {
        TODO("Not yet implemented")
    }

    override fun clanChannelKickUser(message: ClanChannelKickUser) {
        TODO("Not yet implemented")
    }

    override fun clanSettingsFullRequest(message: ClanSettingsFullRequest) {
        TODO("Not yet implemented")
    }

    override fun eventAppletFocus(message: EventAppletFocus) {
        TODO("Not yet implemented")
    }

    override fun eventCameraPosition(message: EventCameraPosition) {
        TODO("Not yet implemented")
    }

    override fun eventKeyboard(message: EventKeyboard) {
        TODO("Not yet implemented")
    }

    override fun eventMouseClick(message: EventMouseClick) {
        TODO("Not yet implemented")
    }

    override fun eventMouseMove(message: EventMouseMove) {
        TODO("Not yet implemented")
    }

    override fun eventMouseScroll(message: EventMouseScroll) {
        TODO("Not yet implemented")
    }

    override fun eventNativeMouseClick(message: EventNativeMouseClick) {
        TODO("Not yet implemented")
    }

    override fun eventNativeMouseMove(message: EventNativeMouseMove) {
        TODO("Not yet implemented")
    }

    override fun friendChatJoinLeave(message: FriendChatJoinLeave) {
        TODO("Not yet implemented")
    }

    override fun friendChatKick(message: FriendChatKick) {
        TODO("Not yet implemented")
    }

    override fun friendChatSetRank(message: FriendChatSetRank) {
        TODO("Not yet implemented")
    }

    override fun opLoc(message: OpLoc) {
        TODO("Not yet implemented")
    }

    override fun opLoc6(message: OpLoc6) {
        TODO("Not yet implemented")
    }

    override fun opLocT(message: OpLocT) {
        TODO("Not yet implemented")
    }

    override fun messagePrivateClient(message: MessagePrivate) {
        TODO("Not yet implemented")
    }

    override fun messagePublic(message: MessagePublic) {
        TODO("Not yet implemented")
    }

    override fun detectModifiedClient(message: DetectModifiedClient) {
        TODO("Not yet implemented")
    }

    override fun idle(message: Idle) {
        TODO("Not yet implemented")
    }

    override fun mapBuildComplete(message: MapBuildComplete) {
        TODO("Not yet implemented")
    }

    override fun membershipPromotionEligibility(message: MembershipPromotionEligibility) {
        TODO("Not yet implemented")
    }

    override fun noTimeout(message: NoTimeout) {
        TODO("Not yet implemented")
    }

    override fun reflectionCheckReply(message: ReflectionCheckReply) {
        TODO("Not yet implemented")
    }

    override fun sendPingReply(message: SendPingReply) {
        TODO("Not yet implemented")
    }

    override fun soundJingleEnd(message: SoundJingleEnd) {
        TODO("Not yet implemented")
    }

    override fun timings(message: Timings) {
        TODO("Not yet implemented")
    }

    override fun windowStatus(message: WindowStatus) {
        TODO("Not yet implemented")
    }

    override fun bugReport(message: BugReport) {
        TODO("Not yet implemented")
    }

    override fun clickWorldMap(message: ClickWorldMap) {
        TODO("Not yet implemented")
    }

    override fun clientCheat(message: ClientCheat) {
        TODO("Not yet implemented")
    }

    override fun closeModal(message: CloseModal) {
        TODO("Not yet implemented")
    }

    override fun hiscoreRequest(message: HiscoreRequest) {
        TODO("Not yet implemented")
    }

    override fun ifCrmViewClick(message: IfCrmViewClick) {
        TODO("Not yet implemented")
    }

    override fun moveGameClick(message: MoveGameClick) {
        TODO("Not yet implemented")
    }

    override fun moveMinimapClick(message: MoveMinimapClick) {
        TODO("Not yet implemented")
    }

    override fun oculusLeave(message: OculusLeave) {
        TODO("Not yet implemented")
    }

    override fun sendSnapshot(message: SendSnapshot) {
        TODO("Not yet implemented")
    }

    override fun setChatFilterSettings(message: SetChatFilterSettings) {
        TODO("Not yet implemented")
    }

    override fun teleport(message: Teleport) {
        TODO("Not yet implemented")
    }

    override fun updatePlayerModel(message: UpdatePlayerModel) {
        TODO("Not yet implemented")
    }

    override fun opNpc(message: OpNpc) {
        TODO("Not yet implemented")
    }

    override fun opNpc6(message: OpNpc6) {
        TODO("Not yet implemented")
    }

    override fun opNpcT(message: OpNpcT) {
        TODO("Not yet implemented")
    }

    override fun opObj(message: OpObj) {
        TODO("Not yet implemented")
    }

    override fun opObj6(message: OpObj6) {
        TODO("Not yet implemented")
    }

    override fun opObjT(message: OpObjT) {
        TODO("Not yet implemented")
    }

    override fun opPlayer(message: OpPlayer) {
        TODO("Not yet implemented")
    }

    override fun opPlayerT(message: OpPlayerT) {
        TODO("Not yet implemented")
    }

    override fun resumePauseButton(message: ResumePauseButton) {
        TODO("Not yet implemented")
    }

    override fun resumePCountDialog(message: ResumePCountDialog) {
        TODO("Not yet implemented")
    }

    override fun resumePNameDialog(message: ResumePNameDialog) {
        TODO("Not yet implemented")
    }

    override fun resumePObjDialog(message: ResumePObjDialog) {
        TODO("Not yet implemented")
    }

    override fun resumePStringDialog(message: ResumePStringDialog) {
        TODO("Not yet implemented")
    }

    override fun friendListAdd(message: FriendListAdd) {
        TODO("Not yet implemented")
    }

    override fun friendListDel(message: FriendListDel) {
        TODO("Not yet implemented")
    }

    override fun ignoreListAdd(message: IgnoreListAdd) {
        TODO("Not yet implemented")
    }

    override fun ignoreListDel(message: IgnoreListDel) {
        TODO("Not yet implemented")
    }
}
