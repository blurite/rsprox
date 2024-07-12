package net.rsprox.proxy.plugin

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.protocol.session.AttributeMap
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.binary.StreamDirection

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
                DirectionalPacket(binaryPacket.direction, packet)
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error decoding packet: ${binaryPacket.prot}"
                }
                null
            }
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
