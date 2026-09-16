package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.rs3.game.incoming.model.events.MouseMovement

internal fun JagByteBuf.readMouseMovements(nativeMouse: Boolean): List<MouseMovement> {
    require(isReadable) { "Mouse movement payload has no records" }
    return buildList {
        while (isReadable) {
            val tag = g1()
            val elapsedTicks: Int
            val x: Int
            val y: Int
            val absolute: Boolean
            when {
                tag < 0x80 -> {
                    val packed = (tag shl 8) or g1()
                    elapsedTicks = packed ushr 12
                    x = ((packed ushr 6) and 63) - 32
                    y = (packed and 63) - 32
                    absolute = false
                }
                tag < 0xA0 -> {
                    elapsedTicks = tag - 0x80
                    x = g1() - 128
                    y = g1() - 128
                    absolute = false
                }
                tag < 0xC0 -> error("Invalid native mouse movement tag $tag")
                else -> {
                    elapsedTicks = if (tag < 0xE0) tag - 0xC0 else ((tag and 31) shl 8) or g1()
                    val packed = g4()
                    x = if (packed == Int.MIN_VALUE) -1 else packed and 0xFFFF
                    y = if (packed == Int.MIN_VALUE) -1 else packed ushr 16
                    absolute = true
                }
            }
            val nativeFlags = if (nativeMouse) g1() else null
            add(
                if (absolute) {
                    MouseMovement.Absolute(elapsedTicks, x, y, nativeFlags)
                } else {
                    MouseMovement.Delta(elapsedTicks, x, y, nativeFlags)
                },
            )
        }
    }
}
