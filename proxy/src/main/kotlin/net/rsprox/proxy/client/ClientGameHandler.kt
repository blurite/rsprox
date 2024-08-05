package net.rsprox.proxy.client

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprot.buffer.extensions.p4
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.proxy.attributes.INCOMING_BANK_PIN
import net.rsprox.proxy.channel.getBinaryBlob
import net.rsprox.shared.StreamDirection

public class ClientGameHandler(
    private val serverChannel: Channel,
) : SimpleChannelInboundHandler<ClientPacket<GameClientProt>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<GameClientProt>,
    ) {
        try {
            serverChannel.writeAndFlush(msg.encode(ctx.alloc()))
            eraseSensitiveContents(ctx, msg)
            ctx.channel().getBinaryBlob().append(StreamDirection.CLIENT_TO_SERVER, msg.encode(ctx.alloc(), mod = false))
        } finally {
            msg.payload.release()
        }
    }

    private fun eraseSensitiveContents(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<GameClientProt>,
    ) {
        // Nothing to erase unless the bank pin interface is currently open
        if (ctx.channel().attr(INCOMING_BANK_PIN).get() != true) {
            return
        }
        when (msg.prot) {
            GameClientProt.RESUME_P_COUNTDIALOG -> {
                ctx.channel().attr(INCOMING_BANK_PIN).set(null)
                val replacement = ctx.alloc().buffer(4)
                // Replace the real bank pin with a value of zero
                replacement.p4(0)
                msg.replacePayload(replacement)
            }
            GameClientProt.EVENT_KEYBOARD -> {
                val buffer = msg.payload.toJagByteBuf()
                val count = buffer.readableBytes() / 4
                val replacement =
                    Unpooled
                        .buffer(count * 4)
                        .toJagByteBuf()
                for (i in 0..<count) {
                    // Note(revision): These buffer methods change
                    buffer.g1Alt3() // Key
                    val delta = buffer.g3()

                    // Erase any keypresses while the bank pin interface is open
                    // This is because a lot of people use the keyboard bank pin plugin,
                    // and this would allow the bank pins to be easily identified
                    replacement.p1Alt3(0)
                    replacement.p3(delta)
                }

                msg.replacePayload(replacement.buffer)
            }
            else -> {
                // No-op
            }
        }
    }
}
