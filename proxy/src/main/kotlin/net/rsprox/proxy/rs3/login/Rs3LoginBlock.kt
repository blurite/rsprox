package net.rsprox.proxy.rs3.login

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.crypto.cipher.IsaacRandom
import net.rsprot.crypto.cipher.StreamCipherPair

/**
 * The relay needs only the cipher seed prefix. Retain the complete original RSA plaintext:
 * authentication fields, padding and extensions are not ours to interpret or reconstruct.
 */
public class Rs3LoginBlock private constructor(
    private val plaintext: ByteArray,
    private val seeds: IntArray,
) {
    public fun buildStreamCipherPair(): StreamCipherPair {
        return StreamCipherPair(
            encoderCipher = IsaacRandom(seeds.copyOf()),
            decodeCipher = IsaacRandom(IntArray(seeds.size) { seeds[it] + 50 }),
        )
    }

    public companion object {
        public const val MAGIC: Int = 10

        public fun decode(buffer: ByteBuf): Rs3LoginBlock {
            require(buffer.readableBytes() >= 17) { "Truncated RSA cipher seed prefix" }
            val start = buffer.readerIndex()
            check(buffer.getUnsignedByte(start).toInt() == MAGIC) { "RSA magic mismatch" }
            val seeds = IntArray(4) { buffer.getInt(start + 1 + it * 4) }
            val plaintext = ByteArray(buffer.readableBytes())
            buffer.readBytes(plaintext)
            return Rs3LoginBlock(plaintext, seeds)
        }

        public fun encode(block: Rs3LoginBlock): ByteBuf = Unpooled.wrappedBuffer(block.plaintext.copyOf())
    }
}
