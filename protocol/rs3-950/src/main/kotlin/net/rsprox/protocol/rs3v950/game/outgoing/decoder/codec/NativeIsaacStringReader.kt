package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprox.protocol.rs3v950.buffer.nativeCharacter

/** The stream is shared with opcode decoding; each consumed byte (including NUL) draws one key. */
internal fun JagByteBuf.readNativeIsaacString(cipher: StreamCipher, limit: Long): String =
    buildString {
        var count = 0L
        while (count++ < limit) {
            val byte = (g1() - cipher.nextInt()) and 255
            if (byte == 0) break
            val character = nativeCharacter(byte)
            if (character != '\u0000') append(character)
        }
    }
