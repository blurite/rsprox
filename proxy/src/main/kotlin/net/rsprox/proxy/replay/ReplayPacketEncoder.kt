package net.rsprox.proxy.replay

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.Prot
import net.rsprox.proxy.server.ServerPacket

public object ReplayPacketEncoder {
    public fun encode(
        allocator: ByteBufAllocator,
        cipher: StreamCipher,
        frame: ReplayFrame,
    ): ByteBuf {
        return encode(allocator, cipher, frame.prot, frame.payload)
    }

    public fun encode(
        allocator: ByteBufAllocator,
        cipher: StreamCipher,
        prot: Prot,
        payloadBytes: ByteArray,
    ): ByteBuf {
        val cipherMod1 = cipher.nextInt()
        val cipherMod2 = if (prot.opcode >= 0x80) cipher.nextInt() else 0
        val payload = Unpooled.wrappedBuffer(payloadBytes)
        return try {
            ServerPacket(prot, cipherMod1, cipherMod2, payload).encode(allocator)
        } finally {
            payload.release()
        }
    }
}
