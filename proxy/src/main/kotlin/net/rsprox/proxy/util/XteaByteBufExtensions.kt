package net.rsprox.proxy.util

import io.netty.buffer.ByteBuf

private const val GOLDEN_RATIO = 0x9E3779B9.toInt()
private const val ROUNDS = 32
public const val XTEA_BLOCK_SIZE: Int = 8
private const val BLOCK_SIZE_MASK = XTEA_BLOCK_SIZE - 1

public fun ByteBuf.xteaEncrypt(
    key: IntArray,
    index: Int = readerIndex(),
    length: Int = writerIndex(),
): ByteBuf {
    val result = alloc().buffer(readableBytes())
    val end = index + (length and BLOCK_SIZE_MASK.inv())
    for (i in index until end step XTEA_BLOCK_SIZE) {
        var sum = 0
        var v0 = readInt()
        var v1 = readInt()

        for (j in 0 until ROUNDS) {
            v0 += (((v1 shl 4) xor (v1 ushr 5)) + v1) xor (sum + key[sum and 3])
            sum += GOLDEN_RATIO
            v1 += (((v0 shl 4) xor (v0 ushr 5)) + v0) xor (sum + key[(sum ushr 11) and 3])
        }

        result.writeInt(v0)
        result.writeInt(v1)
    }
    return result
        .writeBytes(this, readableBytes())
}
