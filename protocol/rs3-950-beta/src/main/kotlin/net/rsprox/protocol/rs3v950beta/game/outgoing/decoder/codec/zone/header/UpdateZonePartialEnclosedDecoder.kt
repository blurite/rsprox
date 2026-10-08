package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.LocAddChangeDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.LocAnimDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.LocCustomiseDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.LocDelDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.LocPrefetchDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapAnimV1Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapAnimV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.MapProjAnimV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjAddV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjAddV3Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjCountV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjCountV3Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjDelDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjRevealDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.ObjRevealV3Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.SoundAreaV1Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload.SoundAreaV2Decoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateZonePartialEnclosedDecoder : ProxyMessageDecoder<UpdateZonePartialEnclosed> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialEnclosed {
        val zoneX = buffer.g1Alt2().toByte().toInt()
        val zoneZ = buffer.g1Alt1().toByte().toInt()
        val level = buffer.g1Alt1()
        val packets =
            buildList {
                while (buffer.isReadable) {
                    val selector = buffer.g1()
                    val decoder =
                        requireNotNull(decoders[selector]) {
                            "Unverified or invalid revision 950-beta zone selector $selector at byte ${buffer.buffer.readerIndex() - 1}"
                        }
                    add(decoder.decode(buffer, session) as IncomingServerGameMessage)
                }
            }
        return UpdateZonePartialEnclosed(level, zoneX, zoneZ, packets)
    }

    private companion object {
        // Add only beta-verified routes; never fall back to live-950 selector ordering.
        val decoders: Map<Int, ProxyMessageDecoder<*>> =
            mapOf(
                0 to LocAnimDecoder(),
                1 to MapProjAnimV2Decoder(),
                2 to MapProjAnimDecoder(),
                3 to LocPrefetchDecoder(),
                4 to ObjDelDecoder(),
                5 to ObjAddV3Decoder(),
                6 to MapProjAnimHalfsqV2Decoder(),
                7 to MapAnimV1Decoder(),
                8 to SoundAreaV1Decoder(),
                9 to SoundAreaV2Decoder(),
                10 to LocCustomiseDecoder(),
                11 to ObjCountV2Decoder(),
                12 to ObjCountV3Decoder(),
                13 to LocDelDecoder(),
                14 to ObjRevealDecoder(),
                15 to LocPrefetchDecoder(),
                16 to ObjRevealV3Decoder(),
                17 to LocAddChangeDecoder(),
                18 to MapProjAnimHalfsqDecoder(),
                19 to MapAnimV2Decoder(),
                20 to ObjAddV2Decoder(),
            )
    }
}
