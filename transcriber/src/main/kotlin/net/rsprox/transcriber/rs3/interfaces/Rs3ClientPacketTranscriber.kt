package net.rsprox.transcriber.rs3.interfaces

import net.rsprox.protocol.game.incoming.model.unknown.UnknownClientPacket
import net.rsprox.protocol.rs3.game.incoming.model.account.AddNewEmailAddress
import net.rsprox.protocol.rs3.game.incoming.model.account.ChangeEmailAddress
import net.rsprox.protocol.rs3.game.incoming.model.account.CreateLogProgress
import net.rsprox.protocol.rs3.game.incoming.model.account.SendEmailValidationCode
import net.rsprox.protocol.rs3.game.incoming.model.buttons.If3Button
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfButtonD
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfButtonT
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfCrmButton
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfCrmViewOp
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfTextChange
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfValueChange32
import net.rsprox.protocol.rs3.game.incoming.model.chat.ChatSetMode
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessagePrivate
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessagePublic
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessageQuickchatPrivate
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessageQuickchatPublic
import net.rsprox.protocol.rs3.game.incoming.model.chat.SetChatFilterSettings
import net.rsprox.protocol.rs3.game.incoming.model.dialog.AbortPDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePClanForumQfcDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePCountDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePCountDialogLong
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePHslDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePNameDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePObjDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePStringDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePauseButton
import net.rsprox.protocol.rs3.game.incoming.model.events.ClientPreferences
import net.rsprox.protocol.rs3.game.incoming.model.events.Cutscene2DFinished
import net.rsprox.protocol.rs3.game.incoming.model.events.EventAppletFocus
import net.rsprox.protocol.rs3.game.incoming.model.events.EventCameraPosition
import net.rsprox.protocol.rs3.game.incoming.model.events.EventKeyboard
import net.rsprox.protocol.rs3.game.incoming.model.events.EventMouseClick
import net.rsprox.protocol.rs3.game.incoming.model.events.EventMouseMove
import net.rsprox.protocol.rs3.game.incoming.model.events.EventNativeMouseClick
import net.rsprox.protocol.rs3.game.incoming.model.events.EventNativeMouseMove
import net.rsprox.protocol.rs3.game.incoming.model.events.PingStatistics
import net.rsprox.protocol.rs3.game.incoming.model.events.SendPingReply
import net.rsprox.protocol.rs3.game.incoming.model.events.SoundSongEnd
import net.rsprox.protocol.rs3.game.incoming.model.events.TransmitVarVerifyId
import net.rsprox.protocol.rs3.game.incoming.model.events.WindowStatus
import net.rsprox.protocol.rs3.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.rs3.game.incoming.model.locs.OpLocT
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.MapBuildComplete
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.MapBuildCompleteV2
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.NoTimeout
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.StoreServerPermVarcs
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.UnnamedLobbyRequest
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.ApCoordT
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.BugReport
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.ClickWorldMap
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.ClientCheat
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.CloseModal
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.FaceSquare
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.LocSelectSubmit
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.MoveMinimapClick
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.MoveScripted
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.SendSnapshot
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.UrlRequest
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.WorldListFetch
import net.rsprox.protocol.rs3.game.incoming.model.npcs.OpNpc
import net.rsprox.protocol.rs3.game.incoming.model.npcs.OpNpcT
import net.rsprox.protocol.rs3.game.incoming.model.objs.OpObj
import net.rsprox.protocol.rs3.game.incoming.model.objs.OpObjT
import net.rsprox.protocol.rs3.game.incoming.model.players.OpPlayer
import net.rsprox.protocol.rs3.game.incoming.model.players.OpPlayerT
import net.rsprox.protocol.rs3.game.incoming.model.social.AffinedClanSettingsAddBannedFromChannel
import net.rsprox.protocol.rs3.game.incoming.model.social.AffinedClanSettingsSetMutedFromChannel
import net.rsprox.protocol.rs3.game.incoming.model.social.ClanChannelKickUser
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendChatJoinLeave
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendChatKick
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendChatSetRank
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendListAdd
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendListDel
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendSetNotes
import net.rsprox.protocol.rs3.game.incoming.model.social.IgnoreListAdd
import net.rsprox.protocol.rs3.game.incoming.model.social.IgnoreListDel
import net.rsprox.protocol.rs3.game.incoming.model.social.IgnoreSetNotes

public interface Rs3ClientPacketTranscriber {
    public fun if3Button(message: If3Button)

    public fun opNpc(message: OpNpc)

    public fun opLoc(message: OpLoc)

    public fun opObj(message: OpObj)

    public fun opPlayer(message: OpPlayer)

    public fun eventAppletFocus(message: EventAppletFocus)

    public fun eventNativeMouseClick(message: EventNativeMouseClick)

    public fun moveGameClick(message: MoveGameClick)

    public fun moveMinimapClick(message: MoveMinimapClick)

    public fun moveScripted(message: MoveScripted)

    public fun opNpcT(message: OpNpcT)

    public fun opPlayerT(message: OpPlayerT)

    public fun opLocT(message: OpLocT)

    public fun opObjT(message: OpObjT)

    public fun apCoordT(message: ApCoordT)

    public fun ifButtonT(message: IfButtonT)

    public fun ifButtonD(message: IfButtonD)

    public fun eventCameraPosition(message: EventCameraPosition)

    public fun windowStatus(message: WindowStatus)

    public fun resumePauseButton(message: ResumePauseButton)

    public fun resumePCountDialog(message: ResumePCountDialog)

    public fun resumePCountDialogLong(message: ResumePCountDialogLong)

    public fun resumePHslDialog(message: ResumePHslDialog)

    public fun resumePObjDialog(message: ResumePObjDialog)

    public fun resumePStringDialog(message: ResumePStringDialog)

    public fun resumePNameDialog(message: ResumePNameDialog)

    public fun resumePClanForumQfcDialog(message: ResumePClanForumQfcDialog)

    public fun messagePublic(message: MessagePublic)

    public fun messagePrivate(message: MessagePrivate)

    public fun clientCheat(message: ClientCheat)

    public fun chatSetMode(message: ChatSetMode)

    public fun setChatFilterSettings(message: SetChatFilterSettings)

    public fun mapBuildComplete(message: MapBuildComplete)

    public fun closeModal(message: CloseModal)

    public fun abortPDialog(message: AbortPDialog)

    public fun noTimeout(message: NoTimeout)

    public fun mapBuildCompleteV2(message: MapBuildCompleteV2)

    public fun ifCrmButton(message: IfCrmButton)

    public fun ignoreListDel(message: IgnoreListDel)

    public fun friendListAdd(message: FriendListAdd)

    public fun clanChannelKickUser(message: ClanChannelKickUser)

    public fun clickWorldMap(message: ClickWorldMap)

    public fun bugReport(message: BugReport)

    public fun ifCrmViewOp(message: IfCrmViewOp)

    public fun cutscene2DFinished(message: Cutscene2DFinished)

    public fun transmitVarVerifyId(message: TransmitVarVerifyId)

    public fun pingStatistics(message: PingStatistics)

    public fun sendEmailValidationCode(message: SendEmailValidationCode)

    public fun friendListDel(message: FriendListDel)

    public fun friendChatKick(message: FriendChatKick)

    public fun affinedClanSettingsSetMutedFromChannel(message: AffinedClanSettingsSetMutedFromChannel)

    public fun faceSquare(message: FaceSquare)

    public fun urlRequest(message: UrlRequest)

    public fun eventMouseClick(message: EventMouseClick)

    public fun friendChatJoinLeave(message: FriendChatJoinLeave)

    public fun createLogProgress(message: CreateLogProgress)

    public fun ignoreSetNotes(message: IgnoreSetNotes)

    public fun affinedClanSettingsAddBannedFromChannel(message: AffinedClanSettingsAddBannedFromChannel)

    public fun friendSetNotes(message: FriendSetNotes)

    public fun sendPingReply(message: SendPingReply)

    public fun friendChatSetRank(message: FriendChatSetRank)

    public fun worldListFetch(message: WorldListFetch)

    public fun soundSongEnd(message: SoundSongEnd)

    public fun ignoreListAdd(message: IgnoreListAdd)

    public fun ifTextChange(message: IfTextChange)

    public fun ifValueChange32(message: IfValueChange32)

    public fun locSelectSubmit(message: LocSelectSubmit)

    public fun eventKeyboard(message: EventKeyboard)

    public fun eventMouseMove(message: EventMouseMove)

    public fun eventNativeMouseMove(message: EventNativeMouseMove)

    public fun sendSnapshot(message: SendSnapshot)

    public fun addNewEmailAddress(message: AddNewEmailAddress)

    public fun changeEmailAddress(message: ChangeEmailAddress)

    public fun unnamedLobbyRequest(message: UnnamedLobbyRequest)

    public fun messageQuickchatPublic(message: MessageQuickchatPublic)

    public fun messageQuickchatPrivate(message: MessageQuickchatPrivate)

    public fun storeServerPermVarcs(message: StoreServerPermVarcs)

    public fun clientPreferences(message: ClientPreferences)

    public fun unknownClientOpcode(message: UnknownClientPacket)
}
