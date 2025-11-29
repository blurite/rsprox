package net.rsprox.proxy.client

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprot.buffer.extensions.p4
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.proxy.attributes.INCOMING_BANK_PIN
import net.rsprox.proxy.channel.getBinaryBlob
import net.rsprox.shared.StreamDirection

public class ClientGameHandler(
    private val serverChannel: Channel,
) : SimpleChannelInboundHandler<ClientPacket<*>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<*>,
    ) {
        try {
            serverChannel.writeAndFlush(msg.encode(ctx.alloc()))
            val blob = ctx.channel().getBinaryBlob()
            eraseSensitiveContents(ctx, msg, blob.header.revision)
            blob.append(
                StreamDirection.CLIENT_TO_SERVER,
                msg.encode(ctx.alloc(), mod = false),
                serverChannel,
            )
        } finally {
            msg.payload.release()
        }
    }

    private fun eraseSensitiveContents(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<*>,
        revision: Int,
    ) {
        when (msg.prot.toString()) {
            "RESUME_P_COUNTDIALOG" -> {
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
            "EVENT_KEYBOARD" -> {
                val buffer = msg.payload.toJagByteBuf()
                val count = buffer.readableBytes() / 4
                val replacement =
                    Unpooled
                        .buffer(count * 4)
                        .toJagByteBuf()
                for (i in 0..<count) {
                    // Note(revision): These buffer methods change

                    // Erase any keypresses in general
                    // There is no value in keeping key presses in the logs, at best it helps bot developers,
                    // at worst it leaks sensitive information like private message contents,
                    // which could be re-assembled by going through keyboard events.

                    when (revision) {
                        223 -> {
                            buffer.g1Alt3() // Key
                            val delta = buffer.g3()
                            replacement.p1Alt3(0)
                            replacement.p3(delta)
                        }
                        224 -> {
                            val delta = buffer.g3()
                            buffer.g1Alt1() // Key
                            replacement.p3(delta)
                            replacement.p1Alt1(0)
                        }
                        225 -> {
                            buffer.g1Alt1() // Key
                            val delta = buffer.g3Alt2()
                            replacement.p1Alt1(0)
                            replacement.p3Alt2(delta)
                        }
                        226 -> {
                            val delta = buffer.g3Alt2()
                            buffer.g1Alt1() // Key
                            replacement.p3Alt2(delta)
                            replacement.p1Alt1(0)
                        }
                        227 -> {
                            buffer.g1() // Key
                            val delta = buffer.g3()
                            replacement.p1(0)
                            replacement.p3(delta)
                        }
                        228 -> {
                            buffer.g1Alt2() // Key
                            val delta = buffer.g3Alt2()
                            replacement.p1Alt2(0)
                            replacement.p3Alt2(delta)
                        }
                        229 -> {
                            val delta = buffer.g3()
                            buffer.g1() // Key
                            replacement.p3(delta)
                            replacement.p1(0)
                        }
                        230 -> {
                            val delta = buffer.g3Alt2()
                            buffer.g1() // Key
                            replacement.p3Alt2(delta)
                            replacement.p1(0)
                        }
                        231 -> {
                            buffer.g1Alt1() // Key
                            val delta = buffer.g3Alt2()
                            replacement.p1Alt1(0)
                            replacement.p3Alt2(delta)
                        }
                        232 -> {
                            buffer.g1() // Key
                            val delta = buffer.g3Alt3()
                            replacement.p1(0)
                            replacement.p3Alt3(delta)
                        }
                        233 -> {
                            val delta = buffer.g3Alt1()
                            buffer.g1Alt3() // Key
                            replacement.p3Alt1(delta)
                            replacement.p1Alt3(0)
                        }
                        234 -> {
                            buffer.g1Alt2() // Key
                            val delta = buffer.g3()
                            replacement.p1Alt2(0)
                            replacement.p3(delta)
                        }
                        235 -> {
                            buffer.g1Alt2() // Key
                            val delta = buffer.g3Alt3()
                            replacement.p1Alt2(0)
                            replacement.p3Alt3(delta)
                        }
                    }
                }

                msg.replacePayload(replacement.buffer)
            }
            else -> {
                // No-op
            }
        }
    }
}
