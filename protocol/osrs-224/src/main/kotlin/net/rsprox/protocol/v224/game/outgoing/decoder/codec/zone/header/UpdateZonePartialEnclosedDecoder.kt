package net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.LocAddChangeDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.LocAnimDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.LocDelDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.LocMergeDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.MapAnimDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.MapProjAnimDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.ObjAddDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.ObjCountDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.ObjDelDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.ObjEnabledOpsDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload.SoundAreaDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

public class UpdateZonePartialEnclosedDecoder : ProxyMessageDecoder<UpdateZonePartialEnclosed> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialEnclosed {
        val zoneZ = buffer.g1()
        val level = buffer.g1Alt3()
        val zoneX = buffer.g1Alt3()
        val packets =
            buildList {
                while (buffer.isReadable) {
                    val index = buffer.g1()
                    val decoder = IndexedZoneProtDecoder.entries[index]
                    add(decoder.decoder.decode(buffer, session) as IncomingZoneProt)
                }
            }
        return UpdateZonePartialEnclosed(
            zoneX,
            zoneZ,
            level,
            packets,
        )
    }

    private enum class IndexedZoneProtDecoder(
        val decoder: ProxyMessageDecoder<*>,
    ) {
        LOC_ADD_CHANGE(LocAddChangeDecoder()),
        OBJ_COUNT(ObjCountDecoder()),
        OBJ_ENABLED_OPS(ObjEnabledOpsDecoder()),
        MAP_ANIM(MapAnimDecoder()),
        OBJ_DEL(ObjDelDecoder()),
        OBJ_ADD(ObjAddDecoder()),
        LOC_MERGE(LocMergeDecoder()),
        LOC_ANIM(LocAnimDecoder()),
        MAP_PROJANIM(MapProjAnimDecoder()),
        LOC_DEL(LocDelDecoder()),
        SOUND_AREA(SoundAreaDecoder()),
    }
}
