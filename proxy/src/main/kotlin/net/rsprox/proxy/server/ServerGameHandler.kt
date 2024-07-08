package net.rsprox.proxy.server

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprox.proxy.binary.StreamDirection
import net.rsprox.proxy.channel.getBinaryBlob
import net.rsprox.proxy.server.prot.GameServerProt
import net.rsprox.proxy.util.Packet

public class ServerGameHandler(
    private val serverChannel: Channel,
) : SimpleChannelInboundHandler<Packet<GameServerProt>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: Packet<GameServerProt>,
    ) {
        ctx.channel().getBinaryBlob().append(StreamDirection.ServerToClient, msg.encode(ctx.alloc(), mod = false))
        serverChannel.writeAndFlush(msg.encode(ctx.alloc()))
    }
}
