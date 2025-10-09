package net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.LocAddChangeV2Decoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.LocAnimDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.LocDelDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.LocMergeDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.MapAnimDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.MapProjAnimV2Decoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.ObjAddDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.ObjCountDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.ObjCustomiseDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.ObjDelDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.ObjEnabledOpsDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.ObjUncustomiseDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.codec.zone.payload.SoundAreaDecoder
import net.rsprox.protocol.v234.game.outgoing.decoder.prot.GameServerProt

internal class UpdateZonePartialEnclosedDecoder : ProxyMessageDecoder<UpdateZonePartialEnclosed> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialEnclosed {
        val zoneX = buffer.g1Alt3()
        val zoneZ = buffer.g1Alt2()
        val level = buffer.g1()
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
        LOC_MERGE(LocMergeDecoder()),
        OBJ_ADD(ObjAddDecoder()),
        MAP_PROJANIM_V2(MapProjAnimV2Decoder()),
        MAP_ANIM(MapAnimDecoder()),
        LOC_ANIM(LocAnimDecoder()),
        OBJ_UNCUSTOMISE(ObjUncustomiseDecoder()),
        OBJ_ENABLED_OPS(ObjEnabledOpsDecoder()),
        SOUND_AREA(SoundAreaDecoder()),
        OBJ_DEL(ObjDelDecoder()),
        LOC_DEL(LocDelDecoder()),
        OBJ_COUNT(ObjCountDecoder()),
        LOC_ADD_CHANGE_V2(LocAddChangeV2Decoder()),
        OBJ_CUSTOMISE(ObjCustomiseDecoder()),
    }
}
