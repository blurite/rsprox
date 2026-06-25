package net.rsprox.proxy.replay

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot
import net.rsprox.protocol.ProtProvider
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.binary.BinaryPacket
import net.rsprox.proxy.binary.BinaryStream
import net.rsprox.shared.StreamDirection
import kotlin.math.max

public data class ReplayFrame(
    public val index: Int,
    public val tick: Int,
    public val delayMillis: Long,
    public val epochTimeMillis: Long,
    public val prot: Prot,
    public val payload: ByteArray,
)

public data class ReplayReconnectBootstrap(
    public val payload: ByteArray,
    public val consumeFrame: Boolean,
)

public data class ReplayMetadata(
    public val revision: Int,
    public val subRevision: Int,
    public val clientType: Int,
    public val platformType: Int,
    public val timestamp: Long,
    public val worldId: Int,
    public val worldHost: String,
    public val worldActivity: String,
    public val localPlayerIndex: Int,
    public val clientName: String,
)

public data class ReplayTimeline(
    public val header: BinaryHeader,
    public val frames: List<ReplayFrame>,
) : List<ReplayFrame> by frames {
    public val metadata: ReplayMetadata =
        ReplayMetadata(
            revision = header.revision,
            subRevision = header.subRevision,
            clientType = header.clientType,
            platformType = header.platformType,
            timestamp = header.timestamp,
            worldId = header.worldId,
            worldHost = header.worldHost,
            worldActivity = header.worldActivity,
            localPlayerIndex = header.localPlayerIndex,
            clientName = header.clientName,
        )

    public val totalTicks: Int = frames.maxOfOrNull { it.tick }?.plus(1) ?: 0

    public fun firstFrameIndexAtOrAfterTick(tick: Int): Int {
        if (frames.isEmpty()) return 0
        if (tick >= totalTicks) return frames.size
        val boundedTick = tick.coerceIn(0, max(0, totalTicks - 1))
        return frames.indexOfFirst { it.tick >= boundedTick }.takeIf { it >= 0 } ?: frames.size
    }

    public companion object {
        public fun fromBinaryStream(
            header: BinaryHeader,
            stream: BinaryStream,
            clientProtProvider: ProtProvider<ClientProt>,
            serverProtProvider: ProtProvider<ClientProt>,
        ): ReplayTimeline {
            return fromPackets(
                header,
                stream.toBinaryPacketSequence(header, clientProtProvider, serverProtProvider),
            )
        }

        public fun fromPackets(
            header: BinaryHeader,
            packets: Sequence<BinaryPacket>,
        ): ReplayTimeline {
            var currentTick = 0
            var previousServerEpoch: Long? = null
            var index = 0
            val frames = mutableListOf<ReplayFrame>()
            for (packet in packets) {
                if (packet.direction != StreamDirection.SERVER_TO_CLIENT) {
                    continue
                }
                val payload = packet.payload.copyBytes(packet.size)
                val delayMillis =
                    previousServerEpoch
                        ?.let { max(0, packet.epochTimeMillis - it) }
                        ?: 0
                frames +=
                    ReplayFrame(
                        index = index++,
                        tick = currentTick,
                        delayMillis = delayMillis,
                        epochTimeMillis = packet.epochTimeMillis,
                        prot = packet.prot,
                        payload = payload,
                    )
                previousServerEpoch = packet.epochTimeMillis
                if (packet.prot.isReplayServerTickEnd()) {
                    currentTick++
                }
            }
            return ReplayTimeline(header, frames)
        }

        private fun ByteBuf.copyBytes(size: Int): ByteArray {
            val bytes = ByteArray(size)
            getBytes(readerIndex(), bytes)
            return bytes
        }

    }
}

internal fun ReplayFrame.reconnectBootstrap(localPlayerIndex: Int): ReplayReconnectBootstrap? {
    val playerInfoInitBytes = reconnectPlayerInfoInitBytes(localPlayerIndex)
    if (payload.size < playerInfoInitBytes) {
        return null
    }
    if (prot.isReplayReconnect()) {
        return ReplayReconnectBootstrap(
            payload = payload.copyOf(playerInfoInitBytes),
            consumeFrame = true,
        )
    }
    if (prot.isReplayRebuildNormal() && payload.size >= playerInfoInitBytes + REBUILD_NORMAL_HEADER_BYTES) {
        return ReplayReconnectBootstrap(
            payload = payload.copyOf(playerInfoInitBytes),
            consumeFrame = false,
        )
    }
    return null
}

private fun reconnectPlayerInfoInitBytes(localPlayerIndex: Int): Int {
    val externalPlayerCount =
        (1 until PLAYER_COUNT).count {
            it != localPlayerIndex
        }
    return (LOCAL_PLAYER_ABSOLUTE_POSITION_BITS + EXTERNAL_PLAYER_POSITION_BITS * externalPlayerCount + MAX_BITS_BELOW_BYTE) ushr 3
}

private const val PLAYER_COUNT: Int = 2048
private const val LOCAL_PLAYER_ABSOLUTE_POSITION_BITS: Int = 30
private const val EXTERNAL_PLAYER_POSITION_BITS: Int = 18
private const val MAX_BITS_BELOW_BYTE: Int = Byte.SIZE_BITS - 1
private const val REBUILD_NORMAL_HEADER_BYTES: Int =
    Short.SIZE_BYTES +
        Short.SIZE_BYTES +
        Short.SIZE_BYTES
