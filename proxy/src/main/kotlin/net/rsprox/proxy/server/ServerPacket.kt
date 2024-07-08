package net.rsprox.proxy.server

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.Prot

@Suppress("DuplicatedCode")
public class ServerPacket<out T : Prot>(
    public val prot: T,
    private val cipherMod1: Int,
    private val cipherMod2: Int,
    private var _payload: ByteBuf,
) {
    public val payload: ByteBuf
        get() = _payload
    private var start: Int = payload.readerIndex()

    public fun copy(payload: ByteBuf): ServerPacket<T> {
        return ServerPacket(
            prot,
            cipherMod1,
            cipherMod2,
            payload,
        )
    }

    public fun encode(
        allocator: ByteBufAllocator,
        mod: Boolean = true,
    ): ByteBuf {
        // Ensure we always transfer the data from the first byte that the packet began at
        payload.readerIndex(start)
        val actualSize = payload.readableBytes()
        // Allocate a buffer that can handle payload + opcode + var-short size
        val buf = allocator.buffer(actualSize + 3).toJagByteBuf()
        if (prot.opcode < 0x80) {
            val modToUse = if (mod) this.cipherMod1 else 0
            buf.p1(prot.opcode + modToUse)
        } else {
            val mod1ToUse = if (mod) this.cipherMod1 else 0
            buf.p1((prot.opcode ushr 8 or 0x80) + mod1ToUse)
            val mod2ToUse = if (mod) this.cipherMod2 else 0
            buf.p1((prot.opcode and 0xFF) + mod2ToUse)
        }
        val constantSize = prot.size
        if (constantSize == Prot.VAR_BYTE) {
            buf.p1(actualSize)
        } else if (constantSize == Prot.VAR_SHORT) {
            buf.p2(actualSize)
        }
        buf.pdata(payload)
        return buf.buffer
    }

    public fun replacePayload(buf: ByteBuf) {
        val old = this._payload
        try {
            this._payload = buf
            this.start = buf.readerIndex()
        } finally {
            old.release()
        }
    }

    override fun toString(): String {
        return "Packet(" +
            "prot=$prot, " +
            "payload=$payload" +
            ")"
    }
}
