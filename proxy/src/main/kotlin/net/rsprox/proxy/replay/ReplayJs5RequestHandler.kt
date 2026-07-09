package net.rsprox.proxy.replay

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g2
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

public class ReplayJs5RequestHandler(
    private val replaySession: ReplaySession,
) : ByteToMessageDecoder() {
    private var xorKey: Int = 0

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        while (input.isReadable(4)) {
            val requestType = input.g1()
            if (requestType == REKEY_REQUEST) {
                xorKey = input.g1()
                input.skipBytes(2)
                continue
            }
            val archive = input.g1()
            val group = input.g2()
            val prefetch =
                when (requestType) {
                    PREFETCH_REQUEST -> true
                    URGENT_REQUEST -> false
                    LOGGED_IN_REQUEST, LOGGED_OUT_REQUEST, CONNECTED_REQUEST, DISCONNECT_REQUEST -> {
                        continue
                    }
                    else -> {
                        continue
                    }
                }
            val response =
                if (archive == MASTER_INDEX_ARCHIVE && group == MASTER_INDEX_GROUP) {
                    replaySession.timeline.header.js5MasterIndex
                } else {
                    resolveGroup(archive, group)
                }
            if (response == null) {
                if (missingGroups.add(archive to group)) {
                    logger.warn { "Replay JS5 group unavailable: $archive:$group" }
                }
                continue
            }
            ctx.write(encodeResponse(ctx, prefetch, archive, group, response))
        }
        ctx.flush()
    }

    private fun resolveGroup(
        archive: Int,
        group: Int,
    ): ByteArray? {
        val resolved = replaySession.cacheStore.get(archive, group) ?: return null
        return try {
            val bytes = ByteArray(resolved.readableBytes())
            resolved.getBytes(resolved.readerIndex(), bytes)
            bytes
        } finally {
            resolved.release()
        }
    }

    private fun encodeResponse(
        ctx: ChannelHandlerContext,
        prefetch: Boolean,
        archive: Int,
        group: Int,
        groupData: ByteArray,
    ): ByteBuf {
        val dataOffset =
            if (archive == MASTER_INDEX_ARCHIVE && group == MASTER_INDEX_GROUP && hasWireHeader(groupData)) {
                MASTER_INDEX_WIRE_HEADER_SIZE
            } else {
                0
            }
        val dataLength = groupData.size - dataOffset
        val outputLength = 2 + dataLength + (512 + dataLength) / 511
        val output = ctx.alloc().buffer(outputLength, outputLength)
        output.p1(archive)
        output.p2(group)
        val compression = groupData[dataOffset].toInt() and 0xFF
        output.p1(compression)

        var bytesWritten = 1
        val firstBlockLength = min(dataLength - bytesWritten, FIRST_RESPONSE_BLOCK_SIZE)
        output.writeBytes(groupData, dataOffset + bytesWritten, firstBlockLength)
        bytesWritten += firstBlockLength
        while (bytesWritten < dataLength) {
            output.p1(BLOCK_SEPARATOR)
            val blockLength = min(dataLength - bytesWritten, RESPONSE_BLOCK_SIZE)
            output.writeBytes(groupData, dataOffset + bytesWritten, blockLength)
            bytesWritten += blockLength
        }
        if (xorKey == 0) {
            return output
        }
        for (i in output.readerIndex()..<output.writerIndex()) {
            output.setByte(i, output.getByte(i).toInt() xor xorKey)
        }
        return output
    }

    private fun hasWireHeader(groupData: ByteArray): Boolean {
        return groupData.size >= MASTER_INDEX_WIRE_HEADER_SIZE &&
            groupData[0].toInt() and 0xFF == MASTER_INDEX_ARCHIVE &&
            groupData[1].toInt() and 0xFF == 0 &&
            groupData[2].toInt() and 0xFF == MASTER_INDEX_GROUP
    }

    private companion object {
        private const val URGENT_REQUEST: Int = 1
        private const val PREFETCH_REQUEST: Int = 0
        private const val LOGGED_IN_REQUEST: Int = 2
        private const val LOGGED_OUT_REQUEST: Int = 3
        private const val REKEY_REQUEST: Int = 4
        private const val CONNECTED_REQUEST: Int = 6
        private const val DISCONNECT_REQUEST: Int = 7
        private const val MASTER_INDEX_ARCHIVE: Int = 0xFF
        private const val MASTER_INDEX_GROUP: Int = 0xFF
        private const val MASTER_INDEX_WIRE_HEADER_SIZE: Int = 3
        private const val BLOCK_SEPARATOR: Int = 0xFF
        private const val FIRST_RESPONSE_BLOCK_SIZE: Int = 508
        private const val RESPONSE_BLOCK_SIZE: Int = 511
        private val logger = InlineLogger()
        private val missingGroups = ConcurrentHashMap.newKeySet<Pair<Int, Int>>()
    }
}
