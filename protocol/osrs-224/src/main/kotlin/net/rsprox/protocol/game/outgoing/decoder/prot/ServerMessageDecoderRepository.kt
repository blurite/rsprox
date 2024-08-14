package net.rsprox.protocol.game.outgoing.decoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ProtRepository
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.game.outgoing.decoder.codec.map.ReconnectDecoder

public object ServerMessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): MessageDecoderRepository<GameServerProt> {
        val protRepository = ProtRepository.of<GameServerProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                // bind(CamRotateToDecoder())
                // bind(CamRotateBy())
                // bind(CamLookAtEasedCoordDecoder())
                // bind(CamLookAtDecoder())
                // bind(CamModeDecoder())
                // bind(CamMoveToArcDecoder())
                // bind(CamMoveToCyclesDecoder())
                // bind(CamMoveToDecoder())
                // bind(CamResetDecoder())
                // bind(CamShakeDecoder())
                // bind(CamSmoothResetDecoder())
                // bind(CamTargetDecoder())
                // bind(CamTargetOldDecoder())
                // bind(OculusSyncDecoder())

                // bind(ClanChannelDeltaDecoder())
                // bind(ClanChannelFullDecoder())
                // bind(ClanSettingsDeltaDecoder())
                // bind(ClanSettingsFullDecoder())
                // bind(MessageClanChannelDecoder(huffmanCodec))
                // bind(MessageClanChannelSystemDecoder(huffmanCodec))
                // bind(VarClanDisableDecoder())
                // bind(VarClanEnableDecoder())
                // bind(VarClanDecoder())

                // bind(MessageFriendChannelDecoder(huffmanCodec))
                // bind(UpdateFriendChatChannelFullV1Decoder())
                // bind(UpdateFriendChatChannelFullV2Decoder())
                // bind(UpdateFriendChatChannelSingleUserDecoder())

                // bind(PlayerInfoDecoder())
                // bind(NpcInfoSmallDecoder())
                // bind(NpcInfoLargeDecoder())
                // bind(SetNpcUpdateOriginDecoder())
                // bind(WorldEntityInfoDecoder())

                // bind(IfClearInvDecoder())
                // bind(IfCloseSubDecoder())
                // bind(IfResyncDecoder())
                // bind(IfMoveSubDecoder())
                // bind(IfOpenSubDecoder())
                // bind(IfOpenTopDecoder())
                // bind(IfSetAngleDecoder())
                // bind(IfSetAnimDecoder())
                // bind(IfSetColourDecoder())
                // bind(IfSetEventsDecoder())
                // bind(IfSetHideDecoder())
                // bind(IfSetModelDecoder())
                // bind(IfSetNpcHeadActiveDecoder())
                // bind(IfSetNpcHeadDecoder())
                // bind(IfSetObjectDecoder())
                // bind(IfSetPlayerHeadDecoder())
                // bind(IfSetPlayerModelBaseColourDecoder())
                // bind(IfSetPlayerModelBodyTypeDecoder())
                // bind(IfSetPlayerModelObjDecoder())
                // bind(IfSetPlayerModelSelfDecoder())
                // bind(IfSetPositionDecoder())
                // bind(IfSetRotateSpeedDecoder())
                // bind(IfSetScrollPosDecoder())
                // bind(IfSetTextDecoder())

                // bind(UpdateInvFullDecoder())
                // bind(UpdateInvPartialDecoder())
                // bind(UpdateInvStopTransmitDecoder())

                // bind(LogoutDecoder())
                // bind(LogoutTransferDecoder())
                // bind(LogoutWithReasonDecoder())

                bind(ReconnectDecoder())
                // bind(StaticRebuildDecoder(huffmanCodec, cache))
                // bind(RebuildRegionDecoder())
                // bind(RebuildWorldEntityDecoder())

                // bind(SetHeatmapEnabledDecoder())
                // bind(HideLocOpsDecoder())
                // bind(HideNpcOpsDecoder())
                // bind(HidePlayerOpsDecoder())
                // bind(HintArrowDecoder())
                // bind(HiscoreReplyDecoder())
                // bind(MinimapToggleDecoder())
                // bind(ReflectionCheckerDecoder())
                // bind(ResetAnimsDecoder())
                // bind(SendPingDecoder())
                // bind(ServerTickEndDecoder())
                // bind(UpdateRebootTimerDecoder())
                // bind(SiteSettingsDecoder())
                // bind(UpdateUid192Decoder())
                // bind(UrlOpenDecoder())

                // bind(ChatFilterSettingsDecoder())
                // bind(ChatFilterSettingsPrivateChatDecoder())
                // bind(MessageGameDecoder())
                // bind(RunClientScriptDecoder())
                // bind(SetMapFlagDecoder())
                // bind(SetPlayerOpDecoder())
                // bind(TriggerOnDialogAbortDecoder())
                // bind(UpdateRunEnergyDecoder())
                // bind(UpdateRunWeightDecoder())
                // bind(UpdateStatDecoder())
                // bind(UpdateStatOldDecoder())
                // bind(UpdateStockMarketSlotDecoder())
                // bind(UpdateTradingPostDecoder())

                // bind(FriendListLoadedDecoder())
                // bind(MessagePrivateEchoDecoder(huffmanCodec))
                // bind(MessagePrivateDecoder(huffmanCodec))
                // bind(UpdateFriendListDecoder())
                // bind(UpdateIgnoreListDecoder())

                // bind(MidiJingleDecoder())
                // bind(MidiSongDecoder())
                // bind(MidiSongOldDecoder())
                // bind(MidiSongStopDecoder())
                // bind(MidiSongWithSecondaryDecoder())
                // bind(MidiSwapDecoder())
                // bind(SynthSoundDecoder())

                // bind(LocAnimSpecificDecoder())
                // bind(MapAnimSpecificDecoder())
                // bind(NpcAnimSpecificDecoder())
                // bind(NpcHeadIconSpecificDecoder())
                // bind(NpcSpotAnimSpecificDecoder())
                // bind(PlayerAnimSpecificDecoder())
                // bind(PlayerSpotAnimSpecificDecoder())
                // bind(ProjAnimSpecificDecoder())

                // bind(VarpLargeDecoder())
                // bind(VarpResetDecoder())
                // bind(VarpSmallDecoder())
                // bind(VarpSyncDecoder())

                // bind(ClearEntitiesDecoder())
                // bind(SetActiveWorldDecoder())

                // bind(UpdateZonePartialEnclosedDecoder())
                // bind(UpdateZoneFullFollowsDecoder())
                // bind(UpdateZonePartialFollowsDecoder())

                // bind(LocAddChangeDecoder())
                // bind(LocAnimDecoder())
                // bind(LocDelDecoder())
                // bind(LocMergeDecoder())
                // bind(MapAnimDecoder())
                // bind(MapProjAnimDecoder())
                // bind(ObjAddDecoder())
                // bind(ObjCountDecoder())
                // bind(ObjDelDecoder())
                // bind(ObjEnabledOpsDecoder())
                // bind(SoundAreaDecoder())
            }
        return builder.build()
    }
}
