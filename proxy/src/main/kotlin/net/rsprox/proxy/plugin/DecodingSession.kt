package net.rsprox.proxy.plugin

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.protocol.session.AttributeMap
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.binary.BinaryStream
import net.rsprox.shared.StreamDirection

public class DecodingSession(
    private val blob: BinaryBlob,
    private val plugin: DecoderPlugin,
) {
    public fun sequence(): Sequence<DirectionalPacket> {
        val stream =
            blob.stream.toBinaryPacketSequence(
                blob.header,
                plugin.gameClientProtProvider,
                plugin.gameServerProtProvider,
            )
        val session = Session(blob.header.localPlayerIndex, AttributeMap())
        return stream.mapNotNull { binaryPacket ->
            try {
                val packet =
                    when (binaryPacket.direction) {
                        StreamDirection.CLIENT_TO_SERVER -> {
                            plugin.decodeClientPacket(
                                binaryPacket.prot.opcode,
                                binaryPacket.payload.toJagByteBuf(),
                                session,
                            )
                        }
                        StreamDirection.SERVER_TO_CLIENT -> {
                            plugin.decodeServerPacket(
                                binaryPacket.prot.opcode,
                                binaryPacket.payload.toJagByteBuf(),
                                session,
                            )
                        }
                    }
                DirectionalPacket(
                    binaryPacket.direction,
                    binaryPacket.prot,
                    packet,
                )
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error decoding packet: ${binaryPacket.prot}"
                }
                null
            }
        }
    }

    public fun decodePacket(
        direction: StreamDirection,
        buffer: ByteBuf,
        session: Session,
    ): DirectionalPacket {
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
        return DirectionalPacket(
            direction,
            prot,
            packet,
        )
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
