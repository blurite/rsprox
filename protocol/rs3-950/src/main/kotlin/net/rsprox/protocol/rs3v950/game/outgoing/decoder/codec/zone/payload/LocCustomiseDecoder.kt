package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.LocCustomise
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LocCustomiseDecoder : ProxyMessageDecoder<LocCustomise> {
    override val prot: ClientProt = GameServerProt.LOC_CUSTOMISE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocCustomise {
        val startIndex = buffer.buffer.readerIndex()
        val locId = buffer.g4Alt1()

        val coordByte = buffer.g1()
        val zInZone = coordByte and 0x7
        val xInZone = (coordByte ushr 4) and 0x7

        val shapeRotationByte = buffer.g1Alt3()
        val shape = (shapeRotationByte ushr 2) and 0x1F
        val rotation = shapeRotationByte and 0x3

        val flagsByte = buffer.g1()

        val hasExtendedTransform = (shapeRotationByte and 0x80) != 0
        var rotationX = 0f
        var rotationY = 0f
        var rotationZ = 0f
        var rotationW = 1f
        var translateA = 0f
        var translateB = 0f
        var translateC = 0f
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
                translateA = buffer.g2s().toFloat()
            }
            if ((innerFlags and 0x4) != 0) {
                translateB = -(buffer.g2s().toFloat())
            }
            if ((innerFlags and 0x8) != 0) {
                translateC = buffer.g2s().toFloat()
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

        var uintArray: IntArray? = null
        var opcodeArrayA: IntArray? = null
        var opcodeArrayB: IntArray? = null

        if ((flagsByte and 0x1) == 0) {
            if ((flagsByte and 0x2) != 0) {
                val count = buffer.g1()
                uintArray = IntArray(count) { buffer.g4() }
            }
            if ((flagsByte and 0x4) != 0) {
                val count = buffer.g1()
                opcodeArrayA = IntArray(count) { buffer.g2() }
            }
            if ((flagsByte and 0x8) != 0) {
                val count = buffer.g1()
                opcodeArrayB = IntArray(count) { buffer.g2() }
            }
        }

        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)

        return LocCustomise(
            locId,
            xInZone,
            zInZone,
            shape,
            rotation,
            hasExtendedTransform,
            rotationX,
            rotationY,
            rotationZ,
            rotationW,
            translateA,
            translateB,
            translateC,
            scaleX,
            scaleY,
            scaleZ,
            uintArray,
            opcodeArrayA,
            opcodeArrayB,
            rawBytes,
        )
    }

    private companion object {
        const val ROTATION_SCALE = 3.0517578e-05f
        const val SCALE_SCALE = 0.0078125f
    }
}
