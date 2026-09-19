package net.rsprox.proxy.rs3.relay

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.rsprox.proxy.rs3.login.Rs3WorldContinueAckSkipper

internal class Rs3MappedClientHandler(
    private val stream: Rs3PacketStream,
    private val acknowledgement: Rs3WorldContinueAckSkipper?,
) : ChannelInboundHandlerAdapter() {
    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any,
    ) {
        if (msg !is ByteBuf) {
            ctx.fireChannelRead(msg) // Explicit login frames are not ISAAC game packets.
            return
        }
        var bytes = ByteArray(msg.readableBytes())
        try {
            msg.readBytes(bytes)
        } finally {
            msg.release()
        }
        if (acknowledgement != null && !acknowledgement.isDone) {
            val leftover = acknowledgement.consume(bytes) ?: return
            ctx.fireChannelRead(Unpooled.wrappedBuffer(byteArrayOf(26)))
            bytes = leftover
        }
        stream.accept(bytes) { packet ->
            val body =
                if (packet.entry.name == "WORLDLIST_FETCH") {
                    require(packet.payload.size == 4) { "Unexpected world-list request format" }
                    // Ask for definitions even if this client cached real hosts before launching the proxy.
                    ByteArray(4)
                } else {
                    packet.payload
                }
            ctx.fireChannelRead(Unpooled.wrappedBuffer(packet.encode(body)))
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        stream.close()
        ctx.fireChannelInactive()
    }
}
