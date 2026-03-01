package net.rsprox.proxy.socks

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.socksx.SocksMessage
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus
import io.netty.handler.codec.socksx.v4.Socks4CommandType
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse
import io.netty.handler.codec.socksx.v5.Socks5AddressType
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus
import io.netty.handler.codec.socksx.v5.Socks5CommandType
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest
import net.rsprox.proxy.client.ClientLoginInitializer

/**
 * Handles SOCKS4/SOCKS5 protocol negotiation for VitaLite and other SOCKS-based clients.
 *
 * After successful SOCKS handshake, removes itself from the pipeline and allows
 * the underlying OSRS protocol (ClientLoginInitializer) to take over.
 */
public class SocksProxyHandler(
    private val loginInitializer: ClientLoginInitializer,
) : ChannelInboundHandlerAdapter() {
    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any,
    ) {
        when (msg) {
            is Socks5InitialRequest -> handleSocks5Initial(ctx, msg)
            is Socks5CommandRequest -> handleSocks5Command(ctx, msg)
            is Socks4CommandRequest -> handleSocks4Command(ctx, msg)
            else -> {
                logger.warn { "Unknown SOCKS message type: ${msg.javaClass.name}" }
                ctx.close()
            }
        }
    }

    private fun handleSocks5Initial(
        ctx: ChannelHandlerContext,
        msg: Socks5InitialRequest,
    ) {
        // SOCKS5 initial handshake - respond with NO_AUTH method
        logger.debug { "SOCKS5 initial request from ${ctx.channel().remoteAddress()}" }
        val response = DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH)
        ctx.writeAndFlush(response).addListener {
            if (it.isSuccess) {
                // After sending initial response, the client will send a command request
                // The SocksPortUnificationServerHandler should continue to decode it
                logger.debug { "SOCKS5 initial response sent successfully" }
            } else {
                logger.error { "Failed to send SOCKS5 initial response: ${it.cause()?.message}" }
                ctx.close()
            }
        }
    }

    private fun handleSocks5Command(
        ctx: ChannelHandlerContext,
        msg: Socks5CommandRequest,
    ) {
        logger.debug {
            "SOCKS5 command: ${msg.type()}, dst=${msg.dstAddr()}:${msg.dstPort()}"
        }

        if (msg.type() != Socks5CommandType.CONNECT) {
            logger.warn { "Unsupported SOCKS5 command: ${msg.type()}" }
            val response =
                DefaultSocks5CommandResponse(
                    Socks5CommandStatus.COMMAND_UNSUPPORTED,
                    Socks5AddressType.IPv4,
                    "0.0.0.0",
                    0,
                )
            ctx.writeAndFlush(response).addListener { ctx.close() }
            return
        }

        // Send success response
        val response =
            DefaultSocks5CommandResponse(
                Socks5CommandStatus.SUCCESS,
                msg.dstAddrType(),
                msg.dstAddr(),
                msg.dstPort(),
            )
        ctx.writeAndFlush(response).addListener {
            if (it.isSuccess) {
                // SOCKS handshake complete - switch to OSRS protocol
                logger.info { "SOCKS5 handshake complete for ${ctx.channel().remoteAddress()}, switching to OSRS protocol" }
                switchToOsrsProtocol(ctx)
            } else {
                logger.error { "Failed to send SOCKS5 response: ${it.cause()?.message}" }
                ctx.close()
            }
        }
    }

    private fun handleSocks4Command(
        ctx: ChannelHandlerContext,
        msg: Socks4CommandRequest,
    ) {
        logger.debug {
            "SOCKS4 command: ${msg.type()}, dst=${msg.dstAddr()}:${msg.dstPort()}"
        }

        if (msg.type() != Socks4CommandType.CONNECT) {
            logger.warn { "Unsupported SOCKS4 command: ${msg.type()}" }
            val response = DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED)
            ctx.writeAndFlush(response).addListener { ctx.close() }
            return
        }

        // Send success response
        val response =
            DefaultSocks4CommandResponse(
                Socks4CommandStatus.SUCCESS,
                msg.dstAddr(),
                msg.dstPort(),
            )
        ctx.writeAndFlush(response).addListener {
            if (it.isSuccess) {
                // SOCKS handshake complete - switch to OSRS protocol
                logger.info { "SOCKS4 handshake complete for ${ctx.channel().remoteAddress()}, switching to OSRS protocol" }
                switchToOsrsProtocol(ctx)
            } else {
                logger.error { "Failed to send SOCKS4 response: ${it.cause()?.message}" }
                ctx.close()
            }
        }
    }

    private fun switchToOsrsProtocol(ctx: ChannelHandlerContext) {
        // Remove all SOCKS handlers from the pipeline
        val pipeline = ctx.pipeline()

        // Remove SOCKS decoders/encoders
        pipeline.names().filter { it.contains("Socks", ignoreCase = true) }.forEach { name ->
            try {
                pipeline.remove(name)
                logger.debug { "Removed SOCKS handler: $name" }
            } catch (e: Exception) {
                logger.warn(e) { "Failed to remove handler: $name" }
            }
        }

        // Remove this handler
        try {
            pipeline.remove(this)
            logger.debug { "Removed SocksProxyHandler" }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to remove SocksProxyHandler" }
        }

        // Now initialize the OSRS protocol handlers
        try {
            loginInitializer.initializeOsrsProtocol(ctx.channel())
            logger.debug { "Initialized OSRS protocol handlers" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize OSRS protocol" }
            ctx.close()
        }
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        logger.error(cause) { "Exception in SOCKS proxy handler" }
        ctx.close()
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
