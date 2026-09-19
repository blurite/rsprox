package net.rsprox.proxy.rs3.relay

import io.netty.buffer.Unpooled
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprox.proxy.rs3.protocol.Rs3ProtDecoder.ProtEntry

internal data class Rs3WirePacket(
    val opcode: Int,
    val entry: ProtEntry,
    val encryptedOpcode: ByteArray,
    val payload: ByteArray,
) {
    fun encode(body: ByteArray = payload): ByteArray {
        val lengthBytes =
            when (entry.length) {
                -1 -> 1
                -2 -> 2
                else -> 0
            }
        require(body.size <= if (lengthBytes == 1) 255 else 65535) { "Packet exceeds wire length" }
        require(lengthBytes != 0 || body.size == entry.length) { "Cannot resize a fixed-length packet" }
        val result = ByteArray(encryptedOpcode.size + lengthBytes + body.size)
        encryptedOpcode.copyInto(result)
        var position = encryptedOpcode.size
        if (lengthBytes == 2) result[position++] = (body.size ushr 8).toByte()
        if (lengthBytes != 0) result[position++] = body.size.toByte()
        body.copyInto(result, position)
        return result
    }
}

/** Strict transport framing. Unlike diagnostic decoding, errors must never discard/resync bytes. */
internal class Rs3PacketStream(
    private val table: Map<Int, ProtEntry>,
    private val cipher: () -> StreamCipher,
    private val server: Boolean,
    private val payloadCipher: (ProtEntry, ByteArray) -> Unit = { _, _ -> },
) : AutoCloseable {
    private val buffer = Unpooled.buffer()
    private var first = -1
    private var opcode = -1
    private var encryptedOpcode = ByteArray(0)
    private var length = -1
    private var closed = false

    fun accept(
        bytes: ByteArray,
        consume: (Rs3WirePacket) -> Unit,
    ) {
        require(buffer.readableBytes() + bytes.size <= 2 * 1024 * 1024) { "Transport input limit exceeded" }
        buffer.writeBytes(bytes)
        while (true) {
            if (first == -1) {
                if (!buffer.isReadable) break
                val raw = buffer.readByte()
                encryptedOpcode = byteArrayOf(raw)
                first = (raw.toInt() - cipher().nextInt()) and 255
                if (!server || first < 128) opcode = first
            }
            if (opcode == -1) {
                if (!buffer.isReadable) break
                val raw = buffer.readByte()
                encryptedOpcode += raw
                opcode = ((first - 128) shl 8) or ((raw.toInt() - cipher().nextInt()) and 255)
            }
            val entry = checkNotNull(table[opcode]) { "Unknown transport opcode: $opcode" }
            if (length == -1) {
                length =
                    when (entry.length) {
                        -1 -> {
                            if (!buffer.isReadable) break
                            buffer.readUnsignedByte().toInt()
                        }
                        -2 -> {
                            if (buffer.readableBytes() < 2) break
                            buffer.readUnsignedShort()
                        }
                        else -> entry.length.also { require(it in 0..65535) { "Unsupported packet length" } }
                    }
            }
            if (buffer.readableBytes() < length) break
            val payload = ByteArray(length)
            buffer.readBytes(payload)
            payloadCipher(entry, payload)
            consume(Rs3WirePacket(opcode, entry, encryptedOpcode, payload))
            first = -1
            opcode = -1
            length = -1
        }
        buffer.discardReadBytes()
    }

    fun finishInput() {
        check(first == -1 && !buffer.isReadable) {
            "Connection closed during a packet: opcode=$opcode name=${table[opcode]?.name} " +
                "expectedLength=$length buffered=${buffer.readableBytes()}"
        }
    }

    override fun close() {
        if (closed) return
        closed = true
        buffer.release()
    }
}
