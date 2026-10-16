package net.rsprox.proxy.rs3.replay

import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.binary.BinaryStream
import net.rsprox.proxy.binary.isRuneScape3
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.replay.ReplayFrame
import net.rsprox.proxy.replay.ReplayTimeline
import net.rsprox.proxy.rs3.Rs3DecoderLoader
import net.rsprox.proxy.rs3.Rs3RevisionDecoder
import net.rsprox.proxy.rs3.binary.Rs3RecordingInitialization
import net.rsprox.proxy.rs3.binary.Rs3RecordingProt
import net.rsprox.shared.StreamDirection
import java.io.ByteArrayOutputStream

/** Splits an existing recording into connection phases; initialization records never reach the wire. */
public class Rs3ReplayRecording private constructor(
    public val header: BinaryHeader,
    public val decoder: Rs3RevisionDecoder,
    public val lobbySuccess: ByteArray,
    public val gameSuccess: ByteArray,
    public val variableBlocks: List<ByteArray>,
    public val lobby: ReplayTimeline,
    public val game: ReplayTimeline,
) {
    // Lobby UI requests are interactive, so keep a complete definitions reply to serve on demand.
    // Population-only replies cannot initialize a fresh client world list.
    internal val lobbyWorldList: List<ReplayFrame> =
        run {
            val chunks = ArrayList<ReplayFrame>()
            val data = ByteArrayOutputStream()
            var complete = emptyList<ReplayFrame>()
            for (frame in lobby) {
                if (frame.direction != StreamDirection.SERVER_TO_CLIENT ||
                    frame.prot.toString() != "WORLDLIST_FETCH_REPLY"
                ) {
                    continue
                }
                require(frame.payload.isNotEmpty()) { "Empty recorded world-list chunk" }
                chunks += frame
                data.write(frame.payload, 1, frame.payload.size - 1)
                require(data.size() <= 20_000) { "Recorded world list exceeds native accumulator" }
                if (frame.payload[0] == 1.toByte()) {
                    val payload = data.toByteArray()
                    if (payload.size >= 2 && payload[0] == 2.toByte() && payload[1] == 1.toByte()) {
                        complete = chunks.toList()
                    }
                    chunks.clear()
                    data.reset()
                }
            }
            complete
        }

    public companion object {
        public fun read(binary: BinaryBlob): Rs3ReplayRecording = read(binary.header, binary.stream)

        public fun read(
            header: BinaryHeader,
            source: BinaryStream,
        ): Rs3ReplayRecording {
            require(header.isRuneScape3()) { "Not an RS3 recording" }
            HuffmanProvider.load()
            val decoder = Rs3DecoderLoader.load(header.revision, HuffmanProvider.get()) { NopStreamCipher }
            var lobbySuccess: ByteArray? = null
            var gameSuccess: ByteArray? = null
            val variables = ArrayList<ByteArray>()
            val initialization =
                Rs3RecordingInitialization(header) { world, block, bytes ->
                    when {
                        !world -> lobbySuccess = bytes
                        block == 0 -> gameSuccess = bytes
                        else -> variables += bytes
                    }
                }
            val lobby = ArrayList<ReplayFrame>()
            val game = ArrayList<ReplayFrame>()
            var tick = 0
            var previousTime: Long? = null
            val data = source.copy()
            try {
                val stream = BinaryStream(data)
                for (packet in stream.toBinaryPacketSequence(
                    header,
                    decoder.gameClientProtProvider,
                    Rs3RecordingProt.provider(decoder.gameServerProtProvider),
                )) {
                    when (packet.prot) {
                        Rs3RecordingProt.LOGIN_INITIALIZATION -> {
                            initialization.initialization(packet.payload)
                            continue
                        }
                        Rs3RecordingProt.LOBBY_TRANSFER -> {
                            initialization.transfer(packet.payload, packet.epochTimeMillis)
                            tick = 0
                            previousTime = packet.epochTimeMillis
                            continue
                        }
                        else -> Unit
                    }
                    initialization.packet()
                    val frames = if (initialization.gameReady) game else lobby
                    val payload = ByteArray(packet.size)
                    packet.payload.getBytes(packet.payload.readerIndex(), payload)
                    frames +=
                        ReplayFrame(
                            frames.size,
                            tick,
                            previousTime?.let { (packet.epochTimeMillis - it).coerceAtLeast(0) } ?: 0,
                            packet.epochTimeMillis,
                            packet.direction,
                            packet.prot,
                            payload,
                        )
                    previousTime = packet.epochTimeMillis
                    if (packet.direction == StreamDirection.SERVER_TO_CLIENT &&
                        packet.prot.toString() == "SERVER_TICK_END"
                    ) {
                        tick++
                    }
                }
                initialization.finish()
            } finally {
                data.release()
            }
            require(game.isNotEmpty()) { "Recording has no game packets" }
            return Rs3ReplayRecording(
                header,
                decoder,
                checkNotNull(lobbySuccess),
                checkNotNull(gameSuccess),
                variables,
                ReplayTimeline(-1, header, lobby),
                ReplayTimeline(-1, header, game),
            )
        }
    }
}
