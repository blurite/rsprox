package net.rsprox.protocol.rs3v950.game.incoming.decoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ProtRepository
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.account.AddNewEmailAddressDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.account.ChangeEmailAddressDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.account.CreateLogProgressDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.account.SendEmailValidationCodeDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons.IfButtonDDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons.IfButtonTDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons.If3ButtonDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons.IfCrmButtonDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons.IfCrmViewOpDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons.IfTextChangeDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons.IfValueChange32Decoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat.ChatSetModeDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat.MessagePrivateDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat.MessagePublicDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat.MessageQuickchatPrivateDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat.MessageQuickchatPublicDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat.SetChatFilterSettingsDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog.AbortPDialogDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog.ResumePauseButtonDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog.ResumePClanForumQfcDialogDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog.ResumePCountDialogDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog.ResumePHslDialogDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog.ResumePCountDialogLongDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog.ResumePNameDialogDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog.ResumePObjDialogDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog.ResumePStringDialogDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.ClientPreferencesDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.Cutscene2DFinishedDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.EventAppletFocusDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.EventCameraPositionDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.EventKeyboardDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.EventMouseClickDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.EventMouseMoveDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.EventNativeMouseClickDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.EventNativeMouseMoveDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.PingStatisticsDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.SendPingReplyDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.SoundSongEndDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.TransmitVarVerifyIdDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events.WindowStatusDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.locs.OpLocDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.locs.OpLocTDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.client.MapBuildCompleteDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.client.MapBuildCompleteV2Decoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.client.NoTimeoutDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.client.StoreServerPermVarcsDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.client.UnnamedLobbyRequestDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.ApCoordTDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.BugReportDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.ClickWorldMapDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.ClientCheatDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.CloseModalDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.FaceSquareDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.LocSelectSubmitDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.MoveGameClickDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.MoveMinimapClickDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.MoveScriptedDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.SendSnapshotDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.UrlRequestDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user.WorldListFetchDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.npcs.OpNpcDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.npcs.OpNpcTDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.objs.OpObjDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.objs.OpObjTDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.players.OpPlayerDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.players.OpPlayerTDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.AffinedClanSettingsAddBannedFromChannelDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.AffinedClanSettingsSetMutedFromChannelDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.ClanChannelKickUserDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.FriendChatJoinLeaveDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.FriendChatKickDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.FriendListAddDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.FriendListDelDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.FriendSetNotesDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.FriendChatSetRankDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.IgnoreListAddDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.IgnoreListDelDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social.IgnoreSetNotesDecoder

internal object ClientMessageDecoderRepository {
    @ExperimentalStdlibApi
    fun build(huffmanCodec: HuffmanCodec): MessageDecoderRepository<GameClientProt> {
        val protRepository = ProtRepository.of<GameClientProt>()
        val builder = MessageDecoderRepositoryBuilder(protRepository).apply {
            bind(If3ButtonDecoder(GameClientProt.IF_BUTTON1_V2, 1))
            bind(If3ButtonDecoder(GameClientProt.IF_BUTTON2_V2, 2))
            bind(If3ButtonDecoder(GameClientProt.IF_BUTTON3_V2, 3))
            bind(If3ButtonDecoder(GameClientProt.IF_BUTTON4_V2, 4))
            bind(If3ButtonDecoder(GameClientProt.IF_BUTTON5_V2, 5))
            bind(If3ButtonDecoder(GameClientProt.IF_BUTTON6_V2, 6))
            bind(If3ButtonDecoder(GameClientProt.IF_BUTTON7_V2, 7))
            bind(If3ButtonDecoder(GameClientProt.IF_BUTTON8_V2, 8))
            bind(If3ButtonDecoder(GameClientProt.IF_BUTTON9_V2, 9))
            bind(If3ButtonDecoder(GameClientProt.IF_BUTTON10_V2, 10))
            for (decoder in OpNpcDecoder.all()) bind(decoder)
            for (decoder in OpLocDecoder.all()) bind(decoder)
            for (decoder in OpObjDecoder.all()) bind(decoder)
            for (decoder in OpPlayerDecoder.all()) bind(decoder)
            bind(EventAppletFocusDecoder(GameClientProt.EVENT_APPLET_FOCUS))
            bind(EventNativeMouseClickDecoder(GameClientProt.EVENT_NATIVE_MOUSE_CLICK))
            bind(MoveGameClickDecoder(GameClientProt.MOVE_GAMECLICK))
            bind(MoveMinimapClickDecoder(GameClientProt.MOVE_MINIMAPCLICK))
            bind(MoveScriptedDecoder(GameClientProt.MOVE_SCRIPTED))
            bind(OpNpcTDecoder(GameClientProt.OPNPCT_V2))
            bind(OpPlayerTDecoder(GameClientProt.OPPLAYERT_V2))
            bind(OpLocTDecoder(GameClientProt.OPLOCT_V2))
            bind(OpObjTDecoder(GameClientProt.OPOBJT_V2))
            bind(ApCoordTDecoder(GameClientProt.APCOORDT_V2))
            bind(IfButtonTDecoder(GameClientProt.IF_BUTTONT_V2))
            bind(IfButtonDDecoder(GameClientProt.IF_BUTTOND_V2))
            bind(EventCameraPositionDecoder(GameClientProt.EVENT_CAMERA_POSITION))
            bind(WindowStatusDecoder(GameClientProt.WINDOW_STATUS))
            bind(ResumePauseButtonDecoder(GameClientProt.RESUME_PAUSEBUTTON))
            bind(ResumePCountDialogDecoder(GameClientProt.RESUME_P_COUNTDIALOG))
            bind(ResumePCountDialogLongDecoder(GameClientProt.RESUME_P_COUNTDIALOG_LONG))
            bind(ResumePHslDialogDecoder(GameClientProt.RESUME_P_HSLDIALOG))
            bind(ResumePObjDialogDecoder(GameClientProt.RESUME_P_OBJDIALOG))
            bind(ResumePStringDialogDecoder(GameClientProt.RESUME_P_STRINGDIALOG))
            bind(ResumePNameDialogDecoder(GameClientProt.RESUME_P_NAMEDIALOG))
            bind(ResumePClanForumQfcDialogDecoder(GameClientProt.RESUME_P_CLANFORUMQFCDIALOG))
            bind(MessagePublicDecoder(GameClientProt.MESSAGE_PUBLIC, huffmanCodec))
            bind(MessagePrivateDecoder(GameClientProt.MESSAGE_PRIVATE, huffmanCodec))
            bind(ClientCheatDecoder(GameClientProt.CLIENT_CHEAT))
            bind(ChatSetModeDecoder(GameClientProt.CHAT_SETMODE))
            bind(SetChatFilterSettingsDecoder(GameClientProt.SET_CHATFILTERSETTINGS))
            bind(MapBuildCompleteDecoder(GameClientProt.MAP_BUILD_COMPLETE))
            bind(CloseModalDecoder(GameClientProt.CLOSE_MODAL))
            bind(AbortPDialogDecoder(GameClientProt.ABORT_P_DIALOG))
            bind(NoTimeoutDecoder(GameClientProt.NO_TIMEOUT))
            bind(MapBuildCompleteV2Decoder(GameClientProt.MAP_BUILD_COMPLETE_V2))
            bind(IfCrmButtonDecoder(GameClientProt.IF_CRM_BUTTON))
            bind(IgnoreListDelDecoder(GameClientProt.IGNORELIST_DEL))
            bind(FriendListAddDecoder(GameClientProt.FRIENDLIST_ADD))
            bind(ClanChannelKickUserDecoder(GameClientProt.CLANCHANNEL_KICKUSER))
            bind(ClickWorldMapDecoder(GameClientProt.CLICKWORLDMAP))
            bind(BugReportDecoder(GameClientProt.BUG_REPORT))
            bind(IfCrmViewOpDecoder(GameClientProt.IF_CRMVIEW_OP))
            bind(Cutscene2DFinishedDecoder(GameClientProt.CUTSCENE2D_FINISHED))
            bind(TransmitVarVerifyIdDecoder(GameClientProt.TRANSMITVAR_VERIFYID))
            bind(PingStatisticsDecoder(GameClientProt.PING_STATISTICS))
            bind(SendEmailValidationCodeDecoder(GameClientProt.SEND_EMAIL_VALIDATION_CODE))
            bind(FriendListDelDecoder(GameClientProt.FRIENDLIST_DEL))
            bind(FriendChatKickDecoder(GameClientProt.FRIENDCHAT_KICK))
            bind(AffinedClanSettingsSetMutedFromChannelDecoder(GameClientProt.AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL))
            bind(FaceSquareDecoder(GameClientProt.FACE_SQUARE))
            bind(UrlRequestDecoder(GameClientProt.URL_REQUEST))
            bind(EventMouseClickDecoder(GameClientProt.EVENT_MOUSE_CLICK))
            bind(FriendChatJoinLeaveDecoder(GameClientProt.FRIENDCHAT_JOIN_LEAVE))
            bind(CreateLogProgressDecoder(GameClientProt.CREATE_LOG_PROGRESS))
            bind(IgnoreSetNotesDecoder(GameClientProt.IGNORE_SETNOTES))
            bind(
                AffinedClanSettingsAddBannedFromChannelDecoder(
                    GameClientProt.AFFINEDCLANSETTINGS_ADDBANNED_FROMCHANNEL,
                ),
            )
            bind(FriendSetNotesDecoder(GameClientProt.FRIEND_SETNOTES))
            bind(SendPingReplyDecoder(GameClientProt.SEND_PING_REPLY))
            bind(FriendChatSetRankDecoder(GameClientProt.FRIENDCHAT_SETRANK))
            bind(WorldListFetchDecoder(GameClientProt.WORLDLIST_FETCH))
            bind(SoundSongEndDecoder(GameClientProt.SOUND_SONGEND))
            bind(IgnoreListAddDecoder(GameClientProt.IGNORELIST_ADD))
            bind(IfTextChangeDecoder(GameClientProt.IF_TEXT_CHANGE))
            bind(IfValueChange32Decoder(GameClientProt.IF_VALUE_CHANGE_32))
            bind(LocSelectSubmitDecoder(GameClientProt.LOCSELECT_SUBMIT))
            bind(EventKeyboardDecoder(GameClientProt.EVENT_KEYBOARD))
            bind(EventMouseMoveDecoder(GameClientProt.EVENT_MOUSE_MOVE))
            bind(EventNativeMouseMoveDecoder(GameClientProt.EVENT_NATIVE_MOUSE_MOVE))
            bind(SendSnapshotDecoder(GameClientProt.SEND_SNAPSHOT))
            bind(AddNewEmailAddressDecoder(GameClientProt.ADD_NEW_EMAIL_ADDRESS))
            bind(ChangeEmailAddressDecoder(GameClientProt.CHANGE_EMAIL_ADDRESS))
            bind(UnnamedLobbyRequestDecoder(GameClientProt.UNNAMED_LOBBY_REQUEST))
            bind(MessageQuickchatPublicDecoder(GameClientProt.MESSAGE_QUICKCHAT_PUBLIC))
            bind(MessageQuickchatPrivateDecoder(GameClientProt.MESSAGE_QUICKCHAT_PRIVATE))
            bind(StoreServerPermVarcsDecoder(GameClientProt.STORE_SERVERPERM_VARCS))
            bind(ClientPreferencesDecoder(GameClientProt.CLIENT_PREFERENCES))
        }
        return builder.build()
    }
}
