package net.rsprox.cache.rs3

import net.rsprox.cache.api.rs3.Rs3NpcDefinition
import net.rsprox.cache.api.rs3.Rs3NpcMorph
import java.nio.ByteBuffer

/** Revision-950 NPC metadata required to delimit appearance customisations. */
internal object Rs3NpcDefinitionDecoder {
    fun decode(
        id: Int,
        bytes: ByteArray,
    ): Rs3NpcDefinition {
        val input = ByteBuffer.wrap(bytes)

        fun byte(): Int = input.get().toInt() and 255

        fun word(): Int = input.short.toInt() and 65535

        fun smart(): Int = if (input.get(input.position()) < 0) input.int and Int.MAX_VALUE else word()

        fun small(): Int = if (input.get(input.position()) < 0) word() - 32768 else byte()

        fun skip(count: Int) {
            require(count in 0..input.remaining()) { "Truncated NPC definition $id" }
            input.position(input.position() + count)
        }

        fun string() {
            while (byte() != 0) Unit
        }
        var recolours = 0
        var retextures = 0
        var recolourMapping = 0
        var retextureMapping = 0
        var morph: Rs3NpcMorph? = null
        while (true) {
            when (val opcode = byte()) {
                0 -> {
                    require(!input.hasRemaining()) { "Trailing NPC definition $id bytes" }
                    return Rs3NpcDefinition(
                        if (recolourMapping != 0) recolourMapping else recolours,
                        if (retextureMapping != 0) retextureMapping else retextures,
                        morph,
                    )
                }
                1, 60 -> repeat(byte()) { smart() }
                2, 3, in 30..34, in 150..154 -> string()
                12, 39, 100, 101, 119, 125, 128, 140, 163, 165, 168, 180, 184, 253 -> byte()
                13, 14, 15, 16, 18, 26, 27, 95, 97, 98, 103, 122, 123, 127, 137, 142,
                in 170..175, 252,
                -> word()
                17 -> skip(8)
                40 -> {
                    recolours = byte()
                    skip(recolours * 4)
                }
                41 -> {
                    retextures = byte()
                    skip(retextures * 4)
                }
                42 -> skip(byte())
                44 -> {
                    val mask = word()
                    recolourMapping = Integer.bitCount(mask)
                    recolours = 32 - Integer.numberOfLeadingZeros(mask)
                }
                45 -> {
                    val mask = word()
                    retextureMapping = Integer.bitCount(mask)
                    retextures = 32 - Integer.numberOfLeadingZeros(mask)
                }
                93, 99, 107, 109, 111, 141, 143, 158, 159, 162, 169, 178, 182, 185 -> Unit
                102 -> {
                    val mask = byte()
                    repeat(8) {
                        if (mask and (1 shl it) != 0) {
                            smart()
                            small()
                        }
                    }
                }
                106, 118, 187, 188 -> {
                    // Native 0x78e7f0: newer morph definitions use a 24-bit varbit.
                    val wide = opcode >= 187
                    val varbit = if (wide) (byte() shl 16) or word() else word()
                    val varp = word()
                    val fallback = if (opcode == 118 || opcode == 188) word() else 65535
                    val types = List(small() + 1) { word().let { if (it == 65535) -1 else it } }
                    morph =
                        Rs3NpcMorph(
                            if (varbit == if (wide) 0xffffff else 65535) -1 else varbit,
                            if (varp == 65535) -1 else varp,
                            types + if (fallback == 65535) -1 else fallback,
                        )
                }
                113, 155, 164 -> skip(4)
                114, 115 -> skip(2)
                120 -> skip(7)
                121 -> skip(byte() * 4)
                134 -> skip(9)
                135, 136, 181 -> skip(3)
                138, 139 -> smart()
                160 -> repeat(byte()) { word() }
                179 -> repeat(6) { small() }
                186, 189 -> {
                    // 0x19f41c has the same records; 189 widens its varbit to three bytes.
                    skip(if (opcode == 189) 7 else 6)
                    val flags = byte()
                    for (kind in 0..3) {
                        if (flags and (1 shl kind) == 0) continue
                        repeat(byte()) {
                            byte()
                            repeat(byte()) {
                                skip(4)
                                if (kind <= 1) smart() else skip(4)
                                if (kind == 0) {
                                    val count = byte()
                                    skip(minOf(count, 3))
                                }
                            }
                        }
                    }
                    if (flags and 16 != 0) skip(byte() * 8)
                    word()
                }
                249 ->
                    repeat(byte()) {
                        val isString = byte() == 1
                        skip(3)
                        if (isString) string() else skip(4)
                    }
                else -> error("Unknown NPC $id definition opcode $opcode")
            }
        }
    }
}
