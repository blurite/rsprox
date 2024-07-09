package net.rsprox.proxy.huffman

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec

public data object HuffmanProvider {
    private val logger = InlineLogger()
    private lateinit var huffman: HuffmanCodec

    public fun load() {
        try {
            this.huffman = createHuffmanCodec()
        } catch (t: Throwable) {
            logger.error(t) {
                "Unable to create huffman codec"
            }
            throw t
        }
    }

    public fun get(): HuffmanCodec {
        return huffman
    }

    private fun createHuffmanCodec(): HuffmanCodec {
        val resource = HuffmanProvider::class.java.getResourceAsStream("huffman.dat")
        checkNotNull(resource) {
            "huffman.dat could not be found"
        }
        return HuffmanCodec.create(Unpooled.wrappedBuffer(resource.readBytes()))
    }
}
