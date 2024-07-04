package net.rsprox.proxy.client

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

public class ClientRelayHandler(
    private val serverChannel: Channel,
) : ChannelInboundHandlerAdapter() {
    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any,
    ) {
        serverChannel.writeAndFlush(msg)
    }
}
