package net.rsprox.proxy.binary

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g2
import net.rsprot.buffer.extensions.gVarInt
import net.rsprot.buffer.extensions.pVarInt
import net.rsprot.buffer.extensions.pdata
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot
import net.rsprox.protocol.ProtProvider
import net.rsprox.shared.StreamDirection
import kotlin.math.max
import kotlin.math.min

public class BinaryStream(
    private val buffer: ByteBuf,
    private var nanoTime: Long = 0,
) {
    public fun isEmpty(): Boolean {
        return !buffer.isReadable
    }

    @Synchronized
    public fun append(
        direction: StreamDirection,
        packet: ByteBuf,
    ) {
        val directionOpcode = if (direction == StreamDirection.SERVER_TO_CLIENT) 1 else 0
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
        try {
            this.buffer.pdata(packet)
        } finally {
            packet.release()
        }
    }

    @Synchronized
    public fun copy(): ByteBuf {
        return buffer.copy()
    }

    public fun toBinaryPacketSequence(
        header: BinaryHeader,
        clientProtProvider: ProtProvider<ClientProt>,
        serverProtProvider: ProtProvider<ClientProt>,
    ): Sequence<BinaryPacket> {
        var timeMillis = header.timestamp
        return sequence {
            while (buffer.isReadable) {
                val packed = buffer.gVarInt()
                val direction =
                    if (packed and 0x1 == 1) {
                        StreamDirection.SERVER_TO_CLIENT
                    } else {
                        StreamDirection.CLIENT_TO_SERVER
                    }
                val timeDelta = packed ushr 1
                timeMillis += timeDelta
                val opcode = decodeOpcode(buffer, direction)
                val provider =
                    if (direction == StreamDirection.CLIENT_TO_SERVER) {
                        clientProtProvider
                    } else {
                        serverProtProvider
                    }
                val prot = provider[opcode]
                val size = decodeSize(buffer, prot)
                val payload = buffer.readSlice(size)
                yield(
                    BinaryPacket(
                        timeMillis,
                        direction,
                        prot,
                        size,
                        payload,
                    ),
                )
            }
        }
    }

    public companion object {
        private const val MAX_31BIT_INT: Long = 1 shl 30
        private const val NANOSECONDS_IN_MILLISECOND: Long = 1_000_000
        private val logger = InlineLogger()

        public fun decodeOpcode(
            buffer: ByteBuf,
            direction: StreamDirection,
        ): Int {
            return if (direction == StreamDirection.CLIENT_TO_SERVER) {
                buffer.g1()
            } else {
                val p1 = buffer.g1()
                if (p1 >= 128) {
                    val p2 = buffer.g1()
                    ((p1 - 128) shl 8) + p2
                } else {
                    p1
                }
            }
        }

        public fun decodeSize(
            buffer: ByteBuf,
            prot: Prot,
        ): Int {
            if (prot.size == Prot.VAR_BYTE) {
                return buffer.g1()
            }
            if (prot.size == Prot.VAR_SHORT) {
                return buffer.g2()
            }
            return prot.size
        }
    }
}
