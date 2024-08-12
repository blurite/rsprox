package net.rsprox.patch

import kotlin.math.min

/**
 * Finds the first index of the [pattern] in the [data], or -1 if nothing can be found.
 * @param data the byte array to search
 * @param pattern the array to search inside the data
 * @param offset the index to begin searching at
 * @return the starting index of the [data] inside the [pattern], or -1 if it cannot be found.
 */
public fun findBoyerMoore(
    data: ByteArray,
    pattern: ByteArray,
    offset: Int = 0,
): Int {
    val n = data.size
    val m = pattern.size
    if (m == 0) return 0
    val last =
        IntArray(256) {
            -1
        }
    for (i in 0 until m) {
        last[pattern[i].toInt() and 0xFF] = i
    }
    var i = offset + m - 1
    var k = m - 1
    while (i < n) {
        if (data[i] == pattern[k]) {
            if (k == 0) {
                return i
            }
            i--
            k--
        } else {
            i += m - min(k, 1 + last[data[i].toInt() and 0xFF])
            k = m - 1
        }
    }
    return -1
}

/**
 * Finds the first index of the [pattern] in the [data], or -1 if nothing can be found.
 * @param data the byte array to search
 * @param pattern the array to search inside the data
 * @param offset the index to begin searching at
 * @return the starting index of the [data] inside the [pattern], or -1 if it cannot be found.
 */
public fun findBoyerMooreIgnoreNulls(
    data: ByteArray,
    pattern: List<Byte?>,
    offset: Int = 0,
): Int {
    val n = data.size
    val m = pattern.size
    if (m == 0) return 0
    val last =
        IntArray(256) {
            -1
        }
    for (i in 0 until m) {
        val byte = pattern[i] ?: continue
        last[byte.toInt() and 0xFF] = i
    }
    var i = offset + m - 1
    var k = m - 1
    while (i < n) {
        val inputByte = pattern[k]
        if (inputByte == null || data[i] == inputByte) {
            if (k == 0) {
                return i
            }
            i--
            k--
        } else {
            i += m - min(k, 1 + last[data[i].toInt() and 0xFF])
            k = m - 1
        }
    }
    return -1
}

private const val NUL_CHAR: Char = 0.toChar()

public fun String.nullTerminate(): String {
    return "$this$NUL_CHAR"
}

public fun String.nullWrap(): String {
    return "$NUL_CHAR$this$NUL_CHAR"
}
