package net.rsprox.protocol.rs3v950.buffer

import net.rsprot.buffer.JagByteBuf

/** Native CP1252 conversion drops undefined extension bytes, unlike replacement-character decoders. */
internal fun JagByteBuf.readNativeString(): String =
    buildString {
        while (true) {
            val byte = g1()
            if (byte == 0) break
            val character = nativeCharacter(byte)
            if (character != '\u0000') append(character)
        }
    }

/** A version-zero CP1252 string, not a second length prefix. */
internal fun JagByteBuf.readNativeString2(): String {
    require(g1() == 0) { "Expected a zero native string version marker" }
    return readNativeString()
}

internal fun nativeCharacter(byte: Int): Char = if (byte in 128..159) CP1252_EXTENSION[byte - 128] else byte.toChar()

private const val CP1252_EXTENSION =
    "\u20ac\u0000\u201a\u0192\u201e\u2026\u2020\u2021\u02c6\u2030\u0160\u2039\u0152\u0000\u017d\u0000" +
        "\u0000\u2018\u2019\u201c\u201d\u2022\u2013\u2014\u02dc\u2122\u0161\u203a\u0153\u0000\u017e\u0178"
