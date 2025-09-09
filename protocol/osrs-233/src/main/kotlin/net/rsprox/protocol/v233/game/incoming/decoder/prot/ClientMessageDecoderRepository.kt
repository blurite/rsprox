package net.rsprox.protocol.v233.game.incoming.decoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ProtRepository
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.buttons.*
import net.rsprox.protocol.v233.game.incoming.decoder.codec.clan.AffinedClanSettingsAddBannedFromChannelDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.clan.AffinedClanSettingsSetMutedFromChannelDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.clan.ClanChannelFullRequestDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.clan.ClanChannelKickUserDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.clan.ClanSettingsFullRequestDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.events.*
import net.rsprox.protocol.v233.game.incoming.decoder.codec.friendchat.FriendChatJoinLeaveDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.friendchat.FriendChatKickDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.friendchat.FriendChatSetRankDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.locs.OpLoc1Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.locs.OpLoc2Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.locs.OpLoc3Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.locs.OpLoc4Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.locs.OpLoc5Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.locs.OpLoc6Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.locs.OpLocTDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.messaging.MessagePrivateDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.messaging.MessagePublicDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.client.*
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.client.ReflectionCheckReplyDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.BugReportDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.ClickWorldMapDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.ClientCheatDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.CloseModalDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.HiscoreRequestDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.IfCrmViewClickDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.MoveGameClickDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.MoveMinimapClickDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.OculusLeaveDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.SendSnapshotDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.SetChatFilterSettingsDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.SetHeadingDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user.TeleportDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.npcs.OpNpc1Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.npcs.OpNpc2Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.npcs.OpNpc3Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.npcs.OpNpc4Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.npcs.OpNpc5Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.npcs.OpNpc6Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.npcs.OpNpcTDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.objs.OpObj1Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.objs.OpObj2Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.objs.OpObj3Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.objs.OpObj4Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.objs.OpObj5Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.objs.OpObj6Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.objs.OpObjTDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.players.OpPlayer1Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.players.OpPlayer2Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.players.OpPlayer3Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.players.OpPlayer4Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.players.OpPlayer5Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.players.OpPlayer6Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.players.OpPlayer7Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.players.OpPlayer8Decoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.players.OpPlayerTDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.resumed.ResumePCountDialogDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.resumed.ResumePNameDialogDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.resumed.ResumePObjDialogDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.resumed.ResumePStringDialogDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.resumed.ResumePauseButtonDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.social.FriendListAddDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.social.FriendListDelDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.social.IgnoreListAddDecoder
import net.rsprox.protocol.v233.game.incoming.decoder.codec.social.IgnoreListDelDecoder

internal object ClientMessageDecoderRepository {
    @ExperimentalStdlibApi
    fun build(huffmanCodec: HuffmanCodec): MessageDecoderRepository<GameClientProt> {
        val protRepository = ProtRepository.of<GameClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(If1ButtonDecoder())
                bind(IfButtonXDecoder())
                bind(IfSubOpDecoder())
                bind(IfButtonDDecoder())
                bind(IfButtonTDecoder())
                bind(IfRunScriptDecoder())

                bind(OpNpc1Decoder())
                bind(OpNpc2Decoder())
                bind(OpNpc3Decoder())
                bind(OpNpc4Decoder())
                bind(OpNpc5Decoder())
                bind(OpNpc6Decoder())
                bind(OpNpcTDecoder())

                bind(OpLoc1Decoder())
                bind(OpLoc2Decoder())
                bind(OpLoc3Decoder())
                bind(OpLoc4Decoder())
                bind(OpLoc5Decoder())
                bind(OpLoc6Decoder())
                bind(OpLocTDecoder())

                bind(OpObj1Decoder())
                bind(OpObj2Decoder())
                bind(OpObj3Decoder())
                bind(OpObj4Decoder())
                bind(OpObj5Decoder())
                bind(OpObj6Decoder())
                bind(OpObjTDecoder())

                bind(OpPlayer1Decoder())
                bind(OpPlayer2Decoder())
                bind(OpPlayer3Decoder())
                bind(OpPlayer4Decoder())
                bind(OpPlayer5Decoder())
                bind(OpPlayer6Decoder())
                bind(OpPlayer7Decoder())
                bind(OpPlayer8Decoder())
                bind(OpPlayerTDecoder())

                bind(EventAppletFocusDecoder())
                bind(EventCameraPositionDecoder())
                bind(EventKeyboardDecoder())
                bind(EventMouseScrollDecoder())
                bind(EventMouseMoveDecoder())
                bind(EventNativeMouseMoveDecoder())
                bind(EventMouseClickV1Decoder())
                bind(EventMouseClickV2Decoder())

                bind(ResumePauseButtonDecoder())
                bind(ResumePNameDialogDecoder())
                bind(ResumePStringDialogDecoder())
                bind(ResumePCountDialogDecoder())
                bind(ResumePObjDialogDecoder())

                bind(FriendChatKickDecoder())
                bind(FriendChatSetRankDecoder())
                bind(FriendChatJoinLeaveDecoder())

                bind(ClanChannelFullRequestDecoder())
                bind(ClanSettingsFullRequestDecoder())
                bind(ClanChannelKickUserDecoder())
                bind(AffinedClanSettingsAddBannedFromChannelDecoder())
                bind(AffinedClanSettingsSetMutedFromChannelDecoder())

                bind(FriendListAddDecoder())
                bind(FriendListDelDecoder())
                bind(IgnoreListAddDecoder())
                bind(IgnoreListDelDecoder())

                bind(MessagePublicDecoder(huffmanCodec))
                bind(MessagePrivateDecoder(huffmanCodec))

                bind(MoveGameClickDecoder())
                bind(MoveMinimapClickDecoder())
                bind(ClientCheatDecoder())
                bind(SetChatFilterSettingsDecoder())
                bind(SetHeadingDecoder())
                bind(ClickWorldMapDecoder())
                bind(OculusLeaveDecoder())
                bind(CloseModalDecoder())
                bind(TeleportDecoder())
                bind(BugReportDecoder())
                bind(SendSnapshotDecoder())
                bind(HiscoreRequestDecoder())
                bind(IfCrmViewClickDecoder())

                bind(ConnectionTelemetryDecoder())
                bind(SendPingReplyDecoder())
                bind(DetectModifiedClientDecoder())
                bind(ReflectionCheckReplyDecoder())
                bind(NoTimeoutDecoder())
                bind(IdleDecoder())
                bind(MapBuildCompleteDecoder())
                bind(MembershipPromotionEligibilityDecoder())
                bind(SoundJingleEndDecoder())
                bind(WindowStatusDecoder())
                bind(RSevenStatusDecoder())
            }
        return builder.build()
    }
}
