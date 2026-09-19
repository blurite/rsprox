package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.LocAnim
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LocAnimDecoder : ProxyMessageDecoder<LocAnim> {
    override val prot: ClientProt = GameServerProt.LOC_ANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocAnim {
        val shapeRot = buffer.g1Alt2()
        val shape = (shapeRot ushr 2) and 0x1F
        val rotation = shapeRot and 0x3

        val id = buffer.g4Alt2()
        val delay = buffer.g1Alt2()

        val packedCoord = buffer.g1Alt2()
        val zInZone = packedCoord and 0x7
        val xInZone = (packedCoord ushr 4) and 0x7

        // Extensions follow all seven base bytes, not the shape byte itself.
        val hasExtendedTransform = shapeRot and 0x80 != 0
        var rotationX = 0f
        var rotationY = 0f
        var rotationZ = 0f
        var rotationW = 1f
        var translationX = 0f
        var translationY = 0f
        var translationZ = 0f
        var scaleX = 1f
        var scaleY = 1f
        var scaleZ = 1f

        if (hasExtendedTransform) {
            val innerFlags = buffer.g1()
            if ((innerFlags and 0x1) != 0) {
                val a = buffer.g2s()
                val b = buffer.g2s()
                val c = buffer.g2s()
                val d = buffer.g2s()
                rotationX = -(a * ROTATION_SCALE)
                rotationY = b * ROTATION_SCALE
                rotationZ = -(c * ROTATION_SCALE)
                rotationW = d * ROTATION_SCALE
            }
            if ((innerFlags and 0x2) != 0) {
                translationX = buffer.g2s().toFloat()
            }
            if ((innerFlags and 0x4) != 0) {
                translationY = -(buffer.g2s().toFloat())
            }
            if ((innerFlags and 0x8) != 0) {
                translationZ = buffer.g2s().toFloat()
            }
            if ((innerFlags and 0x10) != 0) {
                val uniform = buffer.g2s() * SCALE_SCALE
                scaleX = uniform
                scaleY = uniform
                scaleZ = uniform
            } else {
                if ((innerFlags and 0x20) != 0) {
                    scaleX = buffer.g2s() * SCALE_SCALE
                }
                if ((innerFlags and 0x40) != 0) {
                    scaleY = buffer.g2s() * SCALE_SCALE
                }
                if ((innerFlags and 0x80) != 0) {
                    scaleZ = buffer.g2s() * SCALE_SCALE
                }
            }
        }

        return LocAnim(
            id,
            xInZone,
            zInZone,
            shape,
            rotation,
            delay,
            hasExtendedTransform,
            rotationX,
            rotationY,
            rotationZ,
            rotationW,
            translationX,
            translationY,
            translationZ,
            scaleX,
            scaleY,
            scaleZ,
        )
    }

    private companion object {
        const val ROTATION_SCALE = 3.0517578e-05f
        const val SCALE_SCALE = 0.0078125f
    }
}
