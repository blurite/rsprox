package net.rsprox.cache.type

import net.rsprot.buffer.JagByteBuf

internal object ParamTypeHelper {
    fun unpackClientParams(
        buffer: JagByteBuf,
        params: MutableMap<Int, Any>,
    ) {
        val count = buffer.g1()
        for (i in 0..<count) {
            val isString = buffer.g1() == 1
            val id = buffer.g3()
            val value =
                if (isString) {
                    buffer.gjstr()
                } else {
                    buffer.g4()
                }
            params[id] = value
        }
    }
}
