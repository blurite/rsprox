package net.rsprox.proxy.server

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprot.buffer.extensions.gjstr
import net.rsprot.buffer.extensions.pjstr
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.util.CombinedId
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.proxy.attributes.INCOMING_BANK_PIN
import net.rsprox.proxy.binary.StreamDirection
import net.rsprox.proxy.channel.getBinaryBlob
import net.rsprox.proxy.channel.getServerToClientStreamCipher
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.server.prot.GameServerProt

public class ServerGameHandler(
    private val clientChannel: Channel,
) : SimpleChannelInboundHandler<ServerPacket<GameServerProt>>() {
    private var bankPinComponent: CombinedId? = null

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: ServerPacket<GameServerProt>,
    ) {
        clientChannel.writeAndFlush(msg.encode(ctx.alloc()))
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
        val prot = msg.prot
        // Both the message private and message private echo follow the same consistent structure
        // of [String: name, EncodedString: contents]
        when (prot) {
            GameServerProt.MESSAGE_PRIVATE, GameServerProt.MESSAGE_PRIVATE_ECHO -> {
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
            GameServerProt.URL_OPEN -> {
                val array = ByteArray(msg.payload.readableBytes())
                msg.payload.readBytes(array)
                val isaac = ctx.channel().getServerToClientStreamCipher()
                for (i in array.indices) {
                    array[i] = (array[i].toInt() - isaac.nextInt()).toByte()
                }
                val buf = Unpooled.wrappedBuffer(array)
                val url = buf.gjstr()
                // Secure RuneScape URLs follow the pattern seen below:
                // https://secure.runescape.com/m=weblogin
                // /s=*/p=*/redirect.ws?rpt=1&mod=news&dest=runefest---new-date-announced
                // ^These URLs contain sensitive data, and as such, we strip anything after the
                // secure.runescape.com/ part. This ensures we still get insight about what the URL was,
                // but not any of the potential credentials that come with it.
                val safeUrl =
                    url.replace(SENSITIVE_URL_REGEX) { match ->
                        val (sensitiveSuffix) = match.destructured
                        // Take the sensitive suffix of the URL and replace it with just asterisks
                        match.value.replace(sensitiveSuffix, "*".repeat(sensitiveSuffix.length))
                    }
                val output = ctx.alloc().buffer(safeUrl.length + 1)
                output.pjstr(safeUrl)
                msg.replacePayload(output)
            }
            GameServerProt.IF_OPENSUB -> {
                // Note(revision): This block changes in each revision and must be updated
                val buf = msg.payload.toJagByteBuf()
                buf.skipRead(1)
                val interfaceId = buf.g2Alt1()
                val targetComponent = buf.gCombinedId()
                if (interfaceId == BANK_PIN_INTERFACE) {
                    this.bankPinComponent = targetComponent
                    clientChannel.attr(INCOMING_BANK_PIN).set(true)
                }
            }
            GameServerProt.IF_CLOSESUB -> {
                val combinedId = msg.payload.toJagByteBuf().gCombinedId()
                if (combinedId == this.bankPinComponent) {
                    this.bankPinComponent = null
                    clientChannel.attr(INCOMING_BANK_PIN).set(null)
                }
            }
            else -> {
                // no-op, we don't care about other packets
            }
        }
    }

    private companion object {
        private val SENSITIVE_URL_REGEX: Regex = Regex("""^https?://(?:www\.)?secure\.runescape\.com/(.*)$""")
        private const val BANK_PIN_INTERFACE: Int = 213
    }
}
