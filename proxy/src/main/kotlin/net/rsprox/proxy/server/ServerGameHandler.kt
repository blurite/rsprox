package net.rsprox.proxy.server

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprot.buffer.extensions.gjstr
import net.rsprot.buffer.extensions.pjstr
import net.rsprox.proxy.binary.StreamDirection
import net.rsprox.proxy.channel.getBinaryBlob
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.server.prot.GameServerProt

public class ServerGameHandler(
    private val serverChannel: Channel,
) : SimpleChannelInboundHandler<ServerPacket<GameServerProt>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: ServerPacket<GameServerProt>,
    ) {
        serverChannel.writeAndFlush(msg.encode(ctx.alloc()))
        eraseSensitiveContents(ctx, msg)
        ctx.channel().getBinaryBlob().append(
            StreamDirection.ServerToClient,
            msg.encode(ctx.alloc(), mod = false),
        )
    }

    private fun eraseSensitiveContents(
        ctx: ChannelHandlerContext,
        msg: ServerPacket<GameServerProt>,
    ) {
        // Both the message private and message private echo follow the same consistent structure
        // of [String: name, EncodedString: contents]
        if (msg.prot == GameServerProt.MESSAGE_PRIVATE ||
            msg.prot == GameServerProt.MESSAGE_PRIVATE_ECHO
        ) {
            val payload = msg.payload
            val readableBytes = payload.readableBytes()
            val name = payload.gjstr()
            val huffman = HuffmanProvider.get()
            val contents = huffman.decode(payload)
            val replacement = ctx.alloc().buffer(readableBytes)
            replacement.pjstr(name)
            // Replace the contents of private messages with asterisks of the same msg length
            huffman.encode(replacement, "*".repeat(contents.length))
            msg.replacePayload(replacement)
        }
    }
}
