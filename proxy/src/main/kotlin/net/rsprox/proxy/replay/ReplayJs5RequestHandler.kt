package net.rsprox.proxy.replay

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.DecoderException
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.p1
import net.rsprox.cache.OldSchoolJs5Framing
import net.rsprox.cache.store.GroupStore
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

public class ReplayJs5RequestHandler(
    revision: Int,
    private val masterIndex: ByteArray,
    private val cacheStore: GroupStore,
) : ByteToMessageDecoder() {
    private val framing = OldSchoolJs5Framing.forRevision(revision)
    private var xorKey: Int = 0

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        while (input.isReadable(4)) {
            val requestType = input.getUnsignedByte(input.readerIndex()).toInt()
            if (requestType == CONNECTED_REQUEST || requestType == DISCONNECT_REQUEST) {
                input.skipBytes(4)
                continue
            }
            if (!input.isReadable(1 + framing.prefixSize)) break
            input.skipBytes(1)
            when (requestType) {
                PREFETCH_REQUEST, URGENT_REQUEST -> {
                    val archive = input.g1()
                    val group = framing.readGroup(input)
                    val response = groupResponse(ctx, archive, group) ?: continue
                    if (xorKey != 0) {
                        for (i in response.readerIndex()..<response.writerIndex()) {
                            response.setByte(i, response.getByte(i).toInt() xor xorKey)
                        }
                    }
                    ctx.write(response)
                }
                PRIORITY_CHANGE_HIGH, PRIORITY_CHANGE_LOW -> input.skipBytes(framing.prefixSize)
                REKEY_REQUEST -> {
                    xorKey = input.g1()
                    input.skipBytes(framing.prefixSize - 1)
                }
                else -> throw DecoderException("Unknown replay JS5 request: $requestType")
            }
        }
        ctx.flush()
    }

    private fun groupResponse(
        ctx: ChannelHandlerContext,
        archive: Int,
        group: Int,
    ): ByteBuf? {
        if (archive == MASTER_INDEX_ARCHIVE && group == MASTER_INDEX_GROUP) {
            // A recorded wire response already includes its block separators.
            if (masterIndex.firstOrNull() == MASTER_INDEX_ARCHIVE.toByte()) {
                return ctx.alloc().buffer(masterIndex.size).writeBytes(masterIndex)
            }
            return encodeResponse(ctx, archive, group, masterIndex)
        }
        val data = resolveGroup(archive, group)
        if (data != null) return encodeResponse(ctx, archive, group, data)
        if (missingGroups.add(archive to group)) {
            logger.warn { "Replay JS5 group unavailable: $archive:$group" }
        }
        return null
    }

    private fun resolveGroup(
        archive: Int,
        group: Int,
    ): ByteArray? {
        val resolved = cacheStore.get(archive, group) ?: return null
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
        archive: Int,
        group: Int,
        groupData: ByteArray,
    ): ByteBuf {
        val dataLength = groupData.size
        val outputLength = framing.wireSize(dataLength)
        val output = ctx.alloc().buffer(outputLength, outputLength)
        output.p1(archive)
        framing.writeGroup(output, group)
        var offset = 0
        while (offset < dataLength) {
            if (offset != 0) output.p1(BLOCK_SEPARATOR)
            val count = min(dataLength - offset, framing.remainingInBlock(offset))
            output.writeBytes(groupData, offset, count)
            offset += count
        }
        return output
    }

    private companion object {
        private const val URGENT_REQUEST: Int = 1
        private const val PREFETCH_REQUEST: Int = 0
        private const val PRIORITY_CHANGE_HIGH: Int = 2
        private const val PRIORITY_CHANGE_LOW: Int = 3
        private const val REKEY_REQUEST: Int = 4
        private const val CONNECTED_REQUEST: Int = 6
        private const val DISCONNECT_REQUEST: Int = 7
        private const val MASTER_INDEX_ARCHIVE: Int = 0xFF
        private const val MASTER_INDEX_GROUP: Int = 0xFF
        private const val BLOCK_SEPARATOR: Int = 0xFF
        private val logger = InlineLogger()
        private val missingGroups = ConcurrentHashMap.newKeySet<Pair<Int, Int>>()
    }
}
