package net.rsprox.transcriber.interfaces

import net.rsprox.protocol.game.outgoing.model.camera.*
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
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.*
import net.rsprox.protocol.game.outgoing.model.interfaces.*
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvFull
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvPartial
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvStopTransmit
import net.rsprox.protocol.game.outgoing.model.logout.Logout
import net.rsprox.protocol.game.outgoing.model.logout.LogoutTransfer
import net.rsprox.protocol.game.outgoing.model.logout.LogoutWithReason
import net.rsprox.protocol.game.outgoing.model.map.*
import net.rsprox.protocol.game.outgoing.model.misc.client.*
import net.rsprox.protocol.game.outgoing.model.misc.player.AccountFlags
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettings
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettingsPrivateChat
import net.rsprox.protocol.game.outgoing.model.misc.player.MessageGame
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript
import net.rsprox.protocol.game.outgoing.model.misc.player.SetMapFlagV1
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
import net.rsprox.protocol.game.outgoing.model.specific.*
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownString
import net.rsprox.protocol.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.game.outgoing.model.varp.VarpReset
import net.rsprox.protocol.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.game.outgoing.model.varp.VarpSync
import net.rsprox.protocol.game.outgoing.model.worldentity.ClearEntities
import net.rsprox.protocol.game.outgoing.model.worldentity.SetActiveWorldV1
import net.rsprox.protocol.game.outgoing.model.worldentity.SetActiveWorldV2
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.game.outgoing.model.zone.payload.*

public interface ServerPacketTranscriber {
    public fun camLookAt(message: CamLookAtV1)

    public fun camLookAtEasedCoord(message: CamLookAtEasedCoordV1)

    public fun camMode(message: CamMode)

    public fun camMoveTo(message: CamMoveToV1)

    public fun camMoveToArc(message: CamMoveToArcV1)

    public fun camMoveToCycles(message: CamMoveToCyclesV1)

    public fun camReset(message: CamReset)

    public fun camRotateBy(message: CamRotateBy)

    public fun camRotateTo(message: CamRotateTo)

    public fun camShake(message: CamShake)

    public fun camSmoothReset(message: CamSmoothReset)

    public fun camTargetV3(message: CamTargetV3)

    public fun camTargetV2(message: CamTargetV2)

    public fun camTargetV1(message: CamTargetV1)

    public fun oculusSync(message: OculusSync)

    public fun clanChannelDelta(message: ClanChannelDelta)

    public fun clanChannelFull(message: ClanChannelFull)

    public fun clanSettingsDelta(message: ClanSettingsDelta)

    public fun clanSettingsFull(message: ClanSettingsFull)

    public fun messageClanChannel(message: MessageClanChannel)

    public fun messageClanChannelSystem(message: MessageClanChannelSystem)

    public fun varClan(message: VarClan)

    public fun varClanDisable(message: VarClanDisable)

    public fun varClanEnable(message: VarClanEnable)

    public fun messageFriendChannel(message: MessageFriendChannel)

    public fun updateFriendChatChannelFullV1(message: UpdateFriendChatChannelFullV1)

    public fun updateFriendChatChannelFullV2(message: UpdateFriendChatChannelFullV2)

    public fun updateFriendChatChannelSingleUser(message: UpdateFriendChatChannelSingleUser)

    public fun setNpcUpdateOrigin(message: SetNpcUpdateOrigin)

    public fun worldEntityInfoV1(message: WorldEntityInfoV1)

    public fun worldEntityInfoV2(message: WorldEntityInfoV2)

    public fun worldEntityInfoV3(message: WorldEntityInfoV3)

    public fun worldEntityInfoV4(message: WorldEntityInfoV4)

    public fun worldEntityInfoV5(message: WorldEntityInfoV5)

    public fun worldEntityInfoV6(message: WorldEntityInfoV6)

    public fun worldEntityInfoV7(message: WorldEntityInfoV7)

    public fun ifClearInv(message: IfClearInv)

    public fun ifCloseSub(message: IfCloseSub)

    public fun ifMoveSub(message: IfMoveSub)

    public fun ifOpenSub(message: IfOpenSub)

    public fun ifOpenTop(message: IfOpenTop)

    public fun ifResyncV1(message: IfResyncV1)

    public fun ifResyncV2(message: IfResyncV2)

    public fun ifSetAngle(message: IfSetAngle)

    public fun ifSetAnim(message: IfSetAnim)

    public fun ifSetColour(message: IfSetColour)

    public fun ifSetEventsV1(message: IfSetEventsV1)

    public fun ifSetEventsV2(message: IfSetEventsV2)

    public fun ifSetHide(message: IfSetHide)

    public fun ifSetModel(message: IfSetModel)

    public fun ifSetNpcHead(message: IfSetNpcHead)

    public fun ifSetNpcHeadActive(message: IfSetNpcHeadActive)

    public fun ifSetObject(message: IfSetObject)

    public fun ifSetPlayerHead(message: IfSetPlayerHead)

    public fun ifSetPlayerModelBaseColour(message: IfSetPlayerModelBaseColour)

    public fun ifSetPlayerModelBodyType(message: IfSetPlayerModelBodyType)

    public fun ifSetPlayerModelObj(message: IfSetPlayerModelObj)

    public fun ifSetPlayerModelSelf(message: IfSetPlayerModelSelf)

    public fun ifSetPosition(message: IfSetPosition)

    public fun ifSetRotateSpeed(message: IfSetRotateSpeed)

    public fun ifSetScrollPos(message: IfSetScrollPos)

    public fun ifSetText(message: IfSetText)

    public fun updateInvFull(message: UpdateInvFull)

    public fun updateInvPartial(message: UpdateInvPartial)

    public fun updateInvStopTransmit(message: UpdateInvStopTransmit)

    public fun logout(message: Logout)

    public fun logoutTransfer(message: LogoutTransfer)

    public fun logoutWithReason(message: LogoutWithReason)

    public fun reconnect(message: Reconnect)

    public fun rebuildLogin(message: RebuildLogin)

    public fun rebuildNormal(message: RebuildNormal)

    public fun rebuildRegion(message: RebuildRegion)

    public fun rebuildWorldEntityV1(message: RebuildWorldEntityV1)

    public fun rebuildWorldEntityV2(message: RebuildWorldEntityV2)

    public fun rebuildWorldEntityV3(message: RebuildWorldEntityV3)

    public fun hideLocOps(message: HideLocOps)

    public fun hideNpcOps(message: HideNpcOps)

    public fun hideObjOps(message: HideObjOps)

    public fun hintArrow(message: HintArrow)

    public fun hiscoreReply(message: HiscoreReply)

    public fun minimapToggle(message: MinimapToggle)

    public fun reflectionChecker(message: ReflectionChecker)

    public fun resetAnims(message: ResetAnims)

    public fun sendPing(message: SendPing)

    public fun serverTickEnd(message: ServerTickEnd)

    public fun setHeatmapEnabled(message: SetHeatmapEnabled)

    public fun siteSettings(message: SiteSettings)

    public fun updateRebootTimer(message: UpdateRebootTimerV1)

    public fun updateUid192(message: UpdateUid192)

    public fun urlOpen(message: UrlOpen)

    public fun packetGroupStart(message: PacketGroupStart)

    public fun packetGroupEnd(message: PacketGroupEnd)

    public fun zbuf(message: ZBuf)

    public fun chatFilterSettings(message: ChatFilterSettings)

    public fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat)

    public fun messageGame(message: MessageGame)

    public fun runClientScript(message: RunClientScript)

    public fun setMapFlag(message: SetMapFlagV1)

    public fun setPlayerOp(message: SetPlayerOp)

    public fun triggerOnDialogAbort(message: TriggerOnDialogAbort)

    public fun updateRunEnergy(message: UpdateRunEnergy)

    public fun updateRunWeight(message: UpdateRunWeight)

    public fun updateStatV2(message: UpdateStatV2)

    public fun updateStatV1(message: UpdateStatV1)

    public fun updateStockMarketSlot(message: UpdateStockMarketSlot)

    public fun accountFlags(message: AccountFlags)

    public fun updateTradingPost(message: UpdateTradingPost)

    public fun friendListLoaded(message: FriendListLoaded)

    public fun messagePrivate(message: MessagePrivate)

    public fun messagePrivateEcho(message: MessagePrivateEcho)

    public fun updateFriendList(message: UpdateFriendList)

    public fun updateIgnoreList(message: UpdateIgnoreList)

    public fun midiJingle(message: MidiJingle)

    public fun midiSongV2(message: MidiSongV2)

    public fun midiSongV1(message: MidiSongV1)

    public fun midiSongStop(message: MidiSongStop)

    public fun midiSongWithSecondary(message: MidiSongWithSecondary)

    public fun midiSwap(message: MidiSwap)

    public fun synthSound(message: SynthSound)

    public fun locAnimSpecific(message: LocAnimSpecific)

    public fun mapAnimSpecific(message: MapAnimSpecific)

    public fun npcAnimSpecific(message: NpcAnimSpecific)

    public fun npcHeadIconSpecific(message: NpcHeadIconSpecific)

    public fun npcSpotAnimSpecific(message: NpcSpotAnimSpecific)

    public fun playerAnimSpecific(message: PlayerAnimSpecific)

    public fun playerSpotAnimSpecific(message: PlayerSpotAnimSpecific)

    public fun objAddSpecific(message: ObjAddSpecific)

    public fun objDelSpecific(message: ObjDelSpecific)

    public fun objEnabledOpsSpecific(message: ObjEnabledOpsSpecific)

    public fun objUncustomiseSpecific(message: ObjUncustomiseSpecific)

    public fun objCountSpecific(message: ObjCountSpecific)

    public fun objCustomiseSpecific(message: ObjCustomiseSpecific)

    public fun projAnimSpecificV2(message: ProjAnimSpecificV2)

    public fun projAnimSpecificV3(message: ProjAnimSpecificV3)

    public fun projAnimSpecificV4(message: ProjAnimSpecificV4)

    public fun varpLarge(message: VarpLarge)

    public fun varpReset(message: VarpReset)

    public fun varpSmall(message: VarpSmall)

    public fun varpSync(message: VarpSync)

    public fun clearEntities(message: ClearEntities)

    public fun setActiveWorldV1(message: SetActiveWorldV1)

    public fun setActiveWorldV2(message: SetActiveWorldV2)

    public fun updateZoneFullFollows(message: UpdateZoneFullFollows)

    public fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed)

    public fun updateZonePartialFollows(message: UpdateZonePartialFollows)

    public fun locAddChangeV1(message: LocAddChangeV1)

    public fun locAddChangeV2(message: LocAddChangeV2)

    public fun locAnim(message: LocAnim)

    public fun locDel(message: LocDel)

    public fun locMerge(message: LocMerge)

    public fun mapAnim(message: MapAnim)

    public fun mapProjAnimV1(message: MapProjAnimV1)

    public fun mapProjAnimV2(message: MapProjAnimV2)

    public fun objAdd(message: ObjAdd)

    public fun objCount(message: ObjCount)

    public fun objDel(message: ObjDel)

    public fun objEnabledOps(message: ObjEnabledOps)

    public fun soundArea(message: SoundArea)

    public fun unknownString(message: UnknownString)

    public fun objCustomise(message: ObjCustomise)

    public fun objUncustomise(message: ObjUncustomise)

    public fun setInteractionMode(message: SetInteractionMode)

    public fun resetInteractionMode(message: ResetInteractionMode)
}
