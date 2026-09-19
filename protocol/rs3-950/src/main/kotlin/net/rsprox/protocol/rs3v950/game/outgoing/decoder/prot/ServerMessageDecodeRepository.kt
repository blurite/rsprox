package net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ProtRepository
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account.CreateAccountReplyDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account.CreateCheckEmailReplyDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account.CreateCheckNameReplyDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account.CreateSuggestNameErrorDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account.CreateSuggestNameReplyDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account.FriendlistLoadedDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account.UpdateDobDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.appearance.LobbyAppearanceDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.appearance.PlayerSnapshotDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.camera.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.clan.ClanChannelDeltaDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.clan.ClanChannelFullDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.clan.ClanSettingsDeltaDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.clan.ClanSettingsFullDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.debug.DbFilterDebugDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.group.PlayerGroupDeltaDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.group.PlayerGroupFullDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.group.PlayerGroupVarpsDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo.NpcInfoClient
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.PlayerInfoDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.inv.UpdateInvFullDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.inv.UpdateInvPartialDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.inv.UpdateInvStopTransmitDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.EnvironmentOverrideDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.RebuildNormalDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.RebuildRegionDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.ReconnectDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting.PointLightAttenuationFalloffDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting.PointLightColourDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting.PointLightEnabledDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting.PointLightExtendAboveDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting.PointLightExtendBelowDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting.PointLightIntensityScaleDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.lighting.PointLightShadowDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.selection.LocSelectAddDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.selection.LocSelectClearDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.selection.LocSelectConfigureDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.selection.SetLocOpOverrideDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessageClanchannelDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessageClanchannelSystemDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessageFriendchannelDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessagePlayerGroupDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessagePrivateDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessagePrivateEchoDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessagePublicDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessageQuickchatClanchannelDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessageQuickchatFriendchatDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessageQuickchatPlayerGroupDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessageQuickchatPrivateDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.MessageQuickchatPrivateEchoDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.SocialNetworkLogoutDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.UpdateFriendchatChannelFullDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.UpdateFriendchatChannelSingleUserDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.UpdateFriendlistDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.UpdateIgnorelistDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social.UrlOpenDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.MidiJingleDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.MidiSongDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.MidiSongStopDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.SongPreloadDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.SoundMixbussAddDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.SoundMixbussSetLevelDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.SoundStopDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.SynthSoundDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.VorbisPreloadSoundsDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.VorbisSoundDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.VorbisSoundGroupDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.VorbisSoundGroupStartDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.VorbisSoundGroupStopDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.VorbisSpeechSoundDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.VorbisSpeechStopDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific.LocAnimSpecificDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific.NpcAnimSpecificDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific.NpcHeadiconSpecificDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific.NpcSaySpecificDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific.PlayerAnimSpecificDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific.ProjAnimSpecificDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific.ProjAnimSpecificV2Decoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific.SpotanimSpecificDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific.SpotanimSpecificV2Decoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryClearGridValueDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridAddColumnDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridAddGroupDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridAddRowDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridFullDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridMoveColumnDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridMoveRowDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridRemoveColumnDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridRemoveGroupDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridRemoveRowDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridSetRowPinnedDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry.TelemetryGridValuesDeltaDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varbit.VarbitDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varbit.VarbitLargeDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varbit.VarbitSmallDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varc.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varclan.VarclanDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varclan.VarclanDisableDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varclan.VarclanEnableDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varp.VarpLargeDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varp.VarpLongDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varp.VarpSmallDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.world.WorldlistFetchReplyDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.header.UpdateZoneFullFollowsDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.header.UpdateZonePartialEnclosedDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.header.UpdateZonePartialFollowsDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload.*

internal object ServerMessageDecoderRepository {
    @ExperimentalStdlibApi
    fun build(
        huffmanCodec: HuffmanCodec,
        cipher: () -> StreamCipher?,
    ): MessageDecoderRepository<GameServerProt> {
        val protRepository = ProtRepository.of<GameServerProt>()
        val npcInfoClient = NpcInfoClient()

        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(MessagePublicDecoder(huffmanCodec))
                bind(PlayerGroupFullDecoder())
                bind(PlayerGroupVarpsDecoder())
                bind(PlayerGroupDeltaDecoder())
                bind(MessageQuickchatPrivateDecoder())
                bind(MessageQuickchatPrivateEchoDecoder())
                bind(MessageQuickchatClanchannelDecoder())
                bind(MessageQuickchatFriendchatDecoder())
                bind(MessageQuickchatPlayerGroupDecoder())
                bind(VarclanDecoder())
                bind(LobbyAppearanceDecoder())
                bind(PlayerSnapshotDecoder())
                bind(MessagePrivateEchoDecoder(huffmanCodec))
                bind(MessagePrivateDecoder(huffmanCodec))
                bind(MessageClanchannelDecoder(huffmanCodec))
                bind(MessageFriendchannelDecoder(huffmanCodec))
                bind(MessagePlayerGroupDecoder(huffmanCodec))
                bind(MessageClanchannelSystemDecoder(huffmanCodec))
                bind(DbFilterDebugDecoder())
                bind(UrlOpenDecoder(cipher))
                bind(SocialNetworkLogoutDecoder(cipher))
                bind(WorldlistFetchReplyDecoder())
                bind(CreateSuggestNameReplyDecoder())
                bind(NpcSaySpecificDecoder())
                bind(DoCheatDecoder())
                bind(LogoutTransferDecoder())
                bind(ChangeLobbyDecoder())
                bind(SetLocOpOverrideDecoder())
                bind(ProjAnimSpecificDecoder())
                bind(DebugServerTriggersDecoder())
                bind(Unnamed1Decoder())
                bind(Unnamed2Decoder())
                bind(UpdateFriendlistDecoder())
                bind(UpdateIgnorelistDecoder())
                bind(UpdateFriendchatChannelFullDecoder())
                bind(UpdateFriendchatChannelSingleUserDecoder())
                bind(TelemetryGridValuesDeltaDecoder())
                bind(TelemetryGridFullDecoder())
                bind(LocSelectAddDecoder())
                bind(RebuildRegionDecoder())
                bind(ConsoleFeedbackDecoder())
                bind(UpdateStockmarketSlotV2Decoder())
                bind(EnvironmentOverrideDecoder())
                bind(ClanChannelFullDecoder())
                bind(ClanChannelDeltaDecoder())
                bind(ClanSettingsFullDecoder())
                bind(ClanSettingsDeltaDecoder())
                bind(LogoutDecoder())
                bind(FriendlistLoadedDecoder())
                bind(VarclanEnableDecoder())
                bind(VarclanDisableDecoder())
                bind(ResetAnimsDecoder())
                bind(LocSelectClearDecoder())
                bind(StoreServerpermVarcsAckDecoder())
                bind(Js5ReloadDecoder())
                bind(SiteSettingsDecoder())
                bind(StoreResetDecoder())
                bind(CreateCheckNameReplyDecoder())
                bind(CreateCheckEmailReplyDecoder())
                bind(CreateAccountReplyDecoder())
                bind(CreateSuggestNameErrorDecoder())
                bind(UpdateDobDecoder())
                bind(ExecuteClientCheatDecoder())
                bind(ClearPlayerSnapshotDecoder())
                bind(TelemetryGridAddColumnDecoder())
                bind(TelemetryGridRemoveRowDecoder())
                bind(TelemetryGridSetRowPinnedDecoder())
                bind(TelemetryGridMoveColumnDecoder())
                bind(TelemetryGridAddGroupDecoder())
                bind(TelemetryGridMoveRowDecoder())
                bind(TelemetryGridAddRowDecoder())
                bind(TelemetryClearGridValueDecoder())
                bind(TelemetryGridRemoveGroupDecoder())
                bind(TelemetryGridRemoveColumnDecoder())
                bind(SendPingDecoder())
                bind(UpdateUid192Decoder())
                bind(SetMapFlagDecoder())
                bind(LocSelectConfigureDecoder())
                bind(LocAnimSpecificDecoder())
                bind(NpcAnimSpecificDecoder())
                bind(PlayerAnimSpecificDecoder())
                bind(NpcHeadiconSpecificDecoder())
                bind(SpotanimSpecificDecoder())
                bind(SpotanimSpecificV2Decoder())
                bind(SetMoveActionDecoder())
                bind(IfSetPlayerHeadIgnoreWornDecoder())
                bind(IfSetPlayerHeadOtherDecoder())
                bind(IfSetPlayerModelOtherDecoder())
                bind(IfSetAngleDecoder())
                bind(IfSetTextAntiMacroDecoder())
                bind(IfSetClickMaskDecoder())
                bind(IfSetTextFontDecoder())
                bind(IfSetGraphicDecoder())
                bind(IfSetRecolDecoder())
                bind(IfSetRetexDecoder())
                bind(IfMoveSubDecoder())
                bind(IfSetHttpImageDecoder())
                bind(IfSetObjectLongV2Decoder())
                bind(SetNpcAttackPriorityDecoder())
                bind(SetPlayerAttackPriorityDecoder())
                bind(SetTargetDecoder())
                bind(LoyaltyUpdateDecoder())
                bind(SyncClockDecoder())
                bind(CamRemoveRoofDecoder())
                bind(PointLightExtendAboveDecoder())
                bind(PointLightExtendBelowDecoder())
                bind(PointLightAttenuationFalloffDecoder())
                bind(PointLightIntensityScaleDecoder())
                bind(PointLightColourDecoder())
                bind(PointLightEnabledDecoder())
                bind(PointLightShadowDecoder())
                bind(LastLoginInfoDecoder())
                bind(UpdateRebootTimerDecoder())
                bind(ChatFilterSettingsDecoder())
                bind(LogoutFullDecoder())
                bind(CamResetDecoder())
                bind(Cam2EnableDecoder())
                bind(CamSmoothResetDecoder())
                bind(ShowFaceHereDecoder())
                bind(SetDrawOrderDecoder())
                bind(TriggerOnDialogAbortDecoder())
                bind(CameraUpdateDecoder())
                bind(CamForceAngleDecoder())
                bind(CamLookAtDecoder())
                bind(CamMoveToDecoder())
                bind(CamShakeDecoder())
                bind(npcInfoClient)
                bind(PlayerInfoDecoder())
                bind(IfCloseSubDecoder())
                bind(IfOpenSubActiveLocDecoder())
                bind(IfOpenSubActiveObjDecoder())
                bind(IfOpenSubDecoder())
                bind(IfOpenSubActiveNpcDecoder())
                bind(IfOpenSubActivePlayerDecoder())
                bind(IfOpenTopDecoder())
                bind(IfSetAnimDecoder())
                bind(IfSetColourDecoder())
                bind(IfSetEventsDecoder())
                bind(IfSetHideDecoder())
                bind(IfSetModelDecoder())
                bind(IfSetNpcHeadDecoder())
                bind(IfSetObjectDecoder())
                bind(IfSetPlayerHeadDecoder())
                bind(IfSetPlayerHeadSnapshotDecoder())
                bind(IfSetPlayerModelSelfDecoder())
                bind(IfSetPlayerModelSnapshotDecoder())
                bind(IfSetPositionDecoder())
                bind(IfSetScrollPosDecoder())
                bind(IfSetTargetParamDecoder())
                bind(IfSetTextDecoder())
                bind(UpdateInvFullDecoder())
                bind(UpdateInvPartialDecoder())
                bind(UpdateInvStopTransmitDecoder())
                bind(RebuildNormalDecoder())
                bind(ReconnectDecoder())
                bind(Cutscene2dPlayDecoder())
                bind(HintArrowDecoder())
                bind(HintTrailDecoder())
                bind(MinimapToggleDecoder())
                bind(ChatFilterSettingsPrivateChatDecoder())
                bind(JcoinsUpdateDecoder())
                bind(MessageGameDecoder())
                bind(RunClientScriptDecoder())
                bind(SetPlayerOpDecoder())
                bind(UpdateRunEnergyDecoder())
                bind(UpdateRunWeightDecoder())
                bind(UpdateStatDecoder())
                bind(VorbisSoundDecoder())
                bind(VorbisSpeechStopDecoder())
                bind(MidiSongStopDecoder())
                bind(MidiJingleDecoder())
                bind(SoundStopDecoder())
                bind(SoundMixbussSetLevelDecoder())
                bind(MidiSongDecoder())
                bind(SongPreloadDecoder())
                bind(VorbisSoundGroupStopDecoder())
                bind(VorbisSoundGroupStartDecoder())
                bind(SoundMixbussAddDecoder())
                bind(VorbisPreloadSoundsDecoder())
                bind(VorbisSoundGroupDecoder())
                bind(VorbisSpeechSoundDecoder())
                bind(SynthSoundDecoder())
                bind(ProjAnimSpecificV2Decoder())
                bind(VarbitLargeDecoder())
                bind(VarbitDecoder())
                bind(VarbitSmallDecoder())
                bind(VarcBitLargeDecoder())
                bind(VarcBitSmallDecoder())
                bind(VarcLargeDecoder())
                bind(VarcSmallDecoder())
                bind(VarcStrSmallDecoder())
                bind(VarcStrLargeDecoder())
                bind(VarcLongDecoder())
                bind(ResetClientVarcacheDecoder())
                bind(VarcBitDecoder())
                bind(VarpLargeDecoder())
                bind(VarpLongDecoder())
                bind(VarpSmallDecoder())
                bind(UpdateZoneFullFollowsDecoder())
                bind(UpdateZonePartialEnclosedDecoder())
                bind(UpdateZonePartialFollowsDecoder())
                bind(LocAddChangeDecoder())
                bind(LocAnimDecoder())
                bind(LocCustomiseDecoder())
                bind(LocDelDecoder())
                bind(LocPrefetchDecoder())
                bind(MapAnimV1Decoder())
                bind(MapAnimV2Decoder())
                bind(MapProjAnimDecoder())
                bind(MapProjAnimHalfsqDecoder())
                bind(MapProjAnimHalfsqV2Decoder())
                bind(MapProjAnimV2Decoder())
                bind(MidiSongLocationDecoder())
                bind(ObjAddDecoder())
                bind(ObjCountDecoder())
                bind(ObjDelDecoder())
                bind(SoundAreaV1Decoder())
                bind(TextCoordDecoder())
                bind(NoTimeoutDecoder())
                bind(TickEndDecoder(GameServerProt.SERVER_TICK_END))
            }
        return builder.build()
    }
}
