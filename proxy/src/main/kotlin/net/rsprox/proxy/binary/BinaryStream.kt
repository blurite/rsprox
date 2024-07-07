package net.rsprox.proxy.binary

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.pVarInt
import net.rsprot.buffer.extensions.pdata
import kotlin.math.max
import kotlin.math.min

public class BinaryStream(
    private val buffer: ByteBuf,
    private var nanoTime: Long,
) {
    @Synchronized
    public fun append(
        direction: StreamDirection,
        packet: ByteBuf,
    ) {
        val directionOpcode = if (direction == StreamDirection.ServerToClient) 1 else 0
        val previousPacketNanoTime = this.nanoTime
        val currentPacketNanoTime = System.nanoTime()
        this.nanoTime = currentPacketNanoTime
        val nanoDelta = max(0, currentPacketNanoTime - previousPacketNanoTime)
        val millisecondDelta = nanoDelta / NANOSECONDS_IN_MILLISECOND
        if (millisecondDelta > MAX_31BIT_INT) {
            logger.warn { "Packet delta exceeds max 31bit int value milliseconds: $millisecondDelta" }
        }
        val delta = min(MAX_31BIT_INT, millisecondDelta).toInt()
        val bitpacked = directionOpcode or (delta shl 1)
        this.buffer.pVarInt(bitpacked)
        this.buffer.pdata(packet)
    }

    private companion object {
        private const val MAX_31BIT_INT: Long = 1 shl 30
        private const val NANOSECONDS_IN_MILLISECOND: Long = 1_000_000
        private val logger = InlineLogger()
    }
}
