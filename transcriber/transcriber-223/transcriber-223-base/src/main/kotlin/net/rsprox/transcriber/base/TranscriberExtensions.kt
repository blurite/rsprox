package net.rsprox.transcriber.base

internal fun Int.maxUShortToMinusOne(): Int {
    return if (this == 0xFFFF) {
        -1
    } else {
        this
    }
}

internal fun Int.toFullBinaryString(bitcount: Int): String {
    val builder = StringBuilder("0b")
    for (i in bitcount.dec() downTo 0) {
        val char = if (this ushr i and 0x1 != 0) '1' else '0'
        builder.append(char)
    }
    return builder.toString()
}

public inline fun <reified S> List<*>.firstOfInstanceOfNull(): S? {
    return firstOrNull { it is S } as? S
}
