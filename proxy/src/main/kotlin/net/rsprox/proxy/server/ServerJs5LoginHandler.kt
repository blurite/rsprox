package net.rsprox.proxy.server

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprox.proxy.channel.replace
import net.rsprox.proxy.server.prot.LoginServerProt
import net.rsprox.proxy.util.Packet

public class ServerJs5LoginHandler(
    private val clientChannel: Channel,
) : SimpleChannelInboundHandler<Packet<LoginServerProt>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: Packet<LoginServerProt>,
    ) {
        val response = msg.prot
        if (response != LoginServerProt.SUCCESSFUL) {
            logger.debug { "Unexpected Js5 server response: $response, closing channel" }
            clientChannel.writeAndFlush(msg.encode(ctx.alloc()))
            ctx.close()
            return
        }
        logger.debug { "Js5 login successful, switching to response decoding" }
        clientChannel.writeAndFlush(msg.encode(ctx.alloc()))
        val pipeline = ctx.channel().pipeline()
        pipeline.replace<ServerGenericDecoder<*>>(ServerJs5ResponseDecoder())
        pipeline.replace<ServerJs5LoginHandler>(ServerRelayHandler(clientChannel))
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
