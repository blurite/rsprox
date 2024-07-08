package net.rsprox.proxy.client

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.pjstr
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.IsaacRandom
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprot.crypto.rsa.decipherRsa
import net.rsprox.proxy.attributes.STREAM_CIPHER_PAIR
import net.rsprox.proxy.channel.addLastWithName
import net.rsprox.proxy.channel.getBinaryHeaderBuilder
import net.rsprox.proxy.channel.getClientToServerStreamCipher
import net.rsprox.proxy.channel.getWorld
import net.rsprox.proxy.channel.remove
import net.rsprox.proxy.channel.replace
import net.rsprox.proxy.js5.Js5MasterIndexArchive
import net.rsprox.proxy.rsa.Rsa
import net.rsprox.proxy.rsa.rsa
import net.rsprox.proxy.server.LoginServerProtId
import net.rsprox.proxy.server.ServerGameLoginDecoder
import net.rsprox.proxy.server.ServerJs5LoginHandler
import net.rsprox.proxy.server.ServerLoginDecoder
import net.rsprox.proxy.server.ServerRelayHandler
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.math.BigInteger

public class ClientLoginHandler(
    private val serverChannel: Channel,
    private val rsa: RSAPrivateCrtKeyParameters,
    private val originalModulus: BigInteger,
) : SimpleChannelInboundHandler<WrappedIncomingMessage<LoginClientProt>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: WrappedIncomingMessage<LoginClientProt>,
    ) {
        when (msg.prot) {
            LoginClientProt.INIT_GAME_CONNECTION -> {
                logger.debug {
                    "Init game connection"
                }
                switchServerToGameLoginDecoding(ctx)
            }
            LoginClientProt.INIT_JS5REMOTE_CONNECTION -> {
                logger.debug {
                    "Init JS5 remote connection"
                }
                switchClientToRelay(ctx)
                switchServerToJs5LoginDecoding(ctx)
            }
            LoginClientProt.GAMELOGIN -> {
                logger.debug {
                    "Game login received, re-encrypting RSA"
                }
                handleLogin(ctx, msg)
            }
            LoginClientProt.GAMERECONNECT -> {
                logger.debug {
                    "Game reconnect received, re-encrypting RSA"
                }
                handleLogin(ctx, msg)
            }
            LoginClientProt.POW_REPLY -> {
                logger.debug {
                    "Proof of Work reply received"
                }
            }
            LoginClientProt.UNKNOWN -> {
                logger.debug {
                    "Unknown login prot received"
                }
            }
            LoginClientProt.REMAINING_BETA_ARCHIVE_CRCS -> {
                logger.debug {
                    "Remaining beta archive CRCs received"
                }
            }
            LoginClientProt.SSL_WEB_CONNECTION -> {
                logger.debug { "SSL Web connection received, switching to relay" }
                switchClientToGameDecoding(ctx)
            }
        }
        serverChannel.writeAndFlush(msg.encode(ctx.alloc()))
    }

    private fun handleLogin(
        ctx: ChannelHandlerContext,
        msg: WrappedIncomingMessage<LoginClientProt>,
    ) {
        val builder = ctx.channel().getBinaryHeaderBuilder()
        val buffer = msg.payload.toJagByteBuf()
        val version = buffer.g4()
        val subVersion = buffer.g4()
        val clientType = buffer.g1()
        val platformType = buffer.g1()
        buffer.skipRead(1)

        builder.revision(version)
        builder.subRevision(subVersion)
        builder.clientType(clientType)
        builder.platformType(platformType)
        val masterIndex = Js5MasterIndexArchive.getJs5MasterIndex(ctx.channel().getWorld())
        if (masterIndex == null) {
            // If we can't find a JS5 master index associated to a world,
            // the proxy was likely restarted while the client was kept at login screen.
            // In order to get around it, we just tell the client that the server requested a reload
            // This just reboots the client and forces it to open a JS5 connection to the server,
            // which gets us the JS5 file that we need for our log.
            val forcedReloadBuffer = Unpooled.buffer()
            forcedReloadBuffer.p1(LoginServerProtId.OUT_OF_DATE_RELOAD)
            ctx.channel().writeAndFlush(forcedReloadBuffer)
            serverChannel.close()
            return
        }
        builder.js5MasterIndex(masterIndex)

        // The header^ will just naively be copied over
        val headerSize = 4 + 4 + 1 + 1 + 1

        val originalRsaSize = buffer.g2()
        if (!buffer.isReadable(originalRsaSize)) {
            invalidRsa(ctx)
        }
        val rsaSlice = buffer.buffer.readSlice(originalRsaSize)
        val xteaBlock = buffer.buffer.copy()
        val decryptedRsaBuffer =
            try {
                rsaSlice.rsa(rsa).toJagByteBuf()
            } catch (t: Throwable) {
                invalidRsa(ctx)
            }
        val rsaStart = decryptedRsaBuffer.readerIndex()
        val rsaCheck = decryptedRsaBuffer.g1()
        if (rsaCheck != 1) {
            invalidRsa(ctx)
        }
        val encodeSeed =
            IntArray(4) {
                decryptedRsaBuffer.g4()
            }
        val decodeSeed =
            IntArray(encodeSeed.size) {
                encodeSeed[it] + 50
            }
        // Encoding cipher is for server -> client
        val encodingCipher = IsaacRandom(encodeSeed)
        // Decoding seed is for client -> server
        val decodingCipher = IsaacRandom(decodeSeed)
        val pair = StreamCipherPair(encodingCipher, decodingCipher)
        ctx.channel().attr(STREAM_CIPHER_PAIR).set(pair)
        val encoded = ctx.alloc().buffer(msg.payload.readableBytes())
        encoded.writeBytes(msg.payload, msg.start, headerSize)
        decryptedRsaBuffer.readerIndex(rsaStart)
        val encrypted =
            decryptedRsaBuffer.buffer.decipherRsa(
                Rsa.PUBLIC_EXPONENT,
                originalModulus,
                decryptedRsaBuffer.readableBytes(),
            )
        encoded.p2(encrypted.readableBytes())
        encoded.writeBytes(encrypted)
        encoded.writeBytes(xteaBlock)
        // Swap out the original login packet with the new one
        msg.replacePayload(encoded)
        // Begin decoding game packets now.
        switchClientToGameDecoding(ctx)
    }

    private fun invalidRsa(ctx: ChannelHandlerContext): Nothing {
        // In the case of RSA failure, it implies the client was patched with a different RSA key
        // than what the proxy has loaded up. This can happen if someone deletes the cached proxy
        // key in their user.home/.rsprox directory.
        // In this case, we just drop the connection to the server and write a custom login response
        // to the client indicating what went wrong.
        serverChannel.close()
        val customResponseBuffer = Unpooled.buffer()
        customResponseBuffer.p1(LoginServerProtId.DISALLOWED_BY_SCRIPT)
        val index = customResponseBuffer.writerIndex()
        customResponseBuffer.p2(0)
        customResponseBuffer.pjstr("RSA out of date!")
        customResponseBuffer.pjstr("Re-open the client via the proxy.")
        customResponseBuffer.pjstr("Connection to the server has been killed.")
        val end = customResponseBuffer.writerIndex()
        val length = end - index - 2
        customResponseBuffer.writerIndex(index)
        customResponseBuffer.p2(length)
        customResponseBuffer.writerIndex(end)
        ctx.channel().writeAndFlush(customResponseBuffer).await()
        throw IllegalStateException("Invalid RSA")
    }

    private fun switchClientToGameDecoding(ctx: ChannelHandlerContext) {
        val cipher = ctx.channel().getClientToServerStreamCipher()
        val clientPipeline = ctx.channel().pipeline()
        clientPipeline.replace<ClientDecoder<*>>(ClientDecoder(cipher, GameClientProtProvider))
        clientPipeline.replace<ClientLoginHandler>(ClientGameHandler(serverChannel))
    }

    private fun switchClientToRelay(ctx: ChannelHandlerContext) {
        val clientPipeline = ctx.channel().pipeline()
        clientPipeline.remove<ClientDecoder<*>>()
        clientPipeline.replace<ClientLoginHandler>(ClientRelayHandler(serverChannel))
    }

    private fun switchServerToJs5LoginDecoding(ctx: ChannelHandlerContext) {
        val pipeline = serverChannel.pipeline()
        pipeline.addLastWithName(ServerLoginDecoder())
        pipeline.addLastWithName(ServerJs5LoginHandler(ctx.channel()))
    }

    private fun switchServerToGameLoginDecoding(ctx: ChannelHandlerContext) {
        val pipeline = serverChannel.pipeline()
        pipeline.addLastWithName(ServerGameLoginDecoder(ctx.channel()))
        pipeline.addLastWithName(ServerRelayHandler(ctx.channel()))
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
