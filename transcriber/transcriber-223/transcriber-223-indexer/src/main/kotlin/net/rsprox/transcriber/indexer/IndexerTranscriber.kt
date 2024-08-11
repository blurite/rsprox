package net.rsprox.transcriber.indexer

import net.rsprot.protocol.Prot
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
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
import net.rsprox.protocol.game.outgoing.model.map.Reconnect
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
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.indexing.BinaryIndex
import net.rsprox.transcriber.Transcriber
import net.rsprox.transcriber.state.StateTracker

public class IndexerTranscriber private constructor(
    private val stateTracker: StateTracker,
    cacheProvider: CacheProvider,
    override val monitor: SessionMonitor<*>,
    private val binaryIndex: BinaryIndex,
) : Transcriber {
    public constructor(
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
        stateTracker: StateTracker,
        binaryIndex: BinaryIndex,
    ) : this(
        stateTracker,
        cacheProvider,
        monitor,
        binaryIndex,
    )

    override val cache: Cache = cacheProvider.get()

    override fun setCurrentProt(prot: Prot) {
        stateTracker.currentProt = prot
    }

    override fun onTranscribeStart() {
    }

    override fun onTranscribeEnd() {
    }

    override fun if1Button(message: If1Button) {
    }

    override fun if3Button(message: If3Button) {
    }

    override fun ifButtonD(message: IfButtonD) {
    }

    override fun ifButtonT(message: IfButtonT) {
    }

    override fun affinedClanSettingsAddBannedFromChannel(message: AffinedClanSettingsAddBannedFromChannel) {
    }

    override fun affinedClanSettingsSetMutedFromChannel(message: AffinedClanSettingsSetMutedFromChannel) {
    }

    override fun clanChannelFullRequest(message: ClanChannelFullRequest) {
    }

    override fun clanChannelKickUser(message: ClanChannelKickUser) {
    }

    override fun clanSettingsFullRequest(message: ClanSettingsFullRequest) {
    }

    override fun eventAppletFocus(message: EventAppletFocus) {
    }

    override fun eventCameraPosition(message: EventCameraPosition) {
    }

    override fun eventKeyboard(message: EventKeyboard) {
    }

    override fun eventMouseClick(message: EventMouseClick) {
    }

    override fun eventMouseMove(message: EventMouseMove) {
    }

    override fun eventMouseScroll(message: EventMouseScroll) {
    }

    override fun eventNativeMouseClick(message: EventNativeMouseClick) {
    }

    override fun eventNativeMouseMove(message: EventNativeMouseMove) {
    }

    override fun friendChatJoinLeave(message: FriendChatJoinLeave) {
    }

    override fun friendChatKick(message: FriendChatKick) {
    }

    override fun friendChatSetRank(message: FriendChatSetRank) {
    }

    override fun opLoc(message: OpLoc) {
    }

    override fun opLoc6(message: OpLoc6) {
    }

    override fun opLocT(message: OpLocT) {
    }

    override fun messagePrivateClient(message: MessagePrivate) {
    }

    override fun messagePublic(message: MessagePublic) {
    }

    override fun detectModifiedClient(message: DetectModifiedClient) {
    }

    override fun idle(message: Idle) {
    }

    override fun mapBuildComplete(message: MapBuildComplete) {
    }

    override fun membershipPromotionEligibility(message: MembershipPromotionEligibility) {
    }

    override fun noTimeout(message: NoTimeout) {
    }

    override fun reflectionCheckReply(message: ReflectionCheckReply) {
    }

    override fun sendPingReply(message: SendPingReply) {
    }

    override fun soundJingleEnd(message: SoundJingleEnd) {
    }

    override fun connectionTelemetry(message: ConnectionTelemetry) {
    }

    override fun windowStatus(message: WindowStatus) {
    }

    override fun bugReport(message: BugReport) {
    }

    override fun clickWorldMap(message: ClickWorldMap) {
    }

    override fun clientCheat(message: ClientCheat) {
    }

    override fun closeModal(message: CloseModal) {
    }

    override fun hiscoreRequest(message: HiscoreRequest) {
    }

    override fun ifCrmViewClick(message: IfCrmViewClick) {
    }

    override fun moveGameClick(message: MoveGameClick) {
    }

    override fun moveMinimapClick(message: MoveMinimapClick) {
    }

    override fun oculusLeave(message: OculusLeave) {
    }

    override fun sendSnapshot(message: SendSnapshot) {
    }

    override fun setChatFilterSettings(message: SetChatFilterSettings) {
    }

    override fun teleport(message: Teleport) {
    }

    override fun updatePlayerModel(message: UpdatePlayerModel) {
    }

    override fun opNpc(message: OpNpc) {
    }

    override fun opNpc6(message: OpNpc6) {
    }

    override fun opNpcT(message: OpNpcT) {
    }

    override fun opObj(message: OpObj) {
    }

    override fun opObj6(message: OpObj6) {
    }

    override fun opObjT(message: OpObjT) {
    }

    override fun opPlayer(message: OpPlayer) {
    }

    override fun opPlayerT(message: OpPlayerT) {
    }

    override fun resumePauseButton(message: ResumePauseButton) {
    }

    override fun resumePCountDialog(message: ResumePCountDialog) {
    }

    override fun resumePNameDialog(message: ResumePNameDialog) {
    }

    override fun resumePObjDialog(message: ResumePObjDialog) {
    }

    override fun resumePStringDialog(message: ResumePStringDialog) {
    }

    override fun friendListAdd(message: FriendListAdd) {
    }

    override fun friendListDel(message: FriendListDel) {
    }

    override fun ignoreListAdd(message: IgnoreListAdd) {
    }

    override fun ignoreListDel(message: IgnoreListDel) {
    }

    override fun camLookAt(message: CamLookAt) {
    }

    override fun camLookAtEasedCoord(message: CamLookAtEasedCoord) {
    }

    override fun camMode(message: CamMode) {
    }

    override fun camMoveTo(message: CamMoveTo) {
    }

    override fun camMoveToArc(message: CamMoveToArc) {
    }

    override fun camMoveToCycles(message: CamMoveToCycles) {
    }

    override fun camReset(message: CamReset) {
    }

    override fun camRotateBy(message: CamRotateBy) {
    }

    override fun camRotateTo(message: CamRotateTo) {
    }

    override fun camShake(message: CamShake) {
    }

    override fun camSmoothReset(message: CamSmoothReset) {
    }

    override fun camTarget(message: CamTarget) {
    }

    override fun camTargetOld(message: CamTargetOld) {
    }

    override fun oculusSync(message: OculusSync) {
    }

    override fun clanChannelDelta(message: ClanChannelDelta) {
    }

    override fun clanChannelFull(message: ClanChannelFull) {
    }

    override fun clanSettingsDelta(message: ClanSettingsDelta) {
    }

    override fun clanSettingsFull(message: ClanSettingsFull) {
    }

    override fun messageClanChannel(message: MessageClanChannel) {
    }

    override fun messageClanChannelSystem(message: MessageClanChannelSystem) {
    }

    override fun varClan(message: VarClan) {
    }

    override fun varClanDisable(message: VarClanDisable) {
    }

    override fun varClanEnable(message: VarClanEnable) {
    }

    override fun messageFriendChannel(message: MessageFriendChannel) {
    }

    override fun updateFriendChatChannelFullV1(message: UpdateFriendChatChannelFullV1) {
    }

    override fun updateFriendChatChannelFullV2(message: UpdateFriendChatChannelFullV2) {
    }

    override fun updateFriendChatChannelSingleUser(message: UpdateFriendChatChannelSingleUser) {
    }

    override fun setNpcUpdateOrigin(message: SetNpcUpdateOrigin) {
    }

    override fun worldEntityInfo(message: WorldEntityInfo) {
    }

    override fun ifClearInv(message: IfClearInv) {
    }

    override fun ifCloseSub(message: IfCloseSub) {
    }

    override fun ifMoveSub(message: IfMoveSub) {
    }

    override fun ifOpenSub(message: IfOpenSub) {
    }

    override fun ifOpenTop(message: IfOpenTop) {
    }

    override fun ifResync(message: IfResync) {
    }

    override fun ifSetAngle(message: IfSetAngle) {
    }

    override fun ifSetAnim(message: IfSetAnim) {
    }

    override fun ifSetColour(message: IfSetColour) {
    }

    override fun ifSetEvents(message: IfSetEvents) {
    }

    override fun ifSetHide(message: IfSetHide) {
    }

    override fun ifSetModel(message: IfSetModel) {
    }

    override fun ifSetNpcHead(message: IfSetNpcHead) {
    }

    override fun ifSetNpcHeadActive(message: IfSetNpcHeadActive) {
    }

    override fun ifSetObject(message: IfSetObject) {
    }

    override fun ifSetPlayerHead(message: IfSetPlayerHead) {
    }

    override fun ifSetPlayerModelBaseColour(message: IfSetPlayerModelBaseColour) {
    }

    override fun ifSetPlayerModelBodyType(message: IfSetPlayerModelBodyType) {
    }

    override fun ifSetPlayerModelObj(message: IfSetPlayerModelObj) {
    }

    override fun ifSetPlayerModelSelf(message: IfSetPlayerModelSelf) {
    }

    override fun ifSetPosition(message: IfSetPosition) {
    }

    override fun ifSetRotateSpeed(message: IfSetRotateSpeed) {
    }

    override fun ifSetScrollPos(message: IfSetScrollPos) {
    }

    override fun ifSetText(message: IfSetText) {
    }

    override fun updateInvFull(message: UpdateInvFull) {
    }

    override fun updateInvPartial(message: UpdateInvPartial) {
    }

    override fun updateInvStopTransmit(message: UpdateInvStopTransmit) {
    }

    override fun logout(message: Logout) {
    }

    override fun logoutTransfer(message: LogoutTransfer) {
    }

    override fun logoutWithReason(message: LogoutWithReason) {
    }

    override fun reconnect(message: Reconnect) {
    }

    override fun rebuildLogin(message: RebuildLogin) {
    }

    override fun rebuildNormal(message: RebuildNormal) {
    }

    override fun rebuildRegion(message: RebuildRegion) {
    }

    override fun rebuildWorldEntity(message: RebuildWorldEntity) {
    }

    override fun hideLocOps(message: HideLocOps) {
    }

    override fun hideNpcOps(message: HideNpcOps) {
    }

    override fun hidePlayerOps(message: HidePlayerOps) {
    }

    override fun hintArrow(message: HintArrow) {
    }

    override fun hiscoreReply(message: HiscoreReply) {
    }

    override fun minimapToggle(message: MinimapToggle) {
    }

    override fun reflectionChecker(message: ReflectionChecker) {
    }

    override fun resetAnims(message: ResetAnims) {
    }

    override fun sendPing(message: SendPing) {
    }

    override fun serverTickEnd(message: ServerTickEnd) {
    }

    override fun setHeatmapEnabled(message: SetHeatmapEnabled) {
    }

    override fun siteSettings(message: SiteSettings) {
    }

    override fun updateRebootTimer(message: UpdateRebootTimer) {
    }

    override fun updateUid192(message: UpdateUid192) {
    }

    override fun urlOpen(message: UrlOpen) {
    }

    override fun chatFilterSettings(message: ChatFilterSettings) {
    }

    override fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat) {
    }

    override fun messageGame(message: MessageGame) {
    }

    override fun runClientScript(message: RunClientScript) {
    }

    override fun setMapFlag(message: SetMapFlag) {
    }

    override fun setPlayerOp(message: SetPlayerOp) {
    }

    override fun triggerOnDialogAbort(message: TriggerOnDialogAbort) {
    }

    override fun updateRunEnergy(message: UpdateRunEnergy) {
    }

    override fun updateRunWeight(message: UpdateRunWeight) {
    }

    override fun updateStat(message: UpdateStat) {
    }

    override fun updateStatOld(message: UpdateStatOld) {
    }

    override fun updateStockMarketSlot(message: UpdateStockMarketSlot) {
    }

    override fun updateTradingPost(message: UpdateTradingPost) {
    }

    override fun friendListLoaded(message: FriendListLoaded) {
    }

    override fun messagePrivate(message: net.rsprox.protocol.game.outgoing.model.social.MessagePrivate) {
    }

    override fun messagePrivateEcho(message: MessagePrivateEcho) {
    }

    override fun updateFriendList(message: UpdateFriendList) {
    }

    override fun updateIgnoreList(message: UpdateIgnoreList) {
    }

    override fun midiJingle(message: MidiJingle) {
    }

    override fun midiSong(message: MidiSong) {
    }

    override fun midiSongOld(message: MidiSongOld) {
    }

    override fun midiSongStop(message: MidiSongStop) {
    }

    override fun midiSongWithSecondary(message: MidiSongWithSecondary) {
    }

    override fun midiSwap(message: MidiSwap) {
    }

    override fun synthSound(message: SynthSound) {
    }

    override fun locAnimSpecific(message: LocAnimSpecific) {
    }

    override fun mapAnimSpecific(message: MapAnimSpecific) {
    }

    override fun npcAnimSpecific(message: NpcAnimSpecific) {
    }

    override fun npcHeadIconSpecific(message: NpcHeadIconSpecific) {
    }

    override fun npcSpotAnimSpecific(message: NpcSpotAnimSpecific) {
    }

    override fun playerAnimSpecific(message: PlayerAnimSpecific) {
    }

    override fun playerSpotAnimSpecific(message: PlayerSpotAnimSpecific) {
    }

    override fun projAnimSpecific(message: ProjAnimSpecific) {
    }

    override fun varpLarge(message: VarpLarge) {
    }

    override fun varpReset(message: VarpReset) {
    }

    override fun varpSmall(message: VarpSmall) {
    }

    override fun varpSync(message: VarpSync) {
    }

    override fun clearEntities(message: ClearEntities) {
    }

    override fun setActiveWorld(message: SetActiveWorld) {
    }

    override fun updateZoneFullFollows(message: UpdateZoneFullFollows) {
    }

    override fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed) {
    }

    override fun updateZonePartialFollows(message: UpdateZonePartialFollows) {
    }

    override fun locAddChange(message: LocAddChange) {
    }

    override fun locAnim(message: LocAnim) {
    }

    override fun locDel(message: LocDel) {
    }

    override fun locMerge(message: LocMerge) {
    }

    override fun mapAnim(message: MapAnim) {
    }

    override fun mapProjAnim(message: MapProjAnim) {
    }

    override fun objAdd(message: ObjAdd) {
    }

    override fun objCount(message: ObjCount) {
    }

    override fun objDel(message: ObjDel) {
    }

    override fun objEnabledOps(message: ObjEnabledOps) {
    }

    override fun soundArea(message: SoundArea) {
    }

    override fun playerInfo(message: PlayerInfo) {
    }

    override fun npcInfo(message: NpcInfo) {
    }
}
