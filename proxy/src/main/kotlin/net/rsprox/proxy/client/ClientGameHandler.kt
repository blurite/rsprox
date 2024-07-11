package net.rsprox.proxy.client

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprot.buffer.extensions.p4
import net.rsprox.proxy.attributes.INCOMING_BANK_PIN
import net.rsprox.proxy.binary.StreamDirection
import net.rsprox.proxy.channel.getBinaryBlob
import net.rsprox.proxy.client.prot.GameClientProt

public class ClientGameHandler(
    private val serverChannel: Channel,
) : SimpleChannelInboundHandler<ClientPacket<GameClientProt>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<GameClientProt>,
    ) {
        serverChannel.writeAndFlush(msg.encode(ctx.alloc()))
        eraseSensitiveContents(ctx, msg)
        ctx.channel().getBinaryBlob().append(StreamDirection.CLIENT_TO_SERVER, msg.encode(ctx.alloc(), mod = false))
    }

    private fun eraseSensitiveContents(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<GameClientProt>,
    ) {
        if (msg.prot == GameClientProt.RESUME_P_COUNTDIALOG) {
            val bankPin = ctx.channel().attr(INCOMING_BANK_PIN).get() ?: false
            if (bankPin) {
                ctx.channel().attr(INCOMING_BANK_PIN).set(null)
                val replacement = ctx.alloc().buffer(4)
                // Replace the real bank pin with a value of zero
                replacement.p4(0)
                msg.replacePayload(replacement)
            }
        }
    }
}
