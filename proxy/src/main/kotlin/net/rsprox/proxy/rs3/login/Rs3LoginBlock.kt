package net.rsprox.proxy.rs3.login

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g4
import net.rsprot.buffer.extensions.g8
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p4
import net.rsprot.buffer.extensions.p8
import net.rsprot.crypto.cipher.IsaacRandom
import net.rsprot.crypto.cipher.StreamCipherPair

public data class Rs3LoginBlock(
    public val seeds: IntArray,
    public val uniqueId: Long,
    public val weirdThingId: Int,
    public val weirdThingValue: Int,
    public val someBoolean: Boolean,
    public val password: String,
) {

    public fun buildStreamCipherPair(): StreamCipherPair {
        val decodeSeeds = IntArray(seeds.size) { seeds[it] + 50 }
        return StreamCipherPair(
            encoderCipher = IsaacRandom(seeds),
            decodeCipher = IsaacRandom(decodeSeeds),
        )
    }

    public companion object {
        public const val MAGIC: Int = 10

        public fun decode(buffer: ByteBuf): Rs3LoginBlock {
            val magic = buffer.g1()
            check(magic == MAGIC) { "RSA magic mismatch: expected $MAGIC, got $magic" }

            val seeds = IntArray(4) { buffer.g4() }
            val uniqueId = buffer.g8()

            val weirdThingId = buffer.g1()
            val weirdThingValue =
                when (weirdThingId) {
                    0, 1 -> {
                        val value = (buffer.g1() shl 16) or (buffer.g1() shl 8) or buffer.g1()
                        buffer.g1()
                        value
                    }
                    2 -> buffer.g4()
                    3 -> {
                        buffer.g4()
                        0
                    }
                    else -> error("Unexpected weirdThingId: $weirdThingId")
                }

            val someBoolean = buffer.g1() == 1
            val password = readNullTerminatedString(buffer)

            return Rs3LoginBlock(
                seeds,
                uniqueId,
                weirdThingId,
                weirdThingValue,
                someBoolean,
                password,
            )
        }

        public fun encode(block: Rs3LoginBlock): ByteBuf {
            val buffer = Unpooled.buffer()
            buffer.p1(MAGIC)
            for (seed in block.seeds) {
                buffer.p4(seed)
            }
            buffer.p8(block.uniqueId)
            buffer.p1(block.weirdThingId)
            when (block.weirdThingId) {
                0, 1 -> {
                    val v = block.weirdThingValue
                    buffer.p1((v shr 16) and 0xFF)
                    buffer.p1((v shr 8) and 0xFF)
                    buffer.p1(v and 0xFF)
                    buffer.p1(0)
                }
                2 -> buffer.p4(block.weirdThingValue)
                3 -> buffer.p4(0)
                else -> error("Unexpected weirdThingId: ${block.weirdThingId}")
            }
            buffer.p1(if (block.someBoolean) 1 else 0)
            writeNullTerminatedString(buffer, block.password)
            return buffer
        }

        private fun readNullTerminatedString(buffer: ByteBuf): String {
            val start = buffer.readerIndex()
            var length = 0
            while (buffer.getByte(start + length) != 0.toByte()) {
                length++
            }
            val bytes = ByteArray(length)
            buffer.getBytes(start, bytes)
            buffer.readerIndex(start + length + 1)
            return String(bytes, Charsets.ISO_8859_1)
        }

        private fun writeNullTerminatedString(
            buffer: ByteBuf,
            value: String,
        ) {
            buffer.writeBytes(value.toByteArray(Charsets.ISO_8859_1))
            buffer.p1(0)
        }
    }
}
