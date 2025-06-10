package net.rsprox.transcriber.interfaces

import net.rsprox.protocol.game.incoming.model.buttons.*
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
import net.rsprox.protocol.game.incoming.model.misc.user.SetHeading
import net.rsprox.protocol.game.incoming.model.misc.user.Teleport
import net.rsprox.protocol.game.incoming.model.misc.user.UpdatePlayerModelV1
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

public interface ClientPacketTranscriber {
    public fun if1Button(message: If1Button)

    public fun if3Button(message: If3Button)

    public fun ifButtonX(message: If3Button)

    public fun ifSubOp(message: IfSubOp)

    public fun ifButtonD(message: IfButtonD)

    public fun ifButtonT(message: IfButtonT)

    public fun ifRunScript(message: IfRunScript)

    public fun affinedClanSettingsAddBannedFromChannel(message: AffinedClanSettingsAddBannedFromChannel)

    public fun affinedClanSettingsSetMutedFromChannel(message: AffinedClanSettingsSetMutedFromChannel)

    public fun clanChannelFullRequest(message: ClanChannelFullRequest)

    public fun clanChannelKickUser(message: ClanChannelKickUser)

    public fun clanSettingsFullRequest(message: ClanSettingsFullRequest)

    public fun eventAppletFocus(message: EventAppletFocus)

    public fun eventCameraPosition(message: EventCameraPosition)

    public fun eventKeyboard(message: EventKeyboard)

    public fun eventMouseClick(message: EventMouseClick)

    public fun eventMouseMove(message: EventMouseMove)

    public fun eventMouseScroll(message: EventMouseScroll)

    public fun eventNativeMouseClick(message: EventNativeMouseClick)

    public fun eventNativeMouseMove(message: EventNativeMouseMove)

    public fun friendChatJoinLeave(message: FriendChatJoinLeave)

    public fun friendChatKick(message: FriendChatKick)

    public fun friendChatSetRank(message: FriendChatSetRank)

    public fun opLoc(message: OpLoc)

    public fun opLoc6(message: OpLoc6)

    public fun opLocT(message: OpLocT)

    public fun messagePrivateClient(message: MessagePrivate)

    public fun messagePublic(message: MessagePublic)

    public fun detectModifiedClient(message: DetectModifiedClient)

    public fun idle(message: Idle)

    public fun mapBuildComplete(message: MapBuildComplete)

    public fun membershipPromotionEligibility(message: MembershipPromotionEligibility)

    public fun noTimeout(message: NoTimeout)

    public fun reflectionCheckReply(message: ReflectionCheckReply)

    public fun sendPingReply(message: SendPingReply)

    public fun soundJingleEnd(message: SoundJingleEnd)

    public fun connectionTelemetry(message: ConnectionTelemetry)

    public fun windowStatus(message: WindowStatus)

    public fun bugReport(message: BugReport)

    public fun clickWorldMap(message: ClickWorldMap)

    public fun clientCheat(message: ClientCheat)

    public fun closeModal(message: CloseModal)

    public fun hiscoreRequest(message: HiscoreRequest)

    public fun ifCrmViewClick(message: IfCrmViewClick)

    public fun moveGameClick(message: MoveGameClick)

    public fun moveMinimapClick(message: MoveMinimapClick)

    public fun oculusLeave(message: OculusLeave)

    public fun sendSnapshot(message: SendSnapshot)

    public fun setChatFilterSettings(message: SetChatFilterSettings)

    public fun teleport(message: Teleport)

    public fun updatePlayerModelV1(message: UpdatePlayerModelV1)

    public fun opNpc(message: OpNpc)

    public fun opNpc6(message: OpNpc6)

    public fun opNpcT(message: OpNpcT)

    public fun opObj(message: OpObj)

    public fun opObj6(message: OpObj6)

    public fun opObjT(message: OpObjT)

    public fun opPlayer(message: OpPlayer)

    public fun opPlayerT(message: OpPlayerT)

    public fun resumePauseButton(message: ResumePauseButton)

    public fun resumePCountDialog(message: ResumePCountDialog)

    public fun resumePNameDialog(message: ResumePNameDialog)

    public fun resumePObjDialog(message: ResumePObjDialog)

    public fun resumePStringDialog(message: ResumePStringDialog)

    public fun friendListAdd(message: FriendListAdd)

    public fun friendListDel(message: FriendListDel)

    public fun ignoreListAdd(message: IgnoreListAdd)

    public fun ignoreListDel(message: IgnoreListDel)

    public fun setHeading(message: SetHeading)
}
