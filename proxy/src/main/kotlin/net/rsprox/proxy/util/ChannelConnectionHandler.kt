package net.rsprox.proxy.util

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.rsprox.proxy.attributes.BINARY_BLOB
import net.rsprox.proxy.connection.ProxyConnectionContainer
import java.io.IOException

public class ChannelConnectionHandler(
    private val targetChannel: Channel,
    private val connections: ProxyConnectionContainer,
) : ChannelInboundHandlerAdapter() {
    override fun channelInactive(ctx: ChannelHandlerContext) {
        if (targetChannel.isActive) {
            targetChannel.close()
        }
        val blob = targetChannel.attr(BINARY_BLOB).get() ?: return
        blob.close()
        connections.removeConnection(blob)
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
