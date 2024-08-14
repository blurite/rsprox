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
        when (msg.prot) {
            GameClientProt.RESUME_P_COUNTDIALOG -> {
                // Nothing to erase unless the bank pin interface is currently open
                if (ctx.channel().attr(INCOMING_BANK_PIN).get() != true) {
                    return
                }
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
                    val delta = buffer.g3()
                    buffer.g1Alt1() // Key

                    // Erase any keypresses in general
                    // There is no value in keeping key presses in the logs, at best it helps bot developers,
                    // at worst it leaks sensitive information like private message contents,
                    // which could be re-assembled by going through keyboard events.
                    replacement.p3(delta)
                    replacement.p1Alt1(0)
                }

                msg.replacePayload(replacement.buffer)
            }
            else -> {
                // No-op
            }
        }
    }
}
