package net.rsprox.proxy.server

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g2
import net.rsprot.buffer.extensions.g4
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.p4
import net.rsprox.proxy.attributes.WORLD_ATTRIBUTE
import net.rsprox.proxy.channel.getPort
import net.rsprox.proxy.channel.remove
import net.rsprox.proxy.js5.Js5MasterIndexArchive

public class ServerJs5ResponseDecoder(
    private val clientChannel: Channel,
) : ByteToMessageDecoder() {
    private enum class State {
        Header,
        Payload,
    }

    private var state: State = State.Header
    private var archive: Int = -1
    private var group: Int = -1
    private var compression: Int = -1
    private var size: Int = -1

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (state == State.Header) {
            if (!input.isReadable(8)) {
                return
            }
            this.archive = input.g1()
            this.group = input.g2()
            this.compression = input.g1()
            this.size = input.g4()
            // If what we got from the client doesn't begin with the master index,
            // the client likely opened a new JS5 connection that no longer requires it
            // as it has already been obtained previously.
            // In these cases, just switch over to naive relaying.
            if (this.archive != 0xFF || this.group != 0xFF) {
                val buffer = ctx.alloc().buffer(8)
                buffer.p1(this.archive)
                buffer.p2(this.group)
                buffer.p1(this.compression)
                buffer.p4(this.size)
                out += buffer
                ctx.pipeline().remove<ServerJs5ResponseDecoder>()
                return
            }
            state = State.Payload
            logger.debug {
                "Js5 group response: archive ${this.archive}, group ${this.group}, compression ${this.compression}"
            }
        }
        if (state == State.Payload) {
            if (!input.isReadable(size)) {
                return
            }
            val buffer = ctx.alloc().buffer(8 + size)
            buffer.p1(this.archive)
            buffer.p2(this.group)
            buffer.p1(this.compression)
            buffer.p4(this.size)
            buffer.writeBytes(input.readBytes(size))
            val copy = buffer.copy()
            val array = ByteArray(buffer.readableBytes())
            try {
                buffer.readBytes(array)
            } finally {
                buffer.release()
            }
            val world =
                ctx.channel().attr(WORLD_ATTRIBUTE).get()
                    ?: throw IllegalStateException("World not assigned to JS5 connection!")
            val port = clientChannel.getPort()
            Js5MasterIndexArchive.setJs5MasterIndex(port, world, array)
            // Finally, pass the original message along to the client
            out += copy
            state = State.Header
            // Now that we have our response, we can get rid of the decoder and let the messages
            // get passed onto the client without middle-manning
            ctx.pipeline().remove<ServerJs5ResponseDecoder>()
            logger.debug { "Js5 response decoder detached" }
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
