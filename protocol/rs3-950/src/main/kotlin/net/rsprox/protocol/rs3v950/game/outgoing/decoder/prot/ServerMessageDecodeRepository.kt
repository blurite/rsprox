package net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot

import net.rsprot.protocol.ProtRepository
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.camera.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo.NpcInfoClient
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.PlayerInfoClient
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.inv.UpdateInvFullDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.inv.UpdateInvPartialDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.inv.UpdateInvStopTransmitDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map.RebuildNormalDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound.VorbisSoundDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific.ProjAnimSpecificV2Decoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varbit.VarbitLargeDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varbit.VarbitSmallDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varc.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varp.VarpLargeDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varp.VarpLongDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varp.VarpSmallDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.header.UpdateZoneFullFollowsDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.header.UpdateZonePartialEnclosedDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.header.UpdateZonePartialFollowsDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload.*

internal object ServerMessageDecoderRepository {
    @ExperimentalStdlibApi
    fun build(): MessageDecoderRepository<GameServerProt> {
        val protRepository = ProtRepository.of<GameServerProt>()
        val npcInfoClient = NpcInfoClient()
        val playerInfoClient = PlayerInfoClient()

        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(CameraUpdateDecoder())
                bind(CamForceAngleDecoder())
                bind(CamLookAtDecoder())
                bind(CamMoveToDecoder())
                bind(CamShakeDecoder())
                bind(npcInfoClient)
                bind(playerInfoClient)
                bind(IfCloseSubDecoder())
                bind(IfOpenSubActiveLocDecoder())
                bind(IfOpenSubActiveObjDecoder())
                bind(IfOpenSubDecoder())
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
                bind(RebuildNormalDecoder(npcInfoClient, playerInfoClient))
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
                bind(ProjAnimSpecificV2Decoder())
                bind(VarbitLargeDecoder())
                bind(VarbitSmallDecoder())
                bind(VarcBitLargeDecoder())
                bind(VarcBitSmallDecoder())
                bind(VarcLargeDecoder())
                bind(VarcSmallDecoder())
                bind(VarcStrSmallDecoder())
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
                bind(MapAnimDecoder())
                bind(MapAnimV2Decoder())
                bind(MapProjAnimDecoder())
                bind(MapProjAnimHalfsqDecoder())
                bind(MapProjAnimHalfsqV2Decoder())
                bind(MapProjAnimV2Decoder())
                bind(MidiSongLocationDecoder())
                bind(ObjAddDecoder())
                bind(ObjCountDecoder())
                bind(ObjDelDecoder())
                bind(SoundAreaDecoder())
                bind(TextCoordDecoder())
                bind(TickEndDecoder(GameServerProt.NO_TIMEOUT))
                bind(TickEndDecoder(GameServerProt.SERVER_TICK_END))
            }
        return builder.build()
    }
}
