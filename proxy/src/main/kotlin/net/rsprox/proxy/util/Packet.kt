package net.rsprox.proxy.util

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.Prot

public class Packet<out T : Prot>(
    public val prot: T,
    private val cipherMod: Int,
    private var _payload: ByteBuf,
) {
    public val payload: ByteBuf
        get() = _payload
    private var _start: Int = payload.readerIndex()
    public val start: Int
        get() = _start

    public fun encode(
        allocator: ByteBufAllocator,
        mod: Boolean = true,
    ): ByteBuf {
        // Ensure we always transfer the data from the first byte that the packet began at
        payload.readerIndex(start)
        val actualSize = payload.readableBytes()
        // Allocate a buffer that can handle payload + opcode + var-short size
        val buf = allocator.buffer(actualSize + 3).toJagByteBuf()
        val modToUse = if (mod) this.cipherMod else 0
        buf.p1((prot.opcode + modToUse) and 0xff)
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
            this._start = buf.readerIndex()
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
