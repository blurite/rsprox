package net.rsprox.proxy.plugin

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.Prot
import net.rsprox.protocol.session.AttributeMap
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getBytesConsumed
import net.rsprox.protocol.session.getRemainingBytesInPacketGroup
import net.rsprox.protocol.session.setBytesConsumed
import net.rsprox.protocol.session.setRemainingBytesInPacketGroup
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.binary.BinaryStream
import net.rsprox.shared.StreamDirection

public class DecodingSession(
    private val blob: BinaryBlob,
    private val plugin: RevisionDecoder,
) {
    public fun sequence(): Sequence<DirectionalPacket> {
        val stream =
            blob.stream.toBinaryPacketSequence(
                blob.header,
                plugin.gameClientProtProvider,
                plugin.gameServerProtProvider,
            )
        val session = Session(blob.header.localPlayerIndex, AttributeMap())
        return stream.flatMap { binaryPacket ->
            try {
                if (binaryPacket.direction == StreamDirection.CLIENT_TO_SERVER) {
                    val packet =
                        plugin.decodeClientPacket(
                            binaryPacket.prot.opcode,
                            binaryPacket.payload.toJagByteBuf(),
                            session,
                        )
                    return@flatMap listOf(
                        DirectionalPacket(
                            binaryPacket.direction,
                            binaryPacket.prot,
                            packet,
                        ),
                    )
                }
                var read = binaryPacket.payload.readableBytes()
                val prot = binaryPacket.prot
                val opcode = prot.opcode
                if (opcode < 128) {
                    read++
                } else {
                    read += 2
                }
                if (prot.size == Prot.VAR_BYTE) {
                    read++
                } else if (prot.size == Prot.VAR_SHORT) {
                    read += 2
                }
                val remainingBytesInPacketGroup = session.getRemainingBytesInPacketGroup()
                val packet =
                    plugin.decodeServerPacket(
                        binaryPacket.prot.opcode,
                        binaryPacket.payload.toJagByteBuf(),
                        session,
                    )
                if (remainingBytesInPacketGroup != null && remainingBytesInPacketGroup > 0) {
                    session.setBytesConsumed((session.getBytesConsumed() ?: 0) + read)
                    if (remainingBytesInPacketGroup - read <= 0) {
                        session.setRemainingBytesInPacketGroup(null)
                        val outBuf = Unpooled.buffer(2).toJagByteBuf()
                        outBuf.p2(session.getBytesConsumed() ?: 0)
                        val end =
                            plugin.decodeServerPacket(
                                0xFE,
                                outBuf,
                                session,
                            )
                        return@flatMap listOf(
                            DirectionalPacket(
                                binaryPacket.direction,
                                binaryPacket.prot,
                                packet,
                            ),
                            DirectionalPacket(
                                binaryPacket.direction,
                                plugin.gameServerProtProvider[0xFE],
                                end,
                            ),
                        )
                    } else {
                        session.setRemainingBytesInPacketGroup(remainingBytesInPacketGroup - read)
                    }
                }
                listOf(
                    DirectionalPacket(
                        binaryPacket.direction,
                        binaryPacket.prot,
                        packet,
                    ),
                )
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error decoding packet: ${binaryPacket.prot}"
                }
                emptyList()
            }
        }
    }

    public fun decodePacket(
        direction: StreamDirection,
        buffer: ByteBuf,
        session: Session,
    ): List<DirectionalPacket> {
        val marker = buffer.readerIndex()
        val opcode = BinaryStream.decodeOpcode(buffer, direction)
        val provider =
            if (direction == StreamDirection.CLIENT_TO_SERVER) {
                plugin.gameClientProtProvider
            } else {
                plugin.gameServerProtProvider
            }
        val prot = provider[opcode]
        // Even though we don't use the size, we must
        val size = BinaryStream.decodeSize(buffer, prot)
        val payload = buffer.readSlice(size)
        val remainingBytesInPacketGroup = session.getRemainingBytesInPacketGroup()
        val packet =
            when (direction) {
                StreamDirection.CLIENT_TO_SERVER -> {
                    plugin.decodeClientPacket(
                        prot.opcode,
                        payload.toJagByteBuf(),
                        session,
                    )
                }
                StreamDirection.SERVER_TO_CLIENT -> {
                    plugin.decodeServerPacket(
                        prot.opcode,
                        payload.toJagByteBuf(),
                        session,
                    )
                }
            }
        if (remainingBytesInPacketGroup != null && remainingBytesInPacketGroup > 0) {
            val read = buffer.readerIndex() - marker
            session.setBytesConsumed((session.getBytesConsumed() ?: 0) + read)
            if (remainingBytesInPacketGroup - read <= 0) {
                session.setRemainingBytesInPacketGroup(null)
                val outBuf = Unpooled.buffer(2).toJagByteBuf()
                outBuf.p2(session.getBytesConsumed() ?: 0)
                session.setBytesConsumed(null)
                return listOf(
                    DirectionalPacket(
                        direction,
                        prot,
                        packet,
                    ),
                    DirectionalPacket(
                        direction,
                        plugin.gameServerProtProvider[0xFE],
                        plugin.decodeServerPacket(
                            0xFE,
                            outBuf,
                            session,
                        ),
                    ),
                )
            } else {
                session.setRemainingBytesInPacketGroup(remainingBytesInPacketGroup - read)
            }
        }
        return listOf(
            DirectionalPacket(
                direction,
                prot,
                packet,
            ),
        )
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
