package net.rsprox.cache.rs3

import io.netty.buffer.Unpooled
import net.rsprox.cache.api.rs3.Rs3Gameval
import net.rsprox.cache.api.rs3.Rs3Gamevals
import org.openrs2.buffer.readString
import org.openrs2.buffer.use
import java.util.Locale

/** Archive 67 debug-name packs. These groups are not ordinary multi-file config groups. */
internal object Rs3GamevalDecoder {
    const val ARCHIVE: Int = 67

    fun decode(groups: Map<Rs3Gameval, ByteArray>): Rs3Gamevals {
        val names = mutableMapOf<Rs3Gameval, MutableMap<Int, String>>()
        for (type in Rs3Gameval.entries) {
            val bytes = groups[type] ?: continue
            Unpooled.wrappedBuffer(bytes).use { buffer ->
                val packType = buffer.readInt()
                require(packType == 1 || packType == 2) { "Unsupported RS3 gameval pack type: $packType" }
                val count = buffer.readInt()
                val entrySize = if (packType == 1) 4 else 8
                require(count >= 0 && count <= buffer.readableBytes() / entrySize) { "Invalid gameval entry count" }
                val ids = IntArray(count)
                val offsets = IntArray(count)
                repeat(count) { index ->
                    ids[index] = if (packType == 1) index else buffer.readInt()
                    offsets[index] = buffer.readInt()
                }
                val tableStart = buffer.readerIndex()
                val tableLength = buffer.readableBytes()
                var lastNoPrefix = -1
                repeat(count) { index ->
                    val offset = offsets[index]
                    if (offset == -1) return@repeat
                    require(offset in 0 until tableLength) { "Invalid gameval string offset" }
                    buffer.readerIndex(tableStart + offset)
                    var name = buffer.readString().lowercase(Locale.ROOT)
                    var id = ids[index]
                    var target = type
                    if (type != Rs3Gameval.SOUND) {
                        if (name.startsWith('_')) {
                            target = Rs3Gameval.VARBIT
                            name = name.substring(1)
                            id = Math.subtractExact(Math.subtractExact(id, lastNoPrefix), 1)
                        } else {
                            lastNoPrefix = id
                        }
                    }
                    require(id >= 0) { "Invalid gameval id" }
                    names.getOrPut(target) { mutableMapOf() }[id] = name
                }
            }
        }
        names[Rs3Gameval.COMPONENT]?.let { components ->
            names[Rs3Gameval.COMPONENT] = combineComponents(components, names[Rs3Gameval.INTERFACE].orEmpty())
        }
        return Rs3Gamevals(names)
    }

    private fun combineComponents(
        names: Map<Int, String>,
        interfaces: Map<Int, String>,
    ): MutableMap<Int, String> {
        val interfaceIds = interfaces.entries.associate { (id, name) -> name to id }
        val nextComponentIds = mutableMapOf<Int, Int>()
        val result = mutableMapOf<Int, String>()
        for (name in names.toSortedMap().values) {
            val separator =
                when {
                    ':' in name -> ":"
                    "__" in name -> "__"
                    else -> continue
                }
            val interfaceId = interfaceIds[name.substringBefore(separator)] ?: continue
            val componentId = nextComponentIds.getOrDefault(interfaceId, 0)
            require(interfaceId in 0..0xffff && componentId in 0..0xffff) { "Invalid gameval component id" }
            nextComponentIds[interfaceId] = componentId + 1
            result[(interfaceId shl 16) or componentId] =
                name.substringBefore(separator) + ':' + name.substringAfter(separator)
        }
        return result
    }
}
