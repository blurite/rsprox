package net.rsprox.proxy.client

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprox.proxy.binary.StreamDirection
import net.rsprox.proxy.channel.getBinaryBlob

public class ClientGameHandler(
    private val serverChannel: Channel,
) : SimpleChannelInboundHandler<WrappedIncomingMessage<GameClientProt>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: WrappedIncomingMessage<GameClientProt>,
    ) {
        ctx.channel().getBinaryBlob().append(StreamDirection.ClientToServer, msg.encode(ctx.alloc()))
        serverChannel.writeAndFlush(msg.encode(ctx.alloc()))
    }
}
