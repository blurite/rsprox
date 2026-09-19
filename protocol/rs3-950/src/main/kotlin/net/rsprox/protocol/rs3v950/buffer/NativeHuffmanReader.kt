package net.rsprox.protocol.rs3v950.buffer

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec

/** Chat bodies finish the frame. A bounded view prevents bit reads into spare buffer capacity. */
internal fun JagByteBuf.readNativeHuffman(codec: HuffmanCodec): String {
    val input = buffer.slice(buffer.readerIndex(), buffer.readableBytes())
    val result = codec.decode(input)
    buffer.readerIndex(buffer.readerIndex() + input.readerIndex())
    // U+FFFD cannot originate in CP1252: the library uses it for undefined bytes/native dropped NULs.
    return result.filter { it != '\uFFFD' }
}
