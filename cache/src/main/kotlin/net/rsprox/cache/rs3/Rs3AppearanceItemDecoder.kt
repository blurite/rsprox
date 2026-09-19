package net.rsprox.cache.rs3

import net.rsprox.cache.api.rs3.Rs3AppearanceItem
import java.nio.ByteBuffer

/** Revision-950 obj definitions. Unused fields are consumed by schema, never by a remainder guess. */
internal object Rs3AppearanceItemDecoder {
    fun decode(
        id: Int,
        bytes: ByteArray,
        resolve: ((Int) -> Rs3AppearanceItem)? = null,
    ): Rs3AppearanceItem {
        val input = ByteBuffer.wrap(bytes)

        fun byte(): Int = input.get().toInt() and 255

        fun word(): Int = input.short.toInt() and 65535

        fun smart(): Int =
            if (input.get(input.position()) < 0) {
                input.int and Int.MAX_VALUE
            } else {
                word().let {
                    if (it == 32767) -1 else it
                }
            }

        fun skip(count: Int) {
            require(count in 0..input.remaining()) { "Truncated RS3 item $id" }
            input.position(input.position() + count)
        }

        fun string() {
            while (byte() != 0) Unit
        }
        val maleBody = MutableList(3) { -1 }
        val femaleBody = MutableList(3) { -1 }
        val maleHead = MutableList(2) { -1 }
        val femaleHead = MutableList(2) { -1 }

        fun tri(): Int = (byte() shl 16) or (byte() shl 8) or byte()
        var lendLink = -1
        var lendTemplate = -1
        var boughtLink = -1
        var boughtTemplate = -1
        while (true) {
            when (val opcode = byte()) {
                0 -> {
                    require(!input.hasRemaining()) { "Trailing RS3 item $id definition bytes" }
                    // Native 0x78d510 -> 0x78cfa0: lent (kind 1), then bought (kind 2),
                    // copy all wearable models from the linked item, never the inventory template.
                    val link =
                        when {
                            boughtTemplate != -1 -> boughtLink
                            lendTemplate != -1 -> lendLink
                            else -> -1
                        }
                    if (boughtTemplate != -1 || lendTemplate != -1) {
                        require(link >= 0) { "RS3 item $id has a wearable template without a link" }
                        return checkNotNull(resolve) { "RS3 item $id needs linked item $link" }(link)
                    }
                    return Rs3AppearanceItem(maleBody, femaleBody, maleHead, femaleHead)
                }
                23 -> maleBody[0] = smart()
                24 -> maleBody[1] = smart()
                78 -> maleBody[2] = smart()
                25 -> femaleBody[0] = smart()
                26 -> femaleBody[1] = smart()
                79 -> femaleBody[2] = smart()
                90 -> maleHead[0] = smart()
                92 -> maleHead[1] = smart()
                91 -> femaleHead[0] = smart()
                93 -> femaleHead[1] = smart()
                1, in 46..56 -> smart()
                9 -> repeat(byte()) { smart() }
                2, 3, in 30..39, 131, 164 -> string()
                11, 15, 16, 65, 156, 157, 165, 167, 168, 178 -> Unit
                13, 14, 27, 96, 113, 114, 115, 134 -> byte()
                4, 5, 6, 7, 8, 10, 44, 45, 94, 95, 97, 98, 110, 111, 112,
                148, 149, in 142..146, in 150..154, 161, 162, 163,
                -> word()
                121 -> lendLink = word()
                122 -> lendTemplate = word()
                139 -> boughtLink = word()
                140 -> boughtTemplate = word()
                203 -> lendLink = tri()
                204 -> lendTemplate = tri()
                205 -> boughtLink = tri()
                206 -> boughtTemplate = tri()
                12, 43, 69, in 100..109 -> skip(4)
                40, 41 -> repeat(byte()) { skip(4) }
                42 -> skip(byte())
                125, 126, in 127..130, 182, 201, 202, 207, 208 -> skip(3)
                132 -> repeat(byte()) { word() }
                181 -> skip(8)
                in 190..199 -> skip(5)
                249 ->
                    repeat(byte()) {
                        val isString = byte() == 1
                        skip(3)
                        if (isString) string() else skip(4)
                    }
                else -> error("Unknown RS3 item $id definition opcode $opcode")
            }
        }
    }
}
