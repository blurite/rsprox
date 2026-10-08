package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ProtRepository
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.account.CreateAccountReplyDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.account.CreateCheckEmailReplyDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.account.CreateCheckNameReplyDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.account.CreateSuggestNameErrorDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.account.CreateSuggestNameReplyDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.account.FriendlistLoadedDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.account.UpdateDobDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.appearance.LobbyAppearanceDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.appearance.PlayerSnapshotDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera.Cam2EnableDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera.CamForceAngleDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera.CamLookAtDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera.CamMoveToDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera.CamRemoveRoofDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera.CamResetDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera.CamShakeDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera.CamSmoothResetDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera.CameraUpdateDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.clan.ClanChannelDeltaDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.clan.ClanChannelFullDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.clan.ClanSettingsDeltaDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.clan.ClanSettingsFullDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.debug.DbFilterDebugDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.group.PlayerGroupDeltaDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.group.PlayerGroupFullDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.group.PlayerGroupVarpsDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.info.npcinfo.NpcInfoClient
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.info.playerinfo.PlayerInfoDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfCloseSubDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfMoveSubDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfOpenSubActiveLocDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfOpenSubActiveNpcDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfOpenSubActiveObjDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfOpenSubActivePlayerDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfOpenSubDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfOpenTopDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetAngleDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetAnimDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetClickMaskDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetColourDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetEventsDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetGraphicDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetHideDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetHttpImageDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetModelDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetNpcHeadDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetObjectDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetObjectLongV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetPlayerHeadDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetPlayerHeadIgnoreWornDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetPlayerHeadOtherDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetPlayerHeadSnapshotDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelOtherDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelSelfDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelSnapshotDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetPositionDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetRecolDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetRetexDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetScrollPosDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetTargetParamDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetTextAntiMacroDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetTextDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces.IfSetTextFontDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.inv.UpdateInvFullDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.inv.UpdateInvPartialDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.inv.UpdateInvStopTransmitDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.EnvironmentOverrideDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.RebuildNormalDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.RebuildRegionDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.ReconnectDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.lighting.PointLightAttenuationFalloffDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.lighting.PointLightColourDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.lighting.PointLightEnabledDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.lighting.PointLightExtendAboveDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.lighting.PointLightExtendBelowDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.lighting.PointLightIntensityScaleDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.map.lighting.PointLightShadowDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.ChangeLobbyDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.ConsoleFeedbackDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.Cutscene2dPlayDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.DebugServerTriggersDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.DoCheatDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.ExecuteClientCheatDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.HintArrowDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.HintTrailDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.Js5ReloadDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.LogoutDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.LogoutFullDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.LogoutTransferDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.MinimapToggleDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.NoTimeoutDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.ResetAnimsDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.SendPingDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.SetDrawOrderDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.ShowFaceHereDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.StoreResetDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.StoreServerpermVarcsAckDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.SyncClockDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.TickEndDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.TriggerOnDialogAbortDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.Unnamed1Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.Unnamed2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.UpdateRebootTimerDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.client.UpdateUid192Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.ChatFilterSettingsDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.ChatFilterSettingsPrivateChatDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.ClearPlayerSnapshotDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.JcoinsUpdateDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.LastLoginInfoDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.LoyaltyUpdateDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.MessageGameDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.RunClientScriptDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.SetMapFlagDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.SetMoveActionDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.SetNpcAttackPriorityDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.SetPlayerAttackPriorityDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.SetPlayerOpDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.SetTargetDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.UpdateRunEnergyDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.UpdateRunWeightDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.UpdateStatDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player.UpdateStockmarketSlotV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.selection.LocSelectAddDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.selection.LocSelectClearDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.selection.LocSelectConfigureDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.selection.SetLocOpOverrideDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessageClanchannelDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessageClanchannelSystemDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessageFriendchannelDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessagePlayerGroupDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessagePrivateDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessagePrivateEchoDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessagePublicDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessageQuickchatClanchannelDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessageQuickchatFriendchatDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessageQuickchatPlayerGroupDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessageQuickchatPrivateDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.MessageQuickchatPrivateEchoDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.SocialNetworkLogoutDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.UpdateFriendchatChannelFullDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.UpdateFriendchatChannelSingleUserDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.UpdateFriendlistDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.UpdateIgnorelistDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social.UrlOpenDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.MidiJingleDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.MidiSongDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.MidiSongStopDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.SongPreloadDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.SoundMixbussAddDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.SoundMixbussSetLevelDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.SoundStopDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.SynthSoundDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.VorbisPreloadSoundsDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.VorbisSoundDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.VorbisSoundGroupDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.VorbisSoundGroupStartDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.VorbisSoundGroupStopDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.VorbisSpeechSoundDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.sound.VorbisSpeechStopDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific.LocAnimSpecificDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific.NpcAnimSpecificDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific.NpcHeadiconSpecificDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific.NpcSaySpecificDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific.PlayerAnimSpecificDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific.ProjAnimSpecificDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific.ProjAnimSpecificV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific.SpotanimSpecificDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific.SpotanimSpecificV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryClearGridValueDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridAddColumnDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridAddGroupDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridAddRowDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridFullDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridMoveColumnDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridMoveRowDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridRemoveColumnDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridRemoveGroupDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridRemoveRowDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridSetRowPinnedDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry.TelemetryGridValuesDeltaDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varbit.VarbitDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varbit.VarbitLargeDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varbit.VarbitSmallDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc.ResetClientVarcacheDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc.VarcBitDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc.VarcBitLargeDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc.VarcBitSmallDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc.VarcLargeDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc.VarcLongDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc.VarcSmallDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc.VarcStrLargeDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc.VarcStrSmallDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varclan.VarclanDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varclan.VarclanDisableDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varclan.VarclanEnableDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varp.VarpLargeDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varp.VarpLongDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varp.VarpSmallDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.world.WorldlistFetchReplyDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.header.UpdateZoneFullFollowsDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.header.UpdateZonePartialEnclosedDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.header.UpdateZonePartialFollowsDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.LocAddChangeDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.LocAnimDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.LocCustomiseDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.LocDelDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.LocPrefetchDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapAnimV1Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapAnimV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MidiSongLocationDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjAddV3Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjCountV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjCountV3Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjDelDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.SoundAreaV1Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.StandaloneObjAddV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.TextCoordDecoder

internal object ServerMessageDecoderRepository {
    @ExperimentalStdlibApi
    fun build(
        huffmanCodec: HuffmanCodec? = null,
        cipher: () -> StreamCipher? = { null },
    ): MessageDecoderRepository<GameServerProt> =
        MessageDecoderRepositoryBuilder(ProtRepository.of<GameServerProt>())
            .apply {
                bind(ChatFilterSettingsPrivateChatDecoder())
                bind(UpdateUid192Decoder())
                bind(Cutscene2dPlayDecoder())
                bind(CamShakeDecoder())
                bind(CamLookAtDecoder())
                bind(SoundMixbussSetLevelDecoder())
                bind(CamMoveToDecoder())
                bind(TriggerOnDialogAbortDecoder())
                bind(CamSmoothResetDecoder())
                bind(CamForceAngleDecoder())
                bind(FriendlistLoadedDecoder())
                bind(MidiJingleDecoder())
                bind(MidiSongDecoder())
                bind(MidiSongStopDecoder())
                bind(SongPreloadDecoder())
                bind(SoundMixbussAddDecoder())
                bind(VorbisPreloadSoundsDecoder())
                bind(VorbisSoundGroupDecoder())
                bind(VorbisSoundGroupStartDecoder())
                bind(VorbisSoundGroupStopDecoder())
                bind(VorbisSpeechSoundDecoder())
                bind(VorbisSpeechStopDecoder())
                bind(ObjAddV3Decoder())
                bind(ObjCountV2Decoder())
                bind(ObjDelDecoder())
                bind(LocPrefetchDecoder())
                bind(LocAnimDecoder())
                bind(LocAddChangeDecoder())
                bind(LocCustomiseDecoder())
                bind(LocDelDecoder())
                bind(MapAnimV1Decoder())
                bind(MapAnimV2Decoder())
                bind(MapProjAnimDecoder())
                bind(MapProjAnimV2Decoder())
                bind(MapProjAnimHalfsqDecoder())
                bind(MapProjAnimHalfsqV2Decoder())
                bind(SoundAreaV1Decoder())
                bind(ObjCountV3Decoder())
                bind(UpdateZonePartialEnclosedDecoder())
                bind(UpdateZonePartialFollowsDecoder())
                bind(SetPlayerAttackPriorityDecoder())
                bind(SetNpcAttackPriorityDecoder())
                bind(SetTargetDecoder())
                bind(UpdateRunEnergyDecoder())
                bind(UpdateRunWeightDecoder())
                bind(ChatFilterSettingsDecoder())
                bind(UpdateRebootTimerDecoder())
                bind(CamResetDecoder())
                bind(Cam2EnableDecoder())
                bind(MinimapToggleDecoder())
                bind(SetDrawOrderDecoder())
                bind(ShowFaceHereDecoder())
                bind(IfSetPlayerHeadSnapshotDecoder())
                bind(IfSetPlayerModelSnapshotDecoder())
                bind(IfSetPlayerHeadIgnoreWornDecoder())
                bind(IfSetPlayerHeadOtherDecoder())
                bind(IfSetPlayerHeadDecoder())
                bind(IfSetNpcHeadDecoder())
                bind(IfSetModelDecoder())
                bind(IfSetPlayerModelSelfDecoder())
                bind(IfSetPlayerModelOtherDecoder())
                bind(IfSetAngleDecoder())
                bind(IfSetTextAntiMacroDecoder())
                bind(IfSetClickMaskDecoder())
                bind(IfSetColourDecoder())
                bind(IfSetTextFontDecoder())
                bind(IfSetAnimDecoder())
                bind(IfSetGraphicDecoder())
                bind(IfSetHideDecoder())
                bind(IfSetScrollPosDecoder())
                bind(IfSetPositionDecoder())
                bind(IfSetEventsDecoder())
                bind(IfSetTargetParamDecoder())
                bind(IfSetRecolDecoder())
                bind(IfSetRetexDecoder())
                bind(VarpSmallDecoder())
                bind(VarpLargeDecoder())
                bind(VarpLongDecoder())
                bind(VarcSmallDecoder())
                bind(VarcLargeDecoder())
                bind(VarcLongDecoder())
                bind(VarbitSmallDecoder())
                bind(VarbitLargeDecoder())
                bind(VarcBitSmallDecoder())
                bind(VarcBitLargeDecoder())
                bind(UpdateDobDecoder())
                bind(CreateSuggestNameErrorDecoder())
                bind(CreateCheckNameReplyDecoder())
                bind(CreateCheckEmailReplyDecoder())
                bind(CreateAccountReplyDecoder())
                bind(VarclanEnableDecoder())
                bind(VarclanDisableDecoder())
                bind(LogoutDecoder())
                bind(LogoutFullDecoder())
                bind(LoyaltyUpdateDecoder())
                bind(LastLoginInfoDecoder())
                bind(JcoinsUpdateDecoder())
                bind(SyncClockDecoder())
                bind(StoreServerpermVarcsAckDecoder())
                bind(StoreResetDecoder())
                bind(CamRemoveRoofDecoder())
                bind(UpdateInvStopTransmitDecoder())
                bind(ResetClientVarcacheDecoder())
                bind(SetMapFlagDecoder())
                bind(ResetAnimsDecoder())
                bind(SetMoveActionDecoder())
                bind(TelemetryClearGridValueDecoder())
                bind(TelemetryGridMoveColumnDecoder())
                bind(TelemetryGridMoveRowDecoder())
                bind(TelemetryGridSetRowPinnedDecoder())
                bind(TelemetryGridAddColumnDecoder())
                bind(TelemetryGridAddGroupDecoder())
                bind(TelemetryGridAddRowDecoder())
                bind(TelemetryGridRemoveColumnDecoder())
                bind(TelemetryGridRemoveGroupDecoder())
                bind(TelemetryGridRemoveRowDecoder())
                bind(TelemetryGridValuesDeltaDecoder())
                bind(TelemetryGridFullDecoder())
                bind(SetPlayerOpDecoder())
                bind(SetLocOpOverrideDecoder())
                bind(IfSetTextDecoder())
                bind(VarcStrSmallDecoder())
                bind(VarcStrLargeDecoder())
                bind(CreateSuggestNameReplyDecoder())
                bind(ChangeLobbyDecoder())
                bind(LogoutTransferDecoder())
                bind(NpcSaySpecificDecoder())
                bind(NpcAnimSpecificDecoder())
                bind(IfCloseSubDecoder())
                bind(IfSetObjectDecoder())
                bind(IfMoveSubDecoder())
                bind(VarbitDecoder())
                bind(VarcBitDecoder())
                bind(IfSetHttpImageDecoder())
                bind(NpcHeadiconSpecificDecoder())
                bind(IfSetObjectLongV2Decoder())
                bind(PlayerAnimSpecificDecoder())
                bind(IfOpenTopDecoder())
                bind(IfOpenSubDecoder())
                bind(IfOpenSubActiveNpcDecoder())
                bind(IfOpenSubActivePlayerDecoder())
                bind(IfOpenSubActiveObjDecoder())
                bind(PointLightExtendAboveDecoder())
                bind(PointLightExtendBelowDecoder())
                bind(PointLightAttenuationFalloffDecoder())
                bind(PointLightIntensityScaleDecoder())
                bind(PointLightColourDecoder())
                bind(PointLightEnabledDecoder())
                bind(PointLightShadowDecoder())
                bind(ClearPlayerSnapshotDecoder())
                bind(ProjAnimSpecificDecoder())
                bind(ProjAnimSpecificV2Decoder())
                bind(MidiSongLocationDecoder())
                bind(SpotanimSpecificDecoder())
                bind(SpotanimSpecificV2Decoder())
                bind(DebugServerTriggersDecoder())
                bind(LocSelectClearDecoder())
                bind(MessagePrivateEchoDecoder(huffmanCodec))
                bind(MessageQuickchatPrivateEchoDecoder())
                bind(Unnamed1Decoder())
                bind(Unnamed2Decoder())
                bind(Js5ReloadDecoder())
                bind(IfOpenSubActiveLocDecoder())
                bind(MessagePrivateDecoder(huffmanCodec))
                bind(MessageQuickchatPrivateDecoder())
                bind(MessagePlayerGroupDecoder(huffmanCodec))
                bind(MessageFriendchannelDecoder(huffmanCodec))
                bind(SendPingDecoder())
                bind(MessageQuickchatFriendchatDecoder())
                bind(MessageQuickchatPlayerGroupDecoder())
                bind(MessageQuickchatClanchannelDecoder())
                bind(MessageClanchannelDecoder(huffmanCodec))
                bind(MessageClanchannelSystemDecoder(huffmanCodec))
                bind(UpdateZoneFullFollowsDecoder())
                bind(UpdateInvFullDecoder())
                bind(UpdateInvPartialDecoder())
                bind(UpdateFriendchatChannelFullDecoder())
                bind(LocAnimSpecificDecoder())
                bind(LocSelectAddDecoder())
                bind(UpdateFriendchatChannelSingleUserDecoder())
                bind(ClanChannelFullDecoder())
                bind(ExecuteClientCheatDecoder())
                bind(MessageGameDecoder())
                bind(UpdateStatDecoder())
                bind(UpdateStockmarketSlotV2Decoder())
                bind(LocSelectConfigureDecoder())
                bind(SoundStopDecoder())
                bind(WorldlistFetchReplyDecoder())
                bind(TickEndDecoder(GameServerProt.SERVER_TICK_END))
                bind(ClanChannelDeltaDecoder())
                bind(PlayerGroupVarpsDecoder())
                bind(DoCheatDecoder())
                bind(HintTrailDecoder())
                bind(RunClientScriptDecoder())
                bind(MessagePublicDecoder(huffmanCodec))
                bind(SocialNetworkLogoutDecoder(cipher))
                bind(UpdateFriendlistDecoder())
                bind(UpdateIgnorelistDecoder())
                bind(UrlOpenDecoder(cipher))
                bind(VarclanDecoder())
                bind(HintArrowDecoder())
                bind(StandaloneObjAddV2Decoder())
                bind(ClanSettingsFullDecoder())
                bind(ConsoleFeedbackDecoder())
                bind(DbFilterDebugDecoder())
                bind(LobbyAppearanceDecoder())
                bind(PlayerSnapshotDecoder())
                bind(PlayerGroupDeltaDecoder())
                bind(PlayerGroupFullDecoder())
                bind(ClanSettingsDeltaDecoder())
                bind(EnvironmentOverrideDecoder())
                bind(TextCoordDecoder())
                bind(CameraUpdateDecoder())
                bind(PlayerInfoDecoder())
                bind(NpcInfoClient())
                bind(RebuildNormalDecoder())
                bind(RebuildRegionDecoder())
                bind(SynthSoundDecoder())
                bind(VorbisSoundDecoder())
                bind(NoTimeoutDecoder())
                bind(ReconnectDecoder())
            }.build()
}
