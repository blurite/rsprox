package net.rsprox.proxy.rs3.replay

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.rsprox.cache.rs3.Rs3ReplayGroupStore
import java.util.ArrayDeque

/** Ten-byte requests; five-byte response headers repeated at 100 KiB block boundaries. */
internal class Rs3ReplayJs5Handler(
    private val store: Rs3ReplayGroupStore,
) : ByteToMessageDecoder() {
    private data class Request(
        val archive: Int,
        val group: Int,
        val urgent: Boolean,
    )

    private data class Response(
        val request: Request,
        val bytes: ByteArray,
    )

    private val urgent = ArrayDeque<Request>()
    private val pending = HashSet<Pair<Int, Int>>()
    private val ready = ArrayDeque<Response>()
    private var inFlight = 0
    private var writing = false
    private var key = 0
    private var resumeWrite: (() -> Unit)? = null

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        while (input.isReadable(10)) {
            val type = input.readUnsignedByte().toInt()
            val record = input.readSlice(9)
            when (type) {
                // Native uses 32/33 (and 17); the definition downloader uses 0/1.
                0, 1, 17, 32, 33 -> {
                    val archive = record.readUnsignedByte().toInt()
                    val group = record.readInt()
                    require(group >= 0) { "Invalid RS3 JS5 group" }
                    // Do not service background cache-completion requests. A required group can
                    // still be requested urgently; it is never entered in pending by this branch.
                    if (type == 0 || type == 32) continue
                    if (pending.add(archive to group)) {
                        // NXT queues its interface/script startup set in one burst. Only request
                        // identifiers are retained here; downloaded containers are bounded below.
                        require(pending.size <= 8192) { "Too many pending RS3 cache requests" }
                        val request = Request(archive, group, true)
                        urgent.addLast(request)
                    }
                }
                4 -> key = record.readUnsignedByte().toInt()
                2, 3, 6, 7 -> Unit
                else -> error("Unsupported RS3 JS5 request type $type")
            }
        }
        drain(ctx)
    }

    private fun drain(ctx: ChannelHandlerContext) {
        if (!ctx.channel().isActive || !ctx.channel().isWritable) return
        while (inFlight < 4) {
            val request = urgent.pollFirst() ?: break
            inFlight++
            store.get(request.archive, request.group).whenComplete { bytes, error ->
                ctx.executor().execute {
                    if (!ctx.channel().isActive) return@execute
                    if (error != null) {
                        ctx.fireExceptionCaught(
                            IllegalStateException(
                                "Replay cache unavailable: ${request.archive}:${request.group}",
                                error,
                            ),
                        )
                        ctx.close()
                    } else {
                        ready.addLast(Response(request, bytes))
                        drain(ctx)
                    }
                }
            }
        }
        if (!writing) {
            val response = ready.pollFirst() ?: return
            writing = true
            writeBlock(ctx, response.request, response.bytes, 0)
        }
    }

    private fun writeBlock(
        ctx: ChannelHandlerContext,
        request: Request,
        bytes: ByteArray,
        offset: Int,
    ) {
        if (!ctx.channel().isActive) return
        if (!ctx.channel().isWritable) {
            resumeWrite = { writeBlock(ctx, request, bytes, offset) }
            return
        }
        val count = minOf(bytes.size - offset, BLOCK_SIZE)
        val block = ctx.alloc().buffer(5 + count)
        block.writeByte(request.archive)
        block.writeInt(request.group or (if (request.urgent) 0 else Int.MIN_VALUE))
        block.writeBytes(bytes, offset, count)
        if (key != 0) {
            for (index in 0 until block.writerIndex()) {
                block.setByte(
                    index,
                    block.getUnsignedByte(index).toInt() xor key,
                )
            }
        }
        ctx.writeAndFlush(block).addListener { sent ->
            if (!sent.isSuccess) {
                ctx.fireExceptionCaught(sent.cause())
                ctx.close()
            } else if (offset + count < bytes.size) {
                ctx.executor().execute { writeBlock(ctx, request, bytes, offset + count) }
            } else {
                pending.remove(request.archive to request.group)
                inFlight--
                writing = false
                drain(ctx)
            }
        }
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        val resume = resumeWrite
        if (ctx.channel().isWritable && resume != null) {
            resumeWrite = null
            resume()
        } else {
            drain(ctx)
        }
        ctx.fireChannelWritabilityChanged()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        urgent.clear()
        ready.clear()
        pending.clear()
        resumeWrite = null
        super.channelInactive(ctx)
    }

    private companion object {
        const val BLOCK_SIZE = 100 * 1024 - 5
    }
}
