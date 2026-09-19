package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.selection

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.selection.LocSelectAdd
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LocSelectAddDecoder : ProxyMessageDecoder<LocSelectAdd> {
    override val prot: ClientProt = GameServerProt.LOCSELECT_ADD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocSelectAdd {
        val coordinate = buffer.g2Alt2()
        val id = buffer.g4Alt2()
        val shapeRotation = buffer.g1Alt3()
        val transform =
            if (shapeRotation and 0x80 != 0) {
                val flags = buffer.g1()
                val quaternion = if (flags and 1 != 0) List(4) { buffer.g2s() } else null
                val translateX = if (flags and 2 != 0) buffer.g2s() else null
                val translateY = if (flags and 4 != 0) buffer.g2s() else null
                val translateZ = if (flags and 8 != 0) buffer.g2s() else null
                val uniformScale = if (flags and 16 != 0) buffer.g2s() else null
                val scaleX = if (uniformScale == null && flags and 32 != 0) buffer.g2s() else null
                val scaleY = if (uniformScale == null && flags and 64 != 0) buffer.g2s() else null
                val scaleZ = if (uniformScale == null && flags and 128 != 0) buffer.g2s() else null
                LocSelectAdd.Transform(
                    flags,
                    quaternion,
                    translateX,
                    translateY,
                    translateZ,
                    uniformScale,
                    scaleX,
                    scaleY,
                    scaleZ,
                )
            } else {
                null
            }
        return LocSelectAdd(coordinate, id, shapeRotation, transform)
    }
}
