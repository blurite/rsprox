package net.rsprox.proxy.server

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

public class ServerRelayHandler(
    private val clientChannel: Channel,
) : ChannelInboundHandlerAdapter() {
    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any,
    ) {
        clientChannel.writeAndFlush(msg)
    }
}
