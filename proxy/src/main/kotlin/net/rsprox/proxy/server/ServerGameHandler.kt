package net.rsprox.proxy.server

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g2
import net.rsprot.buffer.extensions.g3
import net.rsprot.buffer.extensions.gjstr
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.p3
import net.rsprot.buffer.extensions.p4
import net.rsprot.buffer.extensions.pdata
import net.rsprot.buffer.extensions.pjstr
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.crc.CyclicRedundancyCheck
import net.rsprot.protocol.util.*
import net.rsprox.proxy.attributes.INCOMING_BANK_PIN
import net.rsprox.proxy.channel.getBinaryBlob
import net.rsprox.proxy.channel.getServerToClientStreamCipher
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.worlds.WorldListProvider
import net.rsprox.shared.StreamDirection

public class ServerGameHandler(
    private val clientChannel: Channel,
    private val worldListProvider: WorldListProvider,
) : SimpleChannelInboundHandler<ServerPacket<*>>() {
    private var bankPinComponent: CombinedId? = null

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: ServerPacket<*>,
    ) {
        try {
            val new = redirectTraffic(ctx, msg)
            try {
                clientChannel.writeAndFlush(new.encode(ctx.alloc()))
            } finally {
                if (new != msg) {
                    new.payload.release()
                }
            }
            val blob = ctx.channel().getBinaryBlob()
            eraseSensitiveContents(ctx, msg, blob.header.revision)
            blob.append(
                StreamDirection.SERVER_TO_CLIENT,
                msg.encode(ctx.alloc(), mod = false),
            )
        } finally {
            msg.payload.release()
        }
    }

    private fun redirectTraffic(
        ctx: ChannelHandlerContext,
        msg: ServerPacket<*>,
    ): ServerPacket<*> {
        if (msg.prot.toString() != "LOGOUT_TRANSFER") {
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
        msg: ServerPacket<*>,
        revision: Int,
    ) {
        val prot = msg.prot
        // Both the message private and message private echo follow the same consistent structure
        // of [String: name, EncodedString: contents]
        when (prot.toString()) {
            "MESSAGE_PRIVATE_ECHO" -> {
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
            "MESSAGE_PRIVATE" -> {
                val payload = msg.payload
                val readableBytes = payload.readableBytes()
                val name = payload.gjstr()
                val worldId = payload.g2()
                val worldMessageCounter = payload.g3()
                val chatCrownType = payload.g1()
                val huffman = HuffmanProvider.get()
                val contents = huffman.decode(payload)
                val replacement = ctx.alloc().buffer(readableBytes)
                replacement.pjstr(name)
                replacement.p2(worldId)
                replacement.p3(worldMessageCounter)
                replacement.p1(chatCrownType)
                // Replace the contents of private messages with asterisks of the same msg length
                huffman.encode(replacement, "*".repeat(contents.length))
                msg.replacePayload(replacement)
            }
            "URL_OPEN" -> {
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
            "IF_OPENSUB" -> {
                // Note(revision): This block changes in each revision and must be updated
                val buf = msg.payload.toJagByteBuf()
                val interfaceId: Int
                val targetComponent: CombinedId
                when (revision) {
                    223 -> {
                        buf.skipRead(1)
                        interfaceId = buf.g2Alt1()
                        targetComponent = buf.gCombinedId()
                    }
                    224 -> {
                        interfaceId = buf.g2Alt3()
                        targetComponent = buf.gCombinedId()
                        buf.skipRead(1)
                    }
                    225 -> {
                        targetComponent = buf.gCombinedIdAlt3()
                        interfaceId = buf.g2Alt2()
                        buf.skipRead(1)
                    }
                    226 -> {
                        targetComponent = buf.gCombinedIdAlt2()
                        interfaceId = buf.g2Alt2()
                        buf.skipRead(1)
                    }
                    227 -> {
                        interfaceId = buf.g2Alt2()
                        targetComponent = buf.gCombinedIdAlt2()
                        buf.skipRead(1)
                    }
                    228 -> {
                        targetComponent = buf.gCombinedIdAlt1()
                        buf.skipRead(1)
                        interfaceId = buf.g2Alt1()
                    }
                    229 -> {
                        interfaceId = buf.g2()
                        targetComponent = buf.gCombinedIdAlt1()
                        buf.skipRead(1)
                    }
                    230 -> {
                        buf.skipRead(1)
                        interfaceId = buf.g2Alt2()
                        targetComponent = buf.gCombinedIdAlt3()
                    }
                    231 -> {
                        interfaceId = buf.g2Alt2()
                        buf.skipRead(1)
                        targetComponent = buf.gCombinedIdAlt3()
                    }

                    else -> {
                        error("Unsupported revision: $revision")
                    }
                }
                if (interfaceId == BANK_PIN_INTERFACE) {
                    this.bankPinComponent = targetComponent
                    clientChannel.attr(INCOMING_BANK_PIN).set(true)
                }
            }
            "IF_CLOSESUB" -> {
                val combinedId = msg.payload.toJagByteBuf().gCombinedId()
                if (combinedId == this.bankPinComponent) {
                    this.bankPinComponent = null
                    clientChannel.attr(INCOMING_BANK_PIN).set(null)
                }
            }
            "UPDATE_UID192" -> {
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
            "SITE_SETTINGS" -> {
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
