package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec

import net.rsprot.buffer.JagByteBuf

/** Unsigned 32-bit LEB128; deliberately not the big-endian RuneScape varint. */
internal fun JagByteBuf.gVarIntLE(): Int {
    var value = 0
    for (shift in 0..28 step 7) {
        val next = g1()
        require(shift != 28 || next <= 0x0F) { "Variable integer exceeds 32 bits" }
        value = value or ((next and 0x7F) shl shift)
        if (next and 0x80 == 0) return value
    }
    error("Unterminated variable integer")
}
