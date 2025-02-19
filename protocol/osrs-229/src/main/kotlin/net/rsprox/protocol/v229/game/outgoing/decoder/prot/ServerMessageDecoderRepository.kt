package net.rsprox.protocol.v229.game.outgoing.decoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ProtRepository
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamLookAtDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamLookAtEasedCoordDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamModeDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamMoveToArcDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamMoveToCyclesDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamMoveToDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamResetDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamRotateBy
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamRotateToDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamShakeDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamSmoothResetDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.CamTargetV3Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.camera.OculusSyncDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.clan.ClanChannelDeltaDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.clan.ClanChannelFullDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.clan.ClanSettingsDeltaDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.clan.ClanSettingsFullDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.clan.MessageClanChannelDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.clan.MessageClanChannelSystemDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.clan.VarClanDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.clan.VarClanDisableDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.clan.VarClanEnableDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.friendchat.MessageFriendChannelDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.friendchat.UpdateFriendChatChannelFullV2Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.friendchat.UpdateFriendChatChannelSingleUserDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.info.*
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.info.NpcInfoLargeV5Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.info.NpcInfoSmallV5Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.info.PlayerInfoDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.info.SetNpcUpdateOriginDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfClearInvDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfCloseSubDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfMoveSubDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfOpenSubDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfOpenTopDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfResyncDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetAngleDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetAnimDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetColourDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetEventsDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetHideDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetModelDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetNpcHeadActiveDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetNpcHeadDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetObjectDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetPlayerHeadDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelBaseColourDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelBodyTypeDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelObjDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelSelfDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetPositionDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetRotateSpeedDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetScrollPosDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces.IfSetTextDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.inv.UpdateInvFullDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.inv.UpdateInvPartialDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.inv.UpdateInvStopTransmitDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.logout.LogoutDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.logout.LogoutTransferDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.logout.LogoutWithReasonDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.map.RebuildRegionDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.map.RebuildWorldEntityV3Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.map.ReconnectDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.map.StaticRebuildDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.*
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.HideLocOpsDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.HideNpcOpsDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.HideObjOpsDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.HintArrowDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.HiscoreReplyDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.MinimapToggleDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.ReflectionCheckerDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.ResetAnimsDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.ResetInteractionModeDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.SendPingDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.ServerTickEndDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.SetHeatmapEnabledDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.SetInteractionModeDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.SiteSettingsDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.UpdateRebootTimerDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.UpdateUid192Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client.UrlOpenDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.ChatFilterSettingsDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.ChatFilterSettingsPrivateChatDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.MessageGameDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.RunClientScriptDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.SetMapFlagDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.SetPlayerOpDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.TriggerOnDialogAbortDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.UpdateRunEnergyDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.UpdateRunWeightDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.UpdateStatV2Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.UpdateStockMarketSlotDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.player.UpdateTradingPostDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.social.FriendListLoadedDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.social.MessagePrivateDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.social.MessagePrivateEchoDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.social.UpdateFriendListDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.social.UpdateIgnoreListDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.sound.MidiJingleDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.sound.MidiSongStopDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.sound.MidiSongV2Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.sound.MidiSongWithSecondaryDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.sound.MidiSwapDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.sound.SynthSoundDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.specific.LocAnimSpecificDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.specific.MapAnimSpecificDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.specific.NpcAnimSpecificDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.specific.NpcHeadIconSpecificDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.specific.NpcSpotAnimSpecificDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.specific.PlayerAnimSpecificDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.specific.PlayerSpotAnimSpecificDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.specific.ProjAnimSpecificV3Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.unknown.UnknownStringDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.varp.VarpLargeDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.varp.VarpResetDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.varp.VarpSmallDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.varp.VarpSyncDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.worldentity.ClearEntitiesDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.worldentity.SetActiveWorldV1Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.worldentity.SetActiveWorldV2Decoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.header.UpdateZoneFullFollowsDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.header.UpdateZonePartialEnclosedDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.header.UpdateZonePartialFollowsDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.*
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.LocAnimDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.LocDelDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.LocMergeDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.MapAnimDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.MapProjAnimDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.ObjAddDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.ObjCountDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.ObjCustomiseDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.ObjDelDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.ObjEnabledOpsDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.ObjUncustomiseDecoder
import net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload.SoundAreaDecoder

internal object ServerMessageDecoderRepository {
    @ExperimentalStdlibApi
    fun build(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): MessageDecoderRepository<GameServerProt> {
        val protRepository = ProtRepository.of<GameServerProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(CamRotateToDecoder())
                bind(CamRotateBy())
                bind(CamLookAtEasedCoordDecoder())
                bind(CamLookAtDecoder())
                bind(CamModeDecoder())
                bind(CamMoveToArcDecoder())
                bind(CamMoveToCyclesDecoder())
                bind(CamMoveToDecoder())
                bind(CamResetDecoder())
                bind(CamShakeDecoder())
                bind(CamSmoothResetDecoder())
                bind(CamTargetV3Decoder())
                bind(OculusSyncDecoder())

                bind(ClanChannelDeltaDecoder())
                bind(ClanChannelFullDecoder())
                bind(ClanSettingsDeltaDecoder())
                bind(ClanSettingsFullDecoder())
                bind(MessageClanChannelDecoder(huffmanCodec))
                bind(MessageClanChannelSystemDecoder(huffmanCodec))
                bind(VarClanDisableDecoder())
                bind(VarClanEnableDecoder())
                bind(VarClanDecoder())

                bind(MessageFriendChannelDecoder(huffmanCodec))
                bind(UpdateFriendChatChannelFullV2Decoder())
                bind(UpdateFriendChatChannelSingleUserDecoder())

                bind(PlayerInfoDecoder())
                bind(NpcInfoSmallV5Decoder())
                bind(NpcInfoLargeV5Decoder())
                bind(SetNpcUpdateOriginDecoder())
                bind(WorldEntityInfoV4Decoder())
                bind(WorldEntityInfoV5Decoder())

                bind(IfClearInvDecoder())
                bind(IfCloseSubDecoder())
                bind(IfResyncDecoder())
                bind(IfMoveSubDecoder())
                bind(IfOpenSubDecoder())
                bind(IfOpenTopDecoder())
                bind(IfSetAngleDecoder())
                bind(IfSetAnimDecoder())
                bind(IfSetColourDecoder())
                bind(IfSetEventsDecoder())
                bind(IfSetHideDecoder())
                bind(IfSetModelDecoder())
                bind(IfSetNpcHeadActiveDecoder())
                bind(IfSetNpcHeadDecoder())
                bind(IfSetObjectDecoder())
                bind(IfSetPlayerHeadDecoder())
                bind(IfSetPlayerModelBaseColourDecoder())
                bind(IfSetPlayerModelBodyTypeDecoder())
                bind(IfSetPlayerModelObjDecoder())
                bind(IfSetPlayerModelSelfDecoder())
                bind(IfSetPositionDecoder())
                bind(IfSetRotateSpeedDecoder())
                bind(IfSetScrollPosDecoder())
                bind(IfSetTextDecoder())

                bind(UpdateInvFullDecoder())
                bind(UpdateInvPartialDecoder())
                bind(UpdateInvStopTransmitDecoder())

                bind(LogoutDecoder())
                bind(LogoutTransferDecoder())
                bind(LogoutWithReasonDecoder())

                bind(ReconnectDecoder())
                bind(StaticRebuildDecoder(huffmanCodec, cache))
                bind(RebuildRegionDecoder())
                bind(RebuildWorldEntityV3Decoder())

                bind(SetHeatmapEnabledDecoder())
                bind(HideLocOpsDecoder())
                bind(HideNpcOpsDecoder())
                bind(HideObjOpsDecoder())
                bind(SetInteractionModeDecoder())
                bind(ResetInteractionModeDecoder())
                bind(HintArrowDecoder())
                bind(HiscoreReplyDecoder())
                bind(MinimapToggleDecoder())
                bind(ReflectionCheckerDecoder())
                bind(ResetAnimsDecoder())
                bind(SendPingDecoder())
                bind(ServerTickEndDecoder())
                bind(UpdateRebootTimerDecoder())
                bind(SiteSettingsDecoder())
                bind(UpdateUid192Decoder())
                bind(UrlOpenDecoder())
                bind(PacketGroupStartDecoder())
                bind(PacketGroupEndDecoder())

                bind(ChatFilterSettingsDecoder())
                bind(ChatFilterSettingsPrivateChatDecoder())
                bind(MessageGameDecoder())
                bind(RunClientScriptDecoder())
                bind(SetMapFlagDecoder())
                bind(SetPlayerOpDecoder())
                bind(TriggerOnDialogAbortDecoder())
                bind(UpdateRunEnergyDecoder())
                bind(UpdateRunWeightDecoder())
                bind(UpdateStatV2Decoder())
                bind(UpdateStockMarketSlotDecoder())
                bind(UpdateTradingPostDecoder())

                bind(FriendListLoadedDecoder())
                bind(MessagePrivateEchoDecoder(huffmanCodec))
                bind(MessagePrivateDecoder(huffmanCodec))
                bind(UpdateFriendListDecoder())
                bind(UpdateIgnoreListDecoder())

                bind(MidiJingleDecoder())
                bind(MidiSongV2Decoder())
                bind(MidiSongStopDecoder())
                bind(MidiSongWithSecondaryDecoder())
                bind(MidiSwapDecoder())
                bind(SynthSoundDecoder())

                bind(LocAnimSpecificDecoder())
                bind(MapAnimSpecificDecoder())
                bind(NpcAnimSpecificDecoder())
                bind(NpcHeadIconSpecificDecoder())
                bind(NpcSpotAnimSpecificDecoder())
                bind(PlayerAnimSpecificDecoder())
                bind(PlayerSpotAnimSpecificDecoder())
                bind(ProjAnimSpecificV3Decoder())

                bind(VarpLargeDecoder())
                bind(VarpResetDecoder())
                bind(VarpSmallDecoder())
                bind(VarpSyncDecoder())

                bind(ClearEntitiesDecoder())
                bind(SetActiveWorldV1Decoder())
                bind(SetActiveWorldV2Decoder())

                bind(UpdateZonePartialEnclosedDecoder())
                bind(UpdateZoneFullFollowsDecoder())
                bind(UpdateZonePartialFollowsDecoder())

                bind(LocAddChangeV2Decoder())
                bind(LocAnimDecoder())
                bind(LocDelDecoder())
                bind(LocMergeDecoder())
                bind(MapAnimDecoder())
                bind(MapProjAnimDecoder())
                bind(ObjAddDecoder())
                bind(ObjCountDecoder())
                bind(ObjDelDecoder())
                bind(ObjEnabledOpsDecoder())
                bind(SoundAreaDecoder())
                bind(ObjCustomiseDecoder())
                bind(ObjUncustomiseDecoder())

                bind(UnknownStringDecoder())
            }
        return builder.build()
    }
}
