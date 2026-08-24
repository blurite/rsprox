package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.LocAnimDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.LocDelDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapAnimDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapAnimV2Decoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.MapProjAnimHalfsqV2Decoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.SoundAreaDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.TextCoordDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjAddDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjCountDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjDelDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload.ObjRevealDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.map.UnknownZoneSubOp
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.session.Session

internal class UpdateZonePartialEnclosedDecoder : ProxyMessageDecoder<UpdateZonePartialEnclosed> {
    override val prot: ClientProt = GameServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED

    private val soundAreaDecoder = SoundAreaDecoder()
    private val textCoordDecoder = TextCoordDecoder()
    private val objRevealDecoder = ObjRevealDecoder.Companion.all()[0]
    private val mapProjAnimDecoder = MapProjAnimDecoder()
    private val mapProjAnimHalfsqDecoder = MapProjAnimHalfsqDecoder()
    private val mapAnimDecoder = MapAnimDecoder()
    private val objDelDecoders = ObjDelDecoder.Companion.all()
    private val locDelDecoder = LocDelDecoder()
    private val objAddDecoders = ObjAddDecoder.Companion.all()
    private val mapAnimV2Decoder = MapAnimV2Decoder()
    private val objCountDecoders = ObjCountDecoder.Companion.all()
    private val locAnimDecoder = LocAnimDecoder()
    private val mapProjAnimHalfsqV2Decoder = MapProjAnimHalfsqV2Decoder()

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateZonePartialEnclosed {
        val level = buffer.g1Alt2()
        val zoneX = buffer.g1Alt3()
        val zoneZ = buffer.g1Alt3()
        val packets = mutableListOf<IncomingServerGameMessage>()

        loop@ while (buffer.isReadable) {
            val subOp = buffer.g1()
            val fixedLen = subOpLength(subOp)
            val actualLen =
                if (fixedLen == VAR_BYTE_SENTINEL) {
                    if (!buffer.isReadable) break@loop
                    buffer.g1()
                } else {
                    fixedLen
                }
            if (actualLen <= 0 || buffer.readableBytes() < actualLen) break@loop

            val slice = buffer.buffer.readSlice(actualLen).toJagByteBuf()
            val message: IncomingServerGameMessage =
                when (subOp) {
                    0 -> soundAreaDecoder.decode(slice, session)
                    1 -> textCoordDecoder.decode(slice, session)
                    2 -> UnknownZoneSubOp(subOp, sliceBytes(slice))
                    3 -> objRevealDecoder.decode(slice, session)
                    4 -> mapProjAnimDecoder.decode(slice, session)
                    5 -> mapProjAnimHalfsqDecoder.decode(slice, session)
                    6, 15 -> mapAnimDecoder.decode(slice, session)
                    7 -> objDelDecoders[0].decode(slice, session)
                    8 -> locDelDecoder.decode(slice, session)
                    9 -> objAddDecoders[0].decode(slice, session)
                    10 -> mapAnimV2Decoder.decode(slice, session)
                    11 -> objCountDecoders[1].decode(slice, session)
                    12 -> locAnimDecoder.decode(slice, session)
                    13 -> objAddDecoders[1].decode(slice, session)
                    14 -> objCountDecoders[0].decode(slice, session)
                    16 -> mapProjAnimHalfsqV2Decoder.decode(slice, session)
                    17 -> objDelDecoders[1].decode(slice, session)
                    else -> break@loop
                }
            packets.add(message)
        }

        return UpdateZonePartialEnclosed(level, zoneX, zoneZ, packets)
    }

    private fun sliceBytes(slice: JagByteBuf): ByteArray {
        val bytes = ByteArray(slice.readableBytes())
        slice.buffer.readBytes(bytes)
        return bytes
    }

    private fun subOpLength(subOp: Int): Int =
        when (subOp) {
            // unconfirmed
            0 -> 10 // SOUND_AREA
            1 -> VAR_BYTE_SENTINEL // TEXT_COORD
            2 -> VAR_BYTE_SENTINEL // SUB_VARBYTE
            3 -> 7 // OBJ_REVEAL
            4 -> 20 // MAP_PROJANIM
            5 -> 21 // MAP_PROJANIM_HALFSQ
            6 -> 11 // MAP_ANIM
            7 -> 3 // OBJ_DEL
            8 -> 2 // LOC_DEL
            9 -> 5 // OBJ_ADD
            10 -> 14 // MAP_ANIM_V2
            11 -> 8 // OBJ_COUNT_V2
            12 -> 7 // LOC_ANIM
            13 -> 6 // OBJ_ADD_V2
            14 -> 7 // OBJ_COUNT
            15 -> 11 // MAP_ANIM
            16 -> 29 // MAP_PROJANIM_HALFSQ_V2
            17 -> 4 // OBJ_DEL_V2
            else -> 0
        }

    private companion object {
        const val VAR_BYTE_SENTINEL = -1
    }
}
