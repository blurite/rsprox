package net.rsprox.transcriber

import java.text.NumberFormat

public const val SINGLE_INDENTATION: String = "    "

public fun String.indent(indentation: Int): String {
    return SINGLE_INDENTATION.repeat(indentation) + this
}

public fun String.quote(): String {
    return "'$this'"
}

public fun Int.format(): String {
    return NumberFormat.getNumberInstance().format(this)
}

public fun Long.format(): String {
    return NumberFormat.getNumberInstance().format(this)
}
