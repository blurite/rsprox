package net.rsprox.proxy.rs3.binary

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.pMidiVarLen
import java.io.ByteArrayOutputStream

internal object Rs3RecordingEncoding {
    const val VERSION = 1
    const val MAX_INITIALIZATION = 16 * 1024 * 1024

    fun record(
        server: Boolean,
        opcode: Int,
        size: Int,
        payload: ByteArray,
        deltaMillis: Long,
    ): ByteArray {
        require(opcode in 0..if (server) 32767 else 255)
        require(deltaMillis in 0..0x7FFFFFFF)
        require(
            when (size) {
                -1 -> payload.size <= 255
                -2 -> payload.size <= 65535
                else -> payload.size == size
            },
        ) { "Invalid recorded packet length" }
        return bytes { buffer ->
            buffer.pMidiVarLen((deltaMillis.toInt() shl 1) or if (server) 1 else 0)
            if (server && opcode >= 128) buffer.writeShort(opcode + 32768) else buffer.writeByte(opcode)
            when (size) {
                -1 -> buffer.writeByte(payload.size)
                -2 -> buffer.writeShort(payload.size)
            }
            buffer.writeBytes(payload)
        }
    }

    fun initialization(
        world: Boolean,
        block: Int,
        payload: ByteArray,
    ): ByteArray {
        require(block in 0..65534 && payload.size <= MAX_INITIALIZATION)
        val result = ByteArrayOutputStream()
        var offset = 0
        do {
            val count = minOf(65000, payload.size - offset)
            val chunk =
                bytes {
                    it.writeByte(VERSION)
                    it.writeBoolean(world)
                    it.writeShort(block)
                    it.writeInt(payload.size)
                    it.writeInt(offset)
                    it.writeBytes(payload, offset, count)
                }
            result.write(record(true, Rs3RecordingProt.LOGIN_INITIALIZATION.opcode, -2, chunk, 0))
            offset += count
        } while (offset < payload.size)
        return result.toByteArray()
    }

    /** Remove optional encrypted auth and zero the raw identity pair; only its hash is retained in the header. */
    fun sanitizeSuccess(payload: ByteArray): ByteArray {
        require(payload.size >= 25) { "Truncated RS3 login-success body" }
        val optional = if (payload[0] == 1.toByte()) 4 else 0
        require(payload.size >= 25 + optional)
        val result = byteArrayOf(0) + payload.copyOfRange(1 + optional, payload.size)
        result.fill(0, result.size - 16, result.size)
        return result
    }

    fun bytes(write: (ByteBuf) -> Unit): ByteArray {
        val buffer = Unpooled.buffer()
        return try {
            write(buffer)
            ByteArray(buffer.readableBytes()).also { buffer.readBytes(it) }
        } finally {
            buffer.release()
        }
    }
}
