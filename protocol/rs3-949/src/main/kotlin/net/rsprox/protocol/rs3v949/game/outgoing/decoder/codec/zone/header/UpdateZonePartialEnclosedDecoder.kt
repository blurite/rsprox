package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.LocAddChangeDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.LocAnimDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.LocCustomiseDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.LocDelDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.LocPrefetchDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapAnimDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapAnimV2Decoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqV2Decoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimV2Decoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MidiSongLocationDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjAddDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjCountDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjDelDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjRevealDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.SoundAreaDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.TextCoordDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.session.Session

internal class UpdateZonePartialEnclosedDecoder : ProxyMessageDecoder<UpdateZonePartialEnclosed> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialEnclosed {
        val level = buffer.g1Alt2()
        val zoneX = buffer.g1sAlt3()
        val zoneZ = buffer.g1sAlt3()
        val packets =
            buildList {
                while (buffer.isReadable) {
                    val index = buffer.g1()
                    val decoder = IndexedZoneProtDecoder.entries[index]
                    add(decoder.decoder.decode(buffer, session) as IncomingServerGameMessage)
                }
            }
        return UpdateZonePartialEnclosed(level, zoneX, zoneZ, packets)
    }

    private enum class IndexedZoneProtDecoder(
        val decoder: ProxyMessageDecoder<*>,
    ) {
        SOUND_AREA(SoundAreaDecoder()), // 0
        LOC_ADD_CHANGE(LocAddChangeDecoder()), // 1
        LOC_CUSTOMISE(LocCustomiseDecoder()), // 2 - guess - could be text_coord
        LOC_ANIM(LocAnimDecoder()), // 3
        MAP_PROJANIM(MapProjAnimDecoder()), // 4
        MAP_PROJANIM_HALFSQ(MapProjAnimHalfsqDecoder()), // 5
        MIDI_SONG_LOCATION(MidiSongLocationDecoder()), // 6 - guess, 11 bytes
        OBJ_DEL(ObjDelDecoder(GameServerProt.OBJ_DEL, big = false)), // 7
        LOC_DEL(LocDelDecoder()), // 8
        OBJ_ADD(ObjAddDecoder(GameServerProt.OBJ_ADD, big = false)), // 9
        MAP_ANIM_V2(MapAnimV2Decoder()), // 10
        OBJ_REVEAL_V2(ObjRevealDecoder(GameServerProt.OBJ_REVEAL_V2, big = true)), // 11
        OBJ_COUNT(ObjCountDecoder(GameServerProt.OBJ_COUNT, big = false)), // 12
        OBJ_ADD_V2(ObjAddDecoder(GameServerProt.OBJ_ADD_V2, big = true)), // 13
        OBJ_REVEAL(ObjRevealDecoder(GameServerProt.OBJ_REVEAL, big = false)), // 14
        MAP_ANIM(MapAnimDecoder()), // 15
        MAP_PROJANIM_HALFSQ_V2(MapProjAnimHalfsqV2Decoder()), // 16
        OBJ_DEL_V2(ObjDelDecoder(GameServerProt.OBJ_DEL_V2, big = true)), // 17
        LOC_PREFETCH(LocPrefetchDecoder()), // 18 - guess
        MAP_PROJANIM_V2(MapProjAnimV2Decoder()), // 19
        OBJ_COUNT_V2(ObjCountDecoder(GameServerProt.OBJ_COUNT_V2, big = true)), // 20
        TEXT_COORD(TextCoordDecoder()), // 21 - guess - could be loc_customise
    }
}
