package net.rsprox.proxy.client

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.io.IOException

public class ClientRelayHandler(
    private val serverChannel: Channel,
) : ChannelInboundHandlerAdapter() {
    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any,
    ) {
        serverChannel.writeAndFlush(msg)
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        // Ignore IOExceptions as those tend to spam whenever something disconnects
        // Those exceptions are not very useful for us, but errors in our handling are.
        if (cause is IOException) {
            return
        }
        logger.error(cause) {
            "Exception in netty channel $ctx"
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
