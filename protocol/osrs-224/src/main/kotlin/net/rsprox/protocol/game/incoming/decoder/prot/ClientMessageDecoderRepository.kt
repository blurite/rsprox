package net.rsprox.protocol.game.incoming.decoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ProtRepository
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
// import net.rsprox.protocol.game.incoming.decoder.codec.misc.client.ReflectionCheckReplyDecoder

public object ClientMessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(huffmanCodec: HuffmanCodec): MessageDecoderRepository<GameClientProt> {
        val protRepository = ProtRepository.of<GameClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                // bind(If1ButtonDecoder())
                // bind(If3ButtonDecoder(GameClientProt.IF_BUTTON1, 1))
                // bind(If3ButtonDecoder(GameClientProt.IF_BUTTON2, 2))
                // bind(If3ButtonDecoder(GameClientProt.IF_BUTTON3, 3))
                // bind(If3ButtonDecoder(GameClientProt.IF_BUTTON4, 4))
                // bind(If3ButtonDecoder(GameClientProt.IF_BUTTON5, 5))
                // bind(If3ButtonDecoder(GameClientProt.IF_BUTTON6, 6))
                // bind(If3ButtonDecoder(GameClientProt.IF_BUTTON7, 7))
                // bind(If3ButtonDecoder(GameClientProt.IF_BUTTON8, 8))
                // bind(If3ButtonDecoder(GameClientProt.IF_BUTTON9, 9))
                // bind(If3ButtonDecoder(GameClientProt.IF_BUTTON10, 10))
                // bind(IfButtonDDecoder())
                // bind(IfButtonTDecoder())

                // bind(OpNpc1Decoder())
                // bind(OpNpc2Decoder())
                // bind(OpNpc3Decoder())
                // bind(OpNpc4Decoder())
                // bind(OpNpc5Decoder())
                // bind(OpNpc6Decoder())
                // bind(OpNpcTDecoder())

                // bind(OpLoc1Decoder())
                // bind(OpLoc2Decoder())
                // bind(OpLoc3Decoder())
                // bind(OpLoc4Decoder())
                // bind(OpLoc5Decoder())
                // bind(OpLoc6Decoder())
                // bind(OpLocTDecoder())

                // bind(OpObj1Decoder())
                // bind(OpObj2Decoder())
                // bind(OpObj3Decoder())
                // bind(OpObj4Decoder())
                // bind(OpObj5Decoder())
                // bind(OpObj6Decoder())
                // bind(OpObjTDecoder())

                // bind(OpPlayer1Decoder())
                // bind(OpPlayer2Decoder())
                // bind(OpPlayer3Decoder())
                // bind(OpPlayer4Decoder())
                // bind(OpPlayer5Decoder())
                // bind(OpPlayer6Decoder())
                // bind(OpPlayer7Decoder())
                // bind(OpPlayer8Decoder())
                // bind(OpPlayerTDecoder())

                // bind(EventAppletFocusDecoder())
                // bind(EventCameraPositionDecoder())
                // bind(EventKeyboardDecoder())
                // bind(EventMouseScrollDecoder())
                // bind(EventMouseMoveDecoder())
                // bind(EventNativeMouseMoveDecoder())
                // bind(EventMouseClickDecoder())
                // bind(EventNativeMouseClickDecoder())

                // bind(ResumePauseButtonDecoder())
                // bind(ResumePNameDialogDecoder())
                // bind(ResumePStringDialogDecoder())
                // bind(ResumePCountDialogDecoder())
                // bind(ResumePObjDialogDecoder())

                // bind(FriendChatKickDecoder())
                // bind(FriendChatSetRankDecoder())
                // bind(FriendChatJoinLeaveDecoder())

                // bind(ClanChannelFullRequestDecoder())
                // bind(ClanSettingsFullRequestDecoder())
                // bind(ClanChannelKickUserDecoder())
                // bind(AffinedClanSettingsAddBannedFromChannelDecoder())
                // bind(AffinedClanSettingsSetMutedFromChannelDecoder())

                // bind(FriendListAddDecoder())
                // bind(FriendListDelDecoder())
                // bind(IgnoreListAddDecoder())
                // bind(IgnoreListDelDecoder())

                // bind(MessagePublicDecoder(huffmanCodec))
                // bind(MessagePrivateDecoder(huffmanCodec))

                // bind(MoveGameClickDecoder())
                // bind(MoveMinimapClickDecoder())
                // bind(ClientCheatDecoder())
                // bind(SetChatFilterSettingsDecoder())
                // bind(ClickWorldMapDecoder())
                // bind(OculusLeaveDecoder())
                // bind(CloseModalDecoder())
                // bind(TeleportDecoder())
                // bind(BugReportDecoder())
                // bind(SendSnapshotDecoder())
                // bind(HiscoreRequestDecoder())
                // bind(IfCrmViewClickDecoder())
                // bind(UpdatePlayerModelDecoder())

                // bind(ConnectionTelemetryDecoder())
                // bind(SendPingReplyDecoder())
                // bind(DetectModifiedClientDecoder())
                // bind(ReflectionCheckReplyDecoder())
                // bind(NoTimeoutDecoder())
                // bind(IdleDecoder())
                // bind(MapBuildCompleteDecoder())
                // bind(MembershipPromotionEligibilityDecoder())
                // bind(SoundJingleEndDecoder())
                // bind(WindowStatusDecoder())
            }
        return builder.build()
    }
}
