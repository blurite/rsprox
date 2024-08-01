package net.rsprox.shared.symbols

import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines

public data class SymbolTypeEntry(
    public val path: Path,
    public val regex: Regex,
) {
    public fun read(): Map<Int, String> {
        if (!path.exists(LinkOption.NOFOLLOW_LINKS)) {
            return emptyMap()
        }
        val map = mutableMapOf<Int, String>()
        for (line in path.readLines()) {
            val result = regex.toPattern().matcher(line) ?: continue
            if (!result.find()) continue
            try {
                val name = result.group("NAME")
                val id = result.group("ID").toInt()
                map[id] = name
            } catch (e: Exception) {
                continue
            }
        }
        return map
    }
}
