package net.rsprox.transcriber

import java.text.NumberFormat

public fun String.indent(indentation: Int): String {
    return "  ".repeat(indentation) + this
}

public fun String.quote(): String {
    return "'$this'"
}

public fun Int.format(): String {
    return NumberFormat.getNumberInstance().format(this)
}
