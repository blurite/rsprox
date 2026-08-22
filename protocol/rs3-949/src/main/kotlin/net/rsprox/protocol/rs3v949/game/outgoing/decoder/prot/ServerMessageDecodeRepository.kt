package net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot

import net.rsprot.protocol.ProtRepository
import net.rsprox.protocol.MessageDecoderRepository
import net.rsprox.protocol.MessageDecoderRepositoryBuilder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.camera.CamForceAngleDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.camera.CamLookAtDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.camera.CamMoveToDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.camera.CamShakeDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.camera.CameraUpdateDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfCloseSubDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfOpenSubActiveLocDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfOpenSubActiveObjDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfOpenSubDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfOpenTopDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetAnimDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetColourDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetEventsDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetHideDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetModelDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetNpcHeadDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetObjectDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetPlayerHeadDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetPlayerHeadSnapshotDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelSelfDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetPlayerModelSnapshotDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetPositionDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetScrollPosDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetTargetParamDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces.IfSetTextDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.inv.UpdateInvFullDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.inv.UpdateInvPartialDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.inv.UpdateInvStopTransmitDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.map.LocPrefetchDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.map.RebuildNormalDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.client.Cutscene2dPlayDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.client.HintArrowDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.client.HintTrailDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.client.MinimapToggleDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.client.TickEndDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player.ChatFilterSettingsPrivateChatDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player.JcoinsUpdateDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.LocAddChangeDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.LocDelDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapAnimDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapAnimV2Decoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqV2Decoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimV2Decoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.SoundAreaDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.TextCoordDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player.MessageGameDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player.RunClientScriptDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player.SetPlayerOpDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player.UpdateRunEnergyDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player.UpdateRunWeightDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player.UpdateStatDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.sound.MidiSongDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.sound.SoundSynthDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.specific.ProjAnimSpecificV2Decoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjAddDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjCountDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjDelDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjRevealDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varbit.VarbitLargeDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varbit.VarbitSmallDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varp.VarpLargeDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varp.VarpLongDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varp.VarpSmallDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.header.UpdateZoneFollowsDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.header.UpdateZonePartialEnclosedDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varc.VarcSmallDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varc.VarcLargeDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varc.VarcBitSmallDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varc.VarcBitLargeDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varc.VarcStrSmallDecoder

internal object ServerMessageDecoderRepository {
    @ExperimentalStdlibApi
    fun build(): MessageDecoderRepository<GameServerProt> {
        val protRepository = ProtRepository.of<GameServerProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(VarbitSmallDecoder())
                bind(VarbitLargeDecoder())
                bind(VarpLargeDecoder())
                bind(VarpSmallDecoder())
                bind(VarpLongDecoder())
                bind(IfOpenTopDecoder())
                bind(IfOpenSubDecoder())
                bind(IfCloseSubDecoder())
                bind(IfSetHideDecoder())
                bind(MessageGameDecoder())
                bind(LocAddChangeDecoder())
                bind(LocDelDecoder())
                for (decoder in UpdateZoneFollowsDecoder.all()) bind(decoder)
                for (decoder in ObjAddDecoder.all()) bind(decoder)
                for (decoder in ObjDelDecoder.all()) bind(decoder)
                for (decoder in ObjCountDecoder.all()) bind(decoder)
                for (decoder in ObjRevealDecoder.all()) bind(decoder)
                bind(MapAnimDecoder())
                bind(MapAnimV2Decoder())
                bind(SoundAreaDecoder())
                bind(TextCoordDecoder())
                bind(MapProjAnimDecoder())
                bind(MapProjAnimHalfsqDecoder())
                bind(MapProjAnimHalfsqV2Decoder())
                bind(MapProjAnimV2Decoder())
                bind(UpdateZonePartialEnclosedDecoder())
                bind(VarcSmallDecoder())
                bind(VarcLargeDecoder())
                bind(VarcBitSmallDecoder())
                bind(VarcBitLargeDecoder())
                bind(VarcStrSmallDecoder())
                bind(IfSetNpcHeadDecoder())
                bind(IfSetPlayerHeadDecoder())
                bind(IfSetTextDecoder())
                bind(IfSetObjectDecoder())
                bind(IfOpenSubActiveObjDecoder())
                bind(IfOpenSubActiveLocDecoder())
                bind(IfSetModelDecoder())
                bind(IfSetPositionDecoder())
                bind(IfSetAnimDecoder())
                bind(IfSetColourDecoder())
                bind(IfSetScrollPosDecoder())
                bind(IfSetPlayerModelSelfDecoder())
                bind(IfSetTargetParamDecoder())
                bind(IfSetEventsDecoder())
                bind(CamLookAtDecoder())
                bind(CamShakeDecoder())
                bind(CamForceAngleDecoder())
                bind(CamMoveToDecoder())
                bind(CameraUpdateDecoder())
                bind(UpdateInvFullDecoder())
                bind(UpdateInvStopTransmitDecoder())
                bind(UpdateInvPartialDecoder())
                bind(UpdateRunWeightDecoder())
                bind(UpdateStatDecoder())
                bind(UpdateRunEnergyDecoder())
                bind(MinimapToggleDecoder())
                bind(HintTrailDecoder())
                bind(HintArrowDecoder())
                bind(ChatFilterSettingsPrivateChatDecoder())
                bind(SetPlayerOpDecoder())
                bind(IfSetPlayerModelSnapshotDecoder())
                bind(IfSetPlayerHeadSnapshotDecoder())
                bind(SoundSynthDecoder())
                bind(RunClientScriptDecoder())
                bind(ProjAnimSpecificV2Decoder())
                bind(MidiSongDecoder())
                bind(LocPrefetchDecoder())
                bind(Cutscene2dPlayDecoder())
                bind(JcoinsUpdateDecoder())
                bind(RebuildNormalDecoder())
                bind(TickEndDecoder(GameServerProt.LOBBY_TICK_END))
                bind(TickEndDecoder(GameServerProt.SERVER_TICK_END))
            }
        return builder.build()
    }
}
