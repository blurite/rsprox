package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.LocAddChangeDecoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.LocAnimDecoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.LocCustomiseDecoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.LocDelDecoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.LocPrefetchDecoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.MapAnimV1Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.MapAnimV2Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimDecoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqDecoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqV2Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimV2Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.ObjAddV2Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.ObjAddV3Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.ObjCountV2Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.ObjCountV3Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.ObjDelDecoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.ObjRevealDecoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.ObjRevealV3Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.SoundAreaV1Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.zone.payload.SoundAreaV2Decoder
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateZonePartialEnclosedDecoder : ProxyMessageDecoder<UpdateZonePartialEnclosed> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialEnclosed {
        val zoneZ = buffer.g1().toByte().toInt()
        val zoneX = buffer.g1Alt3().toByte().toInt()
        val level = buffer.g1()
        val packets =
            buildList {
                while (buffer.isReadable) {
                    val selector = buffer.g1()
                    val decoder =
                        requireNotNull(decoders[selector]) {
                            "Invalid revision 951 beta zone selector $selector at byte ${buffer.buffer.readerIndex() - 1}"
                        }
                    add(decoder.decode(buffer, session) as IncomingServerGameMessage)
                }
            }
        return UpdateZonePartialEnclosed(level, zoneX, zoneZ, packets)
    }

    private companion object {
        // Selectors come from the revision 951 beta native dispatcher.
        val decoders: Map<Int, ProxyMessageDecoder<*>> =
            mapOf(
                0 to ObjRevealDecoder(),
                1 to LocDelDecoder(),
                2 to MapProjAnimV2Decoder(),
                3 to SoundAreaV2Decoder(),
                4 to ObjAddV2Decoder(),
                5 to MapProjAnimDecoder(),
                6 to ObjCountV3Decoder(),
                7 to MapAnimV2Decoder(),
                8 to ObjDelDecoder(),
                9 to ObjRevealV3Decoder(),
                10 to LocAnimDecoder(),
                11 to ObjCountV2Decoder(),
                12 to ObjAddV3Decoder(),
                13 to MapAnimV1Decoder(),
                14 to LocPrefetchDecoder(),
                15 to MapProjAnimHalfsqV2Decoder(),
                16 to LocPrefetchDecoder(),
                17 to LocAddChangeDecoder(),
                18 to MapProjAnimHalfsqDecoder(),
                19 to LocCustomiseDecoder(),
                20 to SoundAreaV1Decoder(),
            )
    }
}
