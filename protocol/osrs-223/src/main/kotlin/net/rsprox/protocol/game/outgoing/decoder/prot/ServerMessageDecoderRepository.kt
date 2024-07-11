package net.rsprox.protocol.game.outgoing.decoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ProtRepository
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamLookAtDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamLookAtEasedAngleAbsoluteDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamLookAtEasedAngleRelativeDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamLookAtEasedCoordDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamModeDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamMoveToDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamMoveToEasedCircularDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamMoveToEasedDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamResetDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamShakeDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamSmoothResetDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamTargetDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.CamTargetOldDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.camera.OculusSyncDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.clan.ClanChannelDeltaDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.clan.ClanChannelFullDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.clan.ClanSettingsDeltaDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.clan.ClanSettingsFullDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.clan.MessageClanChannelDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.clan.MessageClanChannelSystemDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.clan.VarClanDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.clan.VarClanDisableDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.clan.VarClanEnableDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.friendchat.MessageFriendChannelDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.friendchat.UpdateFriendChatChannelFullV1Decoder
import net.rsprox.protocol.game.outgoing.decoder.codec.friendchat.UpdateFriendChatChannelFullV2Decoder
import net.rsprox.protocol.game.outgoing.decoder.codec.friendchat.UpdateFriendChatChannelSingleUserDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfClearInvDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfCloseSubDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfInitialStateDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfMoveSubDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfOpenSubDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfOpenTopDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetAngleDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetAnimDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetColourDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetEventsDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetHideDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetModelDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetNpcHeadActiveDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetNpcHeadDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetObjectDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetPlayerHeadDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelBaseColourDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelBodyTypeDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelObjDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelSelfDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetPositionDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetRotateSpeedDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetScrollPosDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.interfaces.IfSetTextDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.inv.UpdateInvFullDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.inv.UpdateInvPartialDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.inv.UpdateInvStopTransmitDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.logout.LogoutDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.logout.LogoutTransferDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.logout.LogoutWithReasonDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.map.StaticRebuildDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.HeatmapToggleDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.HideLocOpsDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.HideNpcOpsDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.HidePlayerOpsDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.HintArrowDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.HiscoreReplyDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.MinimapToggleDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.ResetAnimsDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.SendPingDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.ServerTickEndDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.UpdateRebootTimerDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.UpdateSiteSettingsDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.UpdateUid192Decoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.client.UrlOpenDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.ChatFilterSettingsDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.ChatFilterSettingsPrivateChatDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.MessageGameDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.RunClientScriptDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.SetMapFlagDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.SetPlayerOpDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.TriggerOnDialogAbortDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.UpdateRunEnergyDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.UpdateRunWeightDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.UpdateStatDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.UpdateStatOldDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.UpdateStockMarketSlotDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.misc.player.UpdateTradingPostDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.social.FriendListLoadedDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.social.MessagePrivateDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.social.MessagePrivateEchoDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.social.UpdateFriendListDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.social.UpdateIgnoreListDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.sound.MidiJingleDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.sound.MidiSongDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.sound.MidiSongOldDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.sound.MidiSongStopDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.sound.MidiSongWithSecondaryDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.sound.MidiSwapDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.sound.SynthSoundDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.varp.VarpLargeDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.varp.VarpResetDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.varp.VarpSmallDecoder
import net.rsprox.protocol.game.outgoing.decoder.codec.varp.VarpSyncDecoder

public object ServerMessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(huffmanCodec: HuffmanCodec): MessageDecoderRepository<GameServerProt> {
        val protRepository = ProtRepository.of<GameServerProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(CamLookAtEasedAngleAbsoluteDecoder())
                bind(CamLookAtEasedAngleRelativeDecoder())
                bind(CamLookAtEasedCoordDecoder())
                bind(CamLookAtDecoder())
                bind(CamModeDecoder())
                bind(CamMoveToEasedCircularDecoder())
                bind(CamMoveToEasedDecoder())
                bind(CamMoveToDecoder())
                bind(CamResetDecoder())
                bind(CamShakeDecoder())
                bind(CamSmoothResetDecoder())
                bind(CamTargetDecoder())
                bind(CamTargetOldDecoder())
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
                bind(UpdateFriendChatChannelFullV1Decoder())
                bind(UpdateFriendChatChannelFullV2Decoder())
                bind(UpdateFriendChatChannelSingleUserDecoder())

                bind(IfClearInvDecoder())
                bind(IfCloseSubDecoder())
                bind(IfInitialStateDecoder())
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

                bind(StaticRebuildDecoder())

                bind(HeatmapToggleDecoder())
                bind(HideLocOpsDecoder())
                bind(HideNpcOpsDecoder())
                bind(HidePlayerOpsDecoder())
                bind(HintArrowDecoder())
                bind(HiscoreReplyDecoder())
                bind(MinimapToggleDecoder())
                bind(ResetAnimsDecoder())
                bind(SendPingDecoder())
                bind(ServerTickEndDecoder())
                bind(UpdateRebootTimerDecoder())
                bind(UpdateSiteSettingsDecoder())
                bind(UpdateUid192Decoder())
                bind(UrlOpenDecoder())

                bind(ChatFilterSettingsDecoder())
                bind(ChatFilterSettingsPrivateChatDecoder())
                bind(MessageGameDecoder())
                bind(RunClientScriptDecoder())
                bind(SetMapFlagDecoder())
                bind(SetPlayerOpDecoder())
                bind(TriggerOnDialogAbortDecoder())
                bind(UpdateRunEnergyDecoder())
                bind(UpdateRunWeightDecoder())
                bind(UpdateStatDecoder())
                bind(UpdateStatOldDecoder())
                bind(UpdateStockMarketSlotDecoder())
                bind(UpdateTradingPostDecoder())

                bind(FriendListLoadedDecoder())
                bind(MessagePrivateEchoDecoder(huffmanCodec))
                bind(MessagePrivateDecoder(huffmanCodec))
                bind(UpdateFriendListDecoder())
                bind(UpdateIgnoreListDecoder())

                bind(MidiJingleDecoder())
                bind(MidiSongDecoder())
                bind(MidiSongOldDecoder())
                bind(MidiSongStopDecoder())
                bind(MidiSongWithSecondaryDecoder())
                bind(MidiSwapDecoder())
                bind(SynthSoundDecoder())

                bind(VarpLargeDecoder())
                bind(VarpResetDecoder())
                bind(VarpSmallDecoder())
                bind(VarpSyncDecoder())
            }
        return builder.build()
    }
}
