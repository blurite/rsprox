package net.rsprox.proxy.socks

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus
import io.netty.handler.codec.socksx.v5.Socks5CommandType
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder
import net.rsprox.proxy.client.ClientLoginInitializer
import io.netty.channel.MultiThreadIoEventLoopGroup

/**
 * Handles SOCKS5 protocol negotiation step-by-step.
 *
 * SOCKS5 Protocol Flow:
 * 1. Client sends initial request with auth methods
 * 2. Server responds with chosen auth method (NO_AUTH for us)
 * 3. Client sends command request (CONNECT)
 * 4. Server responds with success/failure
 * 5. Connection established - switch to OSRS protocol
 */
public class Socks5ServerHandler(
    private val loginInitializer: ClientLoginInitializer,
) : ChannelInboundHandlerAdapter() {
    private var state = State.INITIAL

    private enum class State {
        INITIAL,
        AUTH,
        COMMAND,
        ESTABLISHED,
    }

    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any,
    ) {
        when (state) {
            State.INITIAL -> {
                if (msg is Socks5InitialRequest) {
                    handleInitialRequest(ctx, msg)
                } else {
                    logger.warn { "Expected Socks5InitialRequest but got ${msg.javaClass.name}" }
                    ctx.close()
                }
            }
            State.AUTH -> {
                // We use NO_AUTH, so skip directly to COMMAND
                state = State.COMMAND
                channelRead(ctx, msg)
            }
            State.COMMAND -> {
                if (msg is Socks5CommandRequest) {
                    handleCommandRequest(ctx, msg)
                } else {
                    // After SOCKS handshake, if we receive ByteBuf instead of CommandRequest,
                    // it means we're in proxy mode and decoder was already removed.
                    // Just pass it through (will be handled by relay handlers being set up)
                    logger.debug { "Received data while setting up proxy: ${msg.javaClass.name}" }
                    // Don't close - let the proxy relay handlers deal with it
                    ctx.fireChannelRead(msg)
                }
            }
            State.ESTABLISHED -> {
                // Pass through to relay handlers
                ctx.fireChannelRead(msg)
            }
        }
    }

    private fun handleInitialRequest(
        ctx: ChannelHandlerContext,
        msg: Socks5InitialRequest,
    ) {
        logger.debug { "SOCKS5 initial request from ${ctx.channel().remoteAddress()}" }

        // Respond with NO_AUTH
        val response = DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH)
        ctx.writeAndFlush(response).addListener {
            if (it.isSuccess) {
                // Remove initial decoder, add command decoder
                val pipeline = ctx.pipeline()
                pipeline.remove(Socks5InitialRequestDecoder::class.java)
                pipeline.addFirst(Socks5CommandRequestDecoder())
                state = State.COMMAND
                logger.debug { "SOCKS5 initial handshake complete, waiting for command" }
            } else {
                logger.error { "Failed to send SOCKS5 initial response" }
                ctx.close()
            }
        }
    }

    private fun handleCommandRequest(
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
                    msg.dstAddrType(),
                )
            ctx.writeAndFlush(response).addListener { ctx.close() }
            return
        }

        // Determine if this is a game server connection or external connection
        val isGameServer = isGameServerDestination(msg.dstAddr(), msg.dstPort())

        if (isGameServer) {
            // Send success response, then switch to OSRS protocol
            val response =
                DefaultSocks5CommandResponse(
                    Socks5CommandStatus.SUCCESS,
                    msg.dstAddrType(),
                )
            ctx.writeAndFlush(response).addListener {
                if (it.isSuccess) {
                    logger.info { "SOCKS5 handshake complete, switching to OSRS protocol" }
                    switchToOsrsProtocol(ctx.channel())
                } else {
                    logger.error { "Failed to send SOCKS5 command response" }
                    ctx.close()
                }
            }
        } else {
            // For proxied connections, remove SOCKS handlers first to avoid decoding SSL/TLS data
            logger.info { "SOCKS5 handshake complete, proxying to ${msg.dstAddr()}:${msg.dstPort()}" }

            // Send success response
            val response =
                DefaultSocks5CommandResponse(
                    Socks5CommandStatus.SUCCESS,
                    msg.dstAddrType(),
                )

            // Remove SOCKS decoders immediately after response, before it gets flushed
            val pipeline = ctx.pipeline()
            pipeline.remove(Socks5CommandRequestDecoder::class.java)

            // CRITICAL: Disable AUTO_READ to prevent data from arriving before relay handlers are ready
            ctx.channel().config().isAutoRead = false
            logger.debug { "Disabled AUTO_READ before proxy setup" }

            // Write response and then setup proxy
            ctx.writeAndFlush(response).addListener {
                if (it.isSuccess) {
                    proxyToDestination(ctx.channel(), msg.dstAddr(), msg.dstPort())
                } else {
                    logger.error { "Failed to send SOCKS5 command response" }
                    ctx.close()
                }
            }
        }
    }

    private fun isGameServerDestination(
        host: String,
        port: Int,
    ): Boolean {
        // Game server is typically localhost or specific game server addresses
        // For VitaLite verification, it connects to external IPs (api.ipify.org = 104.26.x.x, 172.67.x.x)
        // Game connections will be to world servers or localhost
        return host.startsWith("127.") ||
               host == "localhost" ||
               host.startsWith("oldschool") ||
               host.endsWith(".runescape.com") ||
               host.startsWith("world")
    }

    private fun proxyToDestination(
        inboundChannel: Channel,
        remoteHost: String,
        remotePort: Int,
    ) {
        // Create outbound connection to the actual destination
        val bootstrap =
            Bootstrap()
                .group(inboundChannel.eventLoop())
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(object : ChannelInboundHandlerAdapter() {
                    override fun channelActive(ctx: ChannelHandlerContext) {
                        // When connected to remote, start reading from outbound
                        logger.debug { "Outbound channel active, starting read" }
                        ctx.read()
                    }

                    override fun channelRead(
                        ctx: ChannelHandlerContext,
                        msg: Any,
                    ) {
                        // Forward data from remote to client
                        logger.debug { "Outbound received ${msg.javaClass.simpleName} from remote, forwarding to client" }
                        inboundChannel.writeAndFlush(msg).addListener {
                            if (it.isSuccess) {
                                logger.debug { "Outbound forwarded data to client successfully, reading next" }
                                ctx.read()
                            } else {
                                logger.error { "Outbound failed to forward data to client: ${it.cause()?.message}" }
                                ctx.close()
                            }
                        }
                    }

                    override fun channelInactive(ctx: ChannelHandlerContext) {
                        logger.debug { "Outbound channel inactive, closing inbound" }
                        closeOnFlush(inboundChannel)
                    }

                    override fun exceptionCaught(
                        ctx: ChannelHandlerContext,
                        cause: Throwable,
                    ) {
                        logger.error(cause) { "Exception in outbound channel" }
                        ctx.close()
                    }
                })

        val connectFuture = bootstrap.connect(remoteHost, remotePort)
        val outboundChannel = connectFuture.channel()

        connectFuture.addListener { future ->
            if (future.isSuccess) {
                logger.debug { "Connected to remote $remoteHost:$remotePort" }

                // Remove remaining SOCKS handlers from inbound channel
                // (Socks5CommandRequestDecoder already removed before sending response)
                val pipeline = inboundChannel.pipeline()
                try {
                    pipeline.remove(Socks5ServerEncoder::class.java)
                } catch (e: Exception) {
                    // May already be removed
                    logger.debug { "Socks5ServerEncoder already removed" }
                }
                try {
                    pipeline.remove(this)
                } catch (e: Exception) {
                    // May already be removed
                    logger.debug { "Socks5ServerHandler already removed" }
                }

                // Add relay handler to forward data from client to remote
                pipeline.addLast(object : ChannelInboundHandlerAdapter() {
                    override fun channelRead(
                        ctx: ChannelHandlerContext,
                        msg: Any,
                    ) {
                        // Forward data from client to remote
                        logger.debug { "Inbound received ${msg.javaClass.simpleName} from client, forwarding to remote" }
                        outboundChannel.writeAndFlush(msg).addListener {
                            if (it.isSuccess) {
                                logger.debug { "Inbound forwarded data to remote successfully, reading next" }
                                ctx.read()
                            } else {
                                logger.error { "Inbound failed to forward data to remote: ${it.cause()?.message}" }
                                ctx.close()
                            }
                        }
                    }

                    override fun channelInactive(ctx: ChannelHandlerContext) {
                        logger.debug { "Inbound channel inactive, closing outbound" }
                        closeOnFlush(outboundChannel)
                    }

                    override fun exceptionCaught(
                        ctx: ChannelHandlerContext,
                        cause: Throwable,
                    ) {
                        logger.error(cause) { "Exception in inbound relay handler" }
                        ctx.close()
                    }
                })

                logger.debug { "Relay handlers installed, starting read on both channels" }
                // Start reading from both channels
                inboundChannel.read()
                outboundChannel.read()
                state = State.ESTABLISHED
                logger.debug { "Proxy relay fully established" }
            } else {
                logger.error { "Failed to connect to $remoteHost:$remotePort: ${future.cause()?.message}" }
                inboundChannel.close()
            }
        }
    }

    private fun closeOnFlush(ch: Channel) {
        if (ch.isActive) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
        }
    }

    private fun switchToOsrsProtocol(channel: Channel) {
        val pipeline = channel.pipeline()

        // Remove all SOCKS handlers
        try {
            pipeline.remove(Socks5ServerEncoder::class.java)
            pipeline.remove(Socks5CommandRequestDecoder::class.java)
            pipeline.remove(this)
            logger.debug { "Removed SOCKS handlers" }
        } catch (e: Exception) {
            logger.warn(e) { "Error removing SOCKS handlers" }
        }

        // Initialize OSRS protocol
        try {
            loginInitializer.initializeOsrsProtocol(channel)
            state = State.ESTABLISHED
            logger.debug { "Initialized OSRS protocol handlers" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize OSRS protocol" }
            channel.close()
        }
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        logger.error(cause) { "Exception in SOCKS5 handler (state=$state)" }
        ctx.close()
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
