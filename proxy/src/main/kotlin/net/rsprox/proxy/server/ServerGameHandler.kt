package net.rsprox.proxy.server

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprot.buffer.extensions.gjstr
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.p4
import net.rsprot.buffer.extensions.pdata
import net.rsprot.buffer.extensions.pjstr
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.crc.CyclicRedundancyCheck
import net.rsprot.protocol.util.CombinedId
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.proxy.attributes.INCOMING_BANK_PIN
import net.rsprox.proxy.binary.StreamDirection
import net.rsprox.proxy.channel.getBinaryBlob
import net.rsprox.proxy.channel.getServerToClientStreamCipher
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.server.prot.GameServerProt
import net.rsprox.proxy.worlds.WorldListProvider

public class ServerGameHandler(
    private val clientChannel: Channel,
    private val worldListProvider: WorldListProvider,
) : SimpleChannelInboundHandler<ServerPacket<GameServerProt>>() {
    private var bankPinComponent: CombinedId? = null

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: ServerPacket<GameServerProt>,
    ) {
        clientChannel.writeAndFlush(redirectTraffic(ctx, msg).encode(ctx.alloc()))
        eraseSensitiveContents(ctx, msg)
        ctx.channel().getBinaryBlob().append(
            StreamDirection.SERVER_TO_CLIENT,
            msg.encode(ctx.alloc(), mod = false),
        )
    }

    private fun redirectTraffic(
        ctx: ChannelHandlerContext,
        msg: ServerPacket<GameServerProt>,
    ): ServerPacket<GameServerProt> {
        if (msg.prot != GameServerProt.LOGOUT_TRANSFER) {
            return msg
        }
        val buf = msg.payload.toJagByteBuf()
        val host = buf.gjstr()
        val id = buf.g2()
        val properties = buf.g4()
        val world =
            checkNotNull(worldListProvider.get().getTargetWorld(host)) {
                "Unable to find world at host address $host, id $id, properties $properties"
            }
        val encoded = ctx.alloc().buffer()
        // Redirect the world to one of our local hosts
        encoded.pjstr(world.localHostAddress.toString())
        encoded.p2(id)
        encoded.p4(properties)
        // Make a clean copy here as we don't want to modify the logged host
        return msg.copy(encoded)
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
            GameServerProt.UPDATE_UID192 -> {
                // Erase all the UID data
                // We shouldn't keep it as this could bear some relevance when it comes to
                // account recovery, as it is another piece of information that can be
                // used to link to a specific user.
                val data = ByteArray(24)
                val crc = CyclicRedundancyCheck.computeCrc32(data)
                val buffer = Unpooled.buffer(28)
                buffer.pdata(data)
                buffer.p4(crc)
                msg.replacePayload(buffer)
            }
            GameServerProt.SITE_SETTINGS -> {
                val old = msg.payload.gjstr()
                // Similarly to update uid192, since both of them are linked, this can be followed
                // back to account recovery system.
                val replacement = Unpooled.buffer().pjstr("*".repeat(old.length))
                msg.replacePayload(replacement)
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
