package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload.*
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateZonePartialEnclosedDecoder : ProxyMessageDecoder<UpdateZonePartialEnclosed> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialEnclosed {
        val level = buffer.g1Alt3()
        val zoneX = buffer.g1sAlt2()
        val zoneZ = buffer.g1s()
        val packets =
            buildList {
                while (buffer.isReadable) {
                    val index = buffer.g1()
                    val decoder = IndexedZoneProtDecoder.byIndexOrNull(index) ?: break
                    add(decoder.decoder.decode(buffer, session) as IncomingServerGameMessage)
                }
            }
        return UpdateZonePartialEnclosed(level, zoneX, zoneZ, packets)
    }

    private enum class IndexedZoneProtDecoder(
        val index: Int,
        val decoder: ProxyMessageDecoder<*>,
    ) {
        OBJ_ADD_V2(0, ObjAddDecoder()),
        UNKNOWN(1, MapAnimDecoder()), // todo: zone only FUN_001f24f0
        MAP_ANIM_V2(2, MapAnimV2Decoder()),
        LOC_PREFETCH(3, LocPrefetchDecoder()),
        SOUND_AREA(4, SoundAreaDecoder()),
        MAP_PROJANIM_HALFSQ(5, MapProjAnimHalfsqDecoder()),
        OBJ_COUNT_V2(6, ObjCountDecoder()),
        LOC_CUSTOMISE(7, LocCustomiseDecoder()),
        LOC_ADD_CHANGE(8, LocAddChangeDecoder()),
        OBJ_REVEAL_V2(9, ObjRevealDecoder()), // zone only
        OBJ_DEL_V2(10, ObjDelDecoder()),
        MAP_ANIM(11, MapAnimDecoder()),
        LOC_DEL(12, LocDelDecoder()),
        LOC_ANIM(13, LocAnimDecoder()),
        UNUSED_LOC_PREFETCH(14, LocPrefetchDecoder()), // 2nd instance should be unused
        MAP_PROJANIM(15, MapProjAnimDecoder()),
        MAP_PROJANIM_V2(16, MapProjAnimV2Decoder()),
        MAP_PROJANIM_HALFSQ_V2(17, MapProjAnimHalfsqV2Decoder()),
        ;

        companion object {
            private val VALUES = entries.toTypedArray()
            fun byIndexOrNull(index: Int): IndexedZoneProtDecoder? = VALUES.getOrNull(index)
        }
    }
}
