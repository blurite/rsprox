package net.rsprox.cache.type

import net.rsprot.buffer.JagByteBuf

internal object ParamTypeHelper {
    fun unpackClientParams(
        buffer: JagByteBuf,
        params: MutableMap<Int, Any>,
    ) {
        val count = buffer.g1()
        for (i in 0..<count) {
            val type = buffer.g1()
            val id = buffer.g3()
            val value =
                when (type) {
                    1 -> buffer.gjstr()
                    2 -> buffer.g8()
                    else -> buffer.g4()
                }
            params[id] = value
        }
    }
}
