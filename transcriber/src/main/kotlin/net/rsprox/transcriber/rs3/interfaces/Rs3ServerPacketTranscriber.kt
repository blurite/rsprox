package net.rsprox.transcriber.rs3.interfaces

import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatClanchannel
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatFriendchat
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPlayerGroup
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPrivate
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPrivateEcho
import net.rsprox.protocol.rs3.game.outgoing.model.varclan.Varclan

import net.rsprox.protocol.rs3.game.outgoing.model.appearance.LobbyAppearance
import net.rsprox.protocol.rs3.game.outgoing.model.appearance.PlayerSnapshot

import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePublic
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupFull
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupDelta
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupVarps
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePrivateEcho
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePrivate
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageClanchannel
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageFriendchannel
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePlayerGroup
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageClanchannelSystem
import net.rsprox.protocol.rs3.game.outgoing.model.debug.DbFilterDebug
import net.rsprox.protocol.rs3.game.outgoing.model.social.UrlOpen
import net.rsprox.protocol.rs3.game.outgoing.model.social.SocialNetworkLogout
import net.rsprox.protocol.game.outgoing.model.misc.client.SiteSettings
import net.rsprox.protocol.rs3.game.outgoing.model.world.WorldlistFetchReply
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Logout
import net.rsprox.protocol.rs3.game.outgoing.model.account.FriendlistLoaded
import net.rsprox.protocol.rs3.game.outgoing.model.varclan.VarclanEnable
import net.rsprox.protocol.rs3.game.outgoing.model.varclan.VarclanDisable
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ResetAnims
import net.rsprox.protocol.rs3.game.outgoing.model.selection.LocSelectClear
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.StoreServerpermVarcsAck
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Js5Reload
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.StoreReset
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateCheckNameReply
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateCheckEmailReply
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateAccountReply
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateSuggestNameError
import net.rsprox.protocol.rs3.game.outgoing.model.account.UpdateDob
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ExecuteClientCheat
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.ClearPlayerSnapshot
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridAddColumn
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridRemoveRow
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridSetRowPinned
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridMoveColumn
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridAddGroup
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridMoveRow
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridAddRow
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryClearGridValue
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridRemoveGroup
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridRemoveColumn
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.SendPing
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.UpdateUid192
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.SetMapFlag
import net.rsprox.protocol.rs3.game.outgoing.model.selection.LocSelectConfigure
import net.rsprox.protocol.rs3.game.outgoing.model.specific.LocAnimSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.NpcAnimSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.PlayerAnimSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.NpcHeadiconSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.SpotanimSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.SpotanimSpecificV2
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.SetMoveAction
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateSuggestNameReply
import net.rsprox.protocol.rs3.game.outgoing.model.specific.NpcSaySpecific
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.DoCheat
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.LogoutTransfer
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ChangeLobby
import net.rsprox.protocol.rs3.game.outgoing.model.selection.SetLocOpOverride
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.DebugServerTriggers
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Unnamed1
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Unnamed2
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateFriendlist
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateIgnorelist
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateFriendchatChannelFull
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateFriendchatChannelSingleUser
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridValuesDelta
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridFull
import net.rsprox.protocol.rs3.game.outgoing.model.selection.LocSelectAdd
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildRegion
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ConsoleFeedback
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.UpdateStockmarketSlotV2
import net.rsprox.protocol.rs3.game.outgoing.model.map.EnvironmentOverride
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanChannelFull
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanChannelDelta
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanSettingsFull
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanSettingsDelta
import net.rsprox.protocol.game.outgoing.model.misc.client.MinimapToggle
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettingsPrivateChat
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownServerPacket
import net.rsprox.protocol.rs3.game.outgoing.model.camera.*
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.*
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvFull
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvPartial
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvStopTransmit
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.rs3.game.outgoing.model.map.Reconnect
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.SyncClock
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightExtendAbove
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightExtendBelow
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightAttenuationFalloff
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightIntensityScale
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightColour
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightEnabled
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightShadow
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Cutscene2dPlay
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintTrail
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.LogoutFull
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.NoTimeout
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.SetDrawOrder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ShowFaceHere
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.TickEnd
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.TriggerOnDialogAbort
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.UpdateRebootTimer
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.*
import net.rsprox.protocol.rs3.game.outgoing.model.sound.MidiJingle
import net.rsprox.protocol.rs3.game.outgoing.model.sound.MidiSong
import net.rsprox.protocol.rs3.game.outgoing.model.sound.MidiSongStop
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SongPreload
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SoundMixbussAdd
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SoundMixbussSetLevel
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SoundStop
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SynthSound
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisPreloadSounds
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSound
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSoundGroup
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSoundGroupStart
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSoundGroupStop
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSpeechSound
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSpeechStop
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.Varbit
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.VarbitLarge
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.VarbitSmall
import net.rsprox.protocol.rs3.game.outgoing.model.varc.*
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpLong
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.*

public interface Rs3ServerPacketTranscriber {
    public fun lastLoginInfo(message: LastLoginInfo)

    public fun updateRebootTimer(message: UpdateRebootTimer)

    public fun chatFilterSettings(message: ChatFilterSettings)

    public fun logoutFull(message: LogoutFull)

    public fun camReset(message: CamReset)

    public fun cam2Enable(message: Cam2Enable)

    public fun camSmoothReset(message: CamSmoothReset)

    public fun showFaceHere(message: ShowFaceHere)

    public fun setDrawOrder(message: SetDrawOrder)

    public fun triggerOnDialogAbort(message: TriggerOnDialogAbort)

    public fun resetClientVarcache(message: ResetClientVarcache)

    public fun ifOpenSubActiveNpc(message: IfOpenSubActiveNpc)

    public fun ifOpenSubActivePlayer(message: IfOpenSubActivePlayer)

    public fun varbit(message: Varbit)

    public fun varcBit(message: VarcBit)

    public fun varcLong(message: VarcLong)

    public fun varcStrLarge(message: VarcStrLarge)

    public fun vorbisSpeechStop(message: VorbisSpeechStop)
    public fun midiSongStop(message: MidiSongStop)
    public fun midiJingle(message: MidiJingle)
    public fun soundStop(message: SoundStop)
    public fun soundMixbussSetLevel(message: SoundMixbussSetLevel)
    public fun midiSong(message: MidiSong)
    public fun songPreload(message: SongPreload)
    public fun vorbisSoundGroupStop(message: VorbisSoundGroupStop)
    public fun vorbisSoundGroupStart(message: VorbisSoundGroupStart)
    public fun soundMixbussAdd(message: SoundMixbussAdd)
    public fun vorbisPreloadSounds(message: VorbisPreloadSounds)
    public fun vorbisSoundGroup(message: VorbisSoundGroup)
    public fun vorbisSpeechSound(message: VorbisSpeechSound)
    public fun synthSound(message: SynthSound)
    public fun mapProjAnimHalfsqV2(message: MapProjAnimHalfsqV2)

    public fun mapProjAnimHalfsq(message: MapProjAnimHalfsq)

    public fun mapProjAnimV2(message: MapProjAnimV2)

    public fun mapProjAnim(message: MapProjAnim)

    public fun soundAreaV2(message: SoundAreaV2)

    public fun soundAreaV1(message: SoundAreaV1)

    public fun varpSmall(message: VarpSmall)

    public fun varpLarge(message: VarpLarge)

    public fun varpLong(message: VarpLong)

    public fun varbitSmall(message: VarbitSmall)

    public fun varbitLarge(message: VarbitLarge)

    public fun ifOpenTop(message: IfOpenTop)

    public fun ifOpenSub(message: IfOpenSub)

    public fun ifCloseSub(message: IfCloseSub)

    public fun ifSetHide(message: IfSetHide)

    public fun messagePublic(message: MessagePublic)

    public fun playerGroupFull(message: PlayerGroupFull)

    public fun playerGroupDelta(message: PlayerGroupDelta)

    public fun playerGroupVarps(message: PlayerGroupVarps)

    public fun messagePrivateEcho(message: MessagePrivateEcho)

    public fun messagePrivate(message: MessagePrivate)

    public fun messageClanchannel(message: MessageClanchannel)

    public fun messageFriendchannel(message: MessageFriendchannel)

    public fun messagePlayerGroup(message: MessagePlayerGroup)

    public fun messageClanchannelSystem(message: MessageClanchannelSystem)

    public fun dbFilterDebug(message: DbFilterDebug)

    public fun urlOpen(message: UrlOpen)

    public fun socialNetworkLogout(message: SocialNetworkLogout)

    public fun siteSettings(message: SiteSettings)

    public fun worldlistFetchReply(message: WorldlistFetchReply)

    public fun messageGame(message: MessageGame)

    public fun rebuildNormal(message: RebuildNormal)

    public fun reconnect(message: Reconnect)

    public fun updateZoneFullFollows(message: UpdateZoneFullFollows)

    public fun updateZonePartialFollows(message: UpdateZonePartialFollows)

    public fun locAnim(message: LocAnim)

    public fun locAddChange(message: LocAddChange)

    public fun locCustomise(message: LocCustomise)

    public fun locDel(message: LocDel)

    public fun objAdd(message: ObjAdd)

    public fun objDel(message: ObjDel)

    public fun objCount(message: ObjCount)

    public fun objReveal(message: ObjReveal)

    public fun mapAnim(message: MapAnim)

    public fun mapAnimV1(message: MapAnimV1)

    public fun mapAnimV2(message: MapAnimV2)

    public fun midiSongLocation(message: MidiSongLocation)

    public fun textCoord(message: TextCoord)

    public fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed)

    public fun varcSmall(message: VarcSmall)

    public fun varcLarge(message: VarcLarge)

    public fun varcBitSmall(message: VarcBitSmall)

    public fun varcBitLarge(message: VarcBitLarge)

    public fun varcStrSmall(message: VarcStrSmall)

    public fun ifSetNpcHead(message: IfSetNpcHead)

    public fun ifSetPlayerHead(message: IfSetPlayerHead)

    public fun ifSetText(message: IfSetText)

    public fun ifSetObject(message: IfSetObject)

    public fun ifOpenSubActiveObj(message: IfOpenSubActiveObj)

    public fun ifOpenSubActiveLoc(message: IfOpenSubActiveLoc)

    public fun ifSetModel(message: IfSetModel)

    public fun ifSetPosition(message: IfSetPosition)

    public fun ifSetAnim(message: IfSetAnim)

    public fun ifSetColour(message: IfSetColour)

    public fun ifSetScrollPos(message: IfSetScrollPos)

    public fun ifSetPlayerModelSelf(message: IfSetPlayerModelSelf)

    public fun ifSetTargetParam(message: IfSetTargetParam)

    public fun ifSetEvents(message: IfSetEvents)

    public fun camLookAt(message: CamLookAt)

    public fun camShake(message: CamShake)

    public fun camForceAngle(message: CamForceAngle)

    public fun camMoveTo(message: CamMoveTo)

    public fun noTimeout(message: NoTimeout)

    public fun tickEnd(message: TickEnd)

    public fun ifSetPlayerHeadIgnoreWorn(message: IfSetPlayerHeadIgnoreWorn)

    public fun ifSetPlayerHeadOther(message: IfSetPlayerHeadOther)

    public fun ifSetPlayerModelOther(message: IfSetPlayerModelOther)

    public fun ifSetAngle(message: IfSetAngle)

    public fun ifSetTextAntiMacro(message: IfSetTextAntiMacro)

    public fun ifSetClickMask(message: IfSetClickMask)

    public fun ifSetTextFont(message: IfSetTextFont)

    public fun ifSetGraphic(message: IfSetGraphic)

    public fun ifSetRecol(message: IfSetRecol)

    public fun ifSetRetex(message: IfSetRetex)

    public fun ifMoveSub(message: IfMoveSub)

    public fun ifSetHttpImage(message: IfSetHttpImage)

    public fun ifSetObjectLongV2(message: IfSetObjectLongV2)

    public fun setNpcAttackPriority(message: SetNpcAttackPriority)

    public fun setPlayerAttackPriority(message: SetPlayerAttackPriority)

    public fun setTarget(message: SetTarget)

    public fun loyaltyUpdate(message: LoyaltyUpdate)

    public fun syncClock(message: SyncClock)

    public fun camRemoveRoof(message: CamRemoveRoof)

    public fun pointLightExtendAbove(message: PointLightExtendAbove)

    public fun pointLightExtendBelow(message: PointLightExtendBelow)

    public fun pointLightAttenuationFalloff(message: PointLightAttenuationFalloff)

    public fun pointLightIntensityScale(message: PointLightIntensityScale)

    public fun pointLightColour(message: PointLightColour)

    public fun pointLightEnabled(message: PointLightEnabled)

    public fun pointLightShadow(message: PointLightShadow)

    public fun cameraUpdate(message: CameraUpdate)

    public fun updateInvFull(message: UpdateInvFull)

    public fun updateInvStopTransmit(message: UpdateInvStopTransmit)

    public fun updateInvPartial(message: UpdateInvPartial)

    public fun updateRunWeight(message: UpdateRunWeight)

    public fun updateStat(message: UpdateStat)

    public fun updateRunEnergy(message: UpdateRunEnergy)

    public fun minimapToggle(message: MinimapToggle)

    public fun hintTrail(message: HintTrail)

    public fun hintArrow(message: HintArrow)

    public fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat)

    public fun setPlayerOp(message: SetPlayerOp)

    public fun ifSetPlayerModelSnapshot(message: IfSetPlayerModelSnapshot)

    public fun ifSetPlayerHeadSnapshot(message: IfSetPlayerHeadSnapshot)

    public fun vorbisSound(message: VorbisSound)

    public fun runClientScript(message: RunClientScript)

    public fun projAnimSpecificV2(message: ProjAnimSpecificV2)

    public fun locPrefetch(message: LocPrefetch)

    public fun cutscene2dPlay(message: Cutscene2dPlay)

    public fun jcoinsUpdate(message: JcoinsUpdate)

    public fun logout(message: Logout)

    public fun friendlistLoaded(message: FriendlistLoaded)

    public fun varclanEnable(message: VarclanEnable)

    public fun varclanDisable(message: VarclanDisable)

    public fun resetAnims(message: ResetAnims)

    public fun locSelectClear(message: LocSelectClear)

    public fun storeServerpermVarcsAck(message: StoreServerpermVarcsAck)

    public fun js5Reload(message: Js5Reload)

    public fun storeReset(message: StoreReset)

    public fun createCheckNameReply(message: CreateCheckNameReply)

    public fun createCheckEmailReply(message: CreateCheckEmailReply)

    public fun createAccountReply(message: CreateAccountReply)

    public fun createSuggestNameError(message: CreateSuggestNameError)

    public fun updateDob(message: UpdateDob)

    public fun executeClientCheat(message: ExecuteClientCheat)

    public fun messageQuickchatPrivateServer(message: MessageQuickchatPrivate)

    public fun messageQuickchatPrivateEcho(message: MessageQuickchatPrivateEcho)

    public fun messageQuickchatClanchannel(message: MessageQuickchatClanchannel)

    public fun messageQuickchatFriendchat(message: MessageQuickchatFriendchat)

    public fun messageQuickchatPlayerGroup(message: MessageQuickchatPlayerGroup)

    public fun varclan(message: Varclan)

    public fun lobbyAppearance(message: LobbyAppearance)

    public fun playerSnapshot(message: PlayerSnapshot)

    public fun clearPlayerSnapshot(message: ClearPlayerSnapshot)

    public fun telemetryGridAddColumn(message: TelemetryGridAddColumn)

    public fun telemetryGridRemoveRow(message: TelemetryGridRemoveRow)

    public fun telemetryGridSetRowPinned(message: TelemetryGridSetRowPinned)

    public fun telemetryGridMoveColumn(message: TelemetryGridMoveColumn)

    public fun telemetryGridAddGroup(message: TelemetryGridAddGroup)

    public fun telemetryGridMoveRow(message: TelemetryGridMoveRow)

    public fun telemetryGridAddRow(message: TelemetryGridAddRow)

    public fun telemetryClearGridValue(message: TelemetryClearGridValue)

    public fun telemetryGridRemoveGroup(message: TelemetryGridRemoveGroup)

    public fun telemetryGridRemoveColumn(message: TelemetryGridRemoveColumn)

    public fun sendPing(message: SendPing)

    public fun updateUid192(message: UpdateUid192)

    public fun setMapFlag(message: SetMapFlag)

    public fun locSelectConfigure(message: LocSelectConfigure)

    public fun locAnimSpecific(message: LocAnimSpecific)

    public fun npcAnimSpecific(message: NpcAnimSpecific)

    public fun playerAnimSpecific(message: PlayerAnimSpecific)

    public fun npcHeadiconSpecific(message: NpcHeadiconSpecific)

    public fun spotanimSpecific(message: SpotanimSpecific)

    public fun spotanimSpecificV2(message: SpotanimSpecificV2)

    public fun setMoveAction(message: SetMoveAction)

    public fun createSuggestNameReply(message: CreateSuggestNameReply)

    public fun npcSaySpecific(message: NpcSaySpecific)

    public fun doCheat(message: DoCheat)

    public fun logoutTransfer(message: LogoutTransfer)

    public fun changeLobby(message: ChangeLobby)

    public fun setLocOpOverride(message: SetLocOpOverride)

    public fun projAnimSpecific(message: ProjAnimSpecific)

    public fun debugServerTriggers(message: DebugServerTriggers)

    public fun unnamed1(message: Unnamed1)

    public fun unnamed2(message: Unnamed2)

    public fun updateFriendlist(message: UpdateFriendlist)

    public fun updateIgnorelist(message: UpdateIgnorelist)

    public fun updateFriendchatChannelFull(message: UpdateFriendchatChannelFull)

    public fun updateFriendchatChannelSingleUser(message: UpdateFriendchatChannelSingleUser)

    public fun telemetryGridValuesDelta(message: TelemetryGridValuesDelta)

    public fun telemetryGridFull(message: TelemetryGridFull)

    public fun locSelectAdd(message: LocSelectAdd)

    public fun rebuildRegion(message: RebuildRegion)

    public fun consoleFeedback(message: ConsoleFeedback)

    public fun updateStockmarketSlotV2(message: UpdateStockmarketSlotV2)

    public fun environmentOverride(message: EnvironmentOverride)

    public fun clanChannelFull(message: ClanChannelFull)

    public fun clanChannelDelta(message: ClanChannelDelta)

    public fun clanSettingsFull(message: ClanSettingsFull)

    public fun clanSettingsDelta(message: ClanSettingsDelta)

    public fun unknownServerOpcode(message: UnknownServerPacket)
}
