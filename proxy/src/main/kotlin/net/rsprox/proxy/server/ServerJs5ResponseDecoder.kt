package net.rsprox.proxy.server

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.DecoderException
import net.rsprox.cache.OldSchoolJs5Framing
import net.rsprox.proxy.attributes.WORLD_ATTRIBUTE
import net.rsprox.proxy.channel.getPort
import net.rsprox.proxy.channel.remove
import net.rsprox.proxy.js5.Js5MasterIndexArchive

public class ServerJs5ResponseDecoder(
    private val clientChannel: Channel,
    revision: Int,
) : ByteToMessageDecoder() {
    private val framing = OldSchoolJs5Framing.forRevision(revision)

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (!input.isReadable(framing.headerSize)) return
        val frame = input.duplicate()
        val archive = frame.readUnsignedByte().toInt()
        val group = framing.readGroup(frame)
        if (archive == 255 && group == 255) {
            val compression = frame.readUnsignedByte().toInt()
            val length = frame.readInt()
            if (length < 0) throw DecoderException("Invalid JS5 master index size: $length")
            val containerSize = Math.addExact(length, if (compression == 0) 5 else 9)
            val wireSize = framing.wireSize(containerSize)
            if (!input.isReadable(wireSize)) return
            val data = ByteArray(wireSize)
            input.getBytes(input.readerIndex(), data)
            val world =
                ctx.channel().attr(WORLD_ATTRIBUTE).get()
                    ?: throw IllegalStateException("World not assigned to JS5 connection!")
            Js5MasterIndexArchive.setJs5MasterIndex(clientChannel.getPort(), world, data)
        }
        // Forward the original wire bytes, including any part of the following response.
        out += input.readRetainedSlice(input.readableBytes())
        ctx.pipeline().remove<ServerJs5ResponseDecoder>()
        logger.debug { "Js5 response decoder detached" }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
