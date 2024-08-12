package net.rsprox.patch.native.processors.utils

import net.rsprox.patch.findBoyerMoore
import net.rsprox.patch.findBoyerMooreIgnoreNulls
import net.rsprox.patch.native.Client

internal fun Client.indexOf(
    search: HexBytePattern,
    startIndex: Int = 0,
): Int {
    require(search.length > 0) {
        "Bytes to search are empty"
    }
    require(startIndex >= 0) {
        "Start index is negative"
    }
    val input =
        buildList(search.length) {
            for (i in 0..<search.length) {
                add(search.getByteOrWildcard(i))
            }
        }
    return findBoyerMooreIgnoreNulls(bytes, input, startIndex)
}

internal fun Client.indexOf(
    search: ByteArray,
    startIndex: Int = 0,
): Int {
    require(search.isNotEmpty()) {
        "Bytes to search are empty"
    }
    require(startIndex >= 0) {
        "Start index is negative"
    }
    return findBoyerMoore(bytes, search, startIndex)
}

internal fun Client.replace(
    search: HexBytePattern,
    replacement: HexBytePattern,
    searchStartIndex: Int = 0,
): Boolean {
    val index = indexOf(search, searchStartIndex)
    if (index == -1) {
        return false
    }
    check(search isCompatible replacement)
    write(replacement, index)
    return true
}

internal fun Client.write(
    pattern: HexBytePattern,
    startIndex: Int,
): ByteArray {
    val old = bytes.sliceArray(startIndex..<(startIndex + pattern.length))
    for (i in 0..<pattern.length) {
        val new = pattern.getByteOrWildcard(i) ?: continue
        bytes[startIndex + i] = new
    }
    return old
}
