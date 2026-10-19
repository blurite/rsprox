package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.LocAddChangeDecoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.LocAnimDecoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.LocCustomiseDecoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.LocDelDecoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.LocPrefetchDecoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.MapAnimV1Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.MapAnimV2Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.MapProjAnimDecoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqDecoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqV2Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.MapProjAnimV2Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.ObjAddV2Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.ObjAddV3Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.ObjCountV2Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.ObjCountV3Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.ObjDelDecoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.ObjRevealDecoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.ObjRevealV3Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.SoundAreaV1Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload.SoundAreaV2Decoder
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateZonePartialEnclosedDecoder : ProxyMessageDecoder<UpdateZonePartialEnclosed> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialEnclosed {
        val zoneX = buffer.g1Alt2().toByte().toInt()
        val level = buffer.g1Alt2()
        val zoneZ = buffer.g1().toByte().toInt()
        val packets =
            buildList {
                while (buffer.isReadable) {
                    val selector = buffer.g1()
                    val decoder =
                        requireNotNull(decoders[selector]) {
                            "Unverified or invalid revision 951 zone selector $selector at byte ${buffer.buffer.readerIndex() - 1}"
                        }
                    add(decoder.decode(buffer, session) as IncomingServerGameMessage)
                }
            }
        return UpdateZonePartialEnclosed(level, zoneX, zoneZ, packets)
    }

    private companion object {
        // Selectors come from the revision 951 native dispatcher.
        val decoders: Map<Int, ProxyMessageDecoder<*>> =
            mapOf(
                0 to LocAddChangeDecoder(),
                1 to MapAnimV2Decoder(),
                2 to ObjDelDecoder(),
                3 to ObjRevealDecoder(),
                4 to ObjRevealV3Decoder(),
                5 to MapAnimV1Decoder(),
                6 to MapProjAnimDecoder(),
                7 to SoundAreaV1Decoder(),
                8 to SoundAreaV2Decoder(),
                9 to ObjAddV3Decoder(),
                10 to LocAnimDecoder(),
                11 to ObjCountV2Decoder(),
                12 to ObjCountV3Decoder(),
                13 to ObjAddV2Decoder(),
                14 to LocPrefetchDecoder(),
                15 to MapProjAnimV2Decoder(),
                16 to LocDelDecoder(),
                17 to MapProjAnimHalfsqDecoder(),
                18 to LocCustomiseDecoder(),
                19 to MapProjAnimHalfsqV2Decoder(),
                20 to LocPrefetchDecoder(),
            )
    }
}
