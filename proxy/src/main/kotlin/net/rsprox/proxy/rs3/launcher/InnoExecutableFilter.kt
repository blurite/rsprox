/*
 * Adapted from innoextract's Inno 5.3.9+ executable decoder (stream/exefilter.hpp).
 * Copyright (C) 2011-2019 Daniel Scharrer
 *
 * This software is provided 'as-is', without any express or implied warranty. In no event
 * will the authors be held liable for any damages arising from the use of this software.
 * Permission is granted to anyone to use this software for any purpose, including commercial
 * applications, and to alter it and redistribute it freely, subject to these restrictions:
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote
 *    the original software. If you use this software in a product, an acknowledgment in the
 *    product documentation would be appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be misrepresented
 *    as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 *
 * This is an altered, bounded byte-array Kotlin adaptation, not the original streaming decoder.
 */
package net.rsprox.proxy.rs3.launcher

internal object InnoExecutableFilter {
    fun decode(bytes: ByteArray) {
        var position = 0
        while (position + 4 < bytes.size) {
            val opcode = bytes[position++].toInt() and 255
            if (opcode !in 0xE8..0xE9 || (position - 1) and 65535 > 65531) continue
            val high = bytes[position + 3].toInt() and 255
            if (high == 0 || high == 255) {
                val absolute =
                    (bytes[position].toInt() and 255) or
                        ((bytes[position + 1].toInt() and 255) shl 8) or
                        ((bytes[position + 2].toInt() and 255) shl 16)
                val relative = absolute - (position + 4)
                bytes[position] = relative.toByte()
                bytes[position + 1] = (relative ushr 8).toByte()
                bytes[position + 2] = (relative ushr 16).toByte()
                if (relative and 0x800000 != 0) bytes[position + 3] = high.inv().toByte()
            }
            position += 4
        }
    }
}
