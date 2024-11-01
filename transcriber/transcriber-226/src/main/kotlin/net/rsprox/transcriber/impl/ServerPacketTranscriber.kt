package net.rsprox.transcriber.impl

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
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetV1
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetV2
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
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.SetNpcUpdateOrigin
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV3
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
import net.rsprox.protocol.game.outgoing.model.misc.client.HideObjOps
import net.rsprox.protocol.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.game.outgoing.model.misc.client.HiscoreReply
import net.rsprox.protocol.game.outgoing.model.misc.client.MinimapToggle
import net.rsprox.protocol.game.outgoing.model.misc.client.ReflectionChecker
import net.rsprox.protocol.game.outgoing.model.misc.client.ResetAnims
import net.rsprox.protocol.game.outgoing.model.misc.client.ResetInteractionMode
import net.rsprox.protocol.game.outgoing.model.misc.client.SendPing
import net.rsprox.protocol.game.outgoing.model.misc.client.ServerTickEnd
import net.rsprox.protocol.game.outgoing.model.misc.client.SetHeatmapEnabled
import net.rsprox.protocol.game.outgoing.model.misc.client.SetInteractionMode
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
import net.rsprox.protocol.game.outgoing.model.specific.LocAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.MapAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcHeadIconSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcSpotAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.PlayerAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.PlayerSpotAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV3
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownString
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
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjCustomise
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjDel
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjEnabledOps
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjUncustomise
import net.rsprox.protocol.game.outgoing.model.zone.payload.SoundArea

public interface ServerPacketTranscriber {
    public fun camLookAt(message: CamLookAt)

    public fun camLookAtEasedCoord(message: CamLookAtEasedCoord)

    public fun camMode(message: CamMode)

    public fun camMoveTo(message: CamMoveTo)

    public fun camMoveToArc(message: CamMoveToArc)

    public fun camMoveToCycles(message: CamMoveToCycles)

    public fun camReset(message: CamReset)

    public fun camRotateBy(message: CamRotateBy)

    public fun camRotateTo(message: CamRotateTo)

    public fun camShake(message: CamShake)

    public fun camSmoothReset(message: CamSmoothReset)

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

    public fun worldEntityInfoV3(message: WorldEntityInfoV3)

    public fun ifClearInv(message: IfClearInv)

    public fun ifCloseSub(message: IfCloseSub)

    public fun ifMoveSub(message: IfMoveSub)

    public fun ifOpenSub(message: IfOpenSub)

    public fun ifOpenTop(message: IfOpenTop)

    public fun ifResync(message: IfResync)

    public fun ifSetAngle(message: IfSetAngle)

    public fun ifSetAnim(message: IfSetAnim)

    public fun ifSetColour(message: IfSetColour)

    public fun ifSetEvents(message: IfSetEvents)

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

    public fun rebuildWorldEntity(message: RebuildWorldEntity)

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

    public fun updateRebootTimer(message: UpdateRebootTimer)

    public fun updateUid192(message: UpdateUid192)

    public fun urlOpen(message: UrlOpen)

    public fun chatFilterSettings(message: ChatFilterSettings)

    public fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat)

    public fun messageGame(message: MessageGame)

    public fun runClientScript(message: RunClientScript)

    public fun setMapFlag(message: SetMapFlag)

    public fun setPlayerOp(message: SetPlayerOp)

    public fun triggerOnDialogAbort(message: TriggerOnDialogAbort)

    public fun updateRunEnergy(message: UpdateRunEnergy)

    public fun updateRunWeight(message: UpdateRunWeight)

    public fun updateStatV2(message: UpdateStatV2)

    public fun updateStatV1(message: UpdateStatV1)

    public fun updateStockMarketSlot(message: UpdateStockMarketSlot)

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

    public fun projAnimSpecificV3(message: ProjAnimSpecificV3)

    public fun varpLarge(message: VarpLarge)

    public fun varpReset(message: VarpReset)

    public fun varpSmall(message: VarpSmall)

    public fun varpSync(message: VarpSync)

    public fun clearEntities(message: ClearEntities)

    public fun setActiveWorld(message: SetActiveWorld)

    public fun updateZoneFullFollows(message: UpdateZoneFullFollows)

    public fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed)

    public fun updateZonePartialFollows(message: UpdateZonePartialFollows)

    public fun locAddChange(message: LocAddChange)

    public fun locAnim(message: LocAnim)

    public fun locDel(message: LocDel)

    public fun locMerge(message: LocMerge)

    public fun mapAnim(message: MapAnim)

    public fun mapProjAnim(message: MapProjAnim)

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
