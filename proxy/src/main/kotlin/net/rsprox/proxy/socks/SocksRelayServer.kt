package net.rsprox.proxy.socks

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse
import io.netty.handler.codec.socksx.v5.Socks5AddressType
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus
import io.netty.handler.codec.socksx.v5.Socks5CommandType
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5Message
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

public class SocksRelayServer : AutoCloseable {
    private val group = MultiThreadIoEventLoopGroup(THREADS, NioIoHandler.newFactory())

    private val resolver =
        Executors.newCachedThreadPool { runnable ->
            Thread(runnable, "socks-relay-resolver").apply { isDaemon = true }
        }

    private val outboundBootstrap =
        Bootstrap()
            .group(group)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.AUTO_READ, false)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
            .handler(
                object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                    }
                },
            )

    private val serverChannel: Channel by lazy {
        ServerBootstrap()
            .group(group)
            .channel(NioServerSocketChannel::class.java)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childHandler(
                object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                        ch.pipeline().addLast(
                            Socks5ServerEncoder.DEFAULT,
                            Socks5InitialRequestDecoder(),
                            HandshakeHandler(),
                        )
                    }
                },
            ).bind(InetAddress.getLoopbackAddress(), 0)
            .sync()
            .channel()
    }

    public val port: Int
        get() = (serverChannel.localAddress() as InetSocketAddress).port

    override fun close() {
        resolver.shutdownNow()
        group.shutdownGracefully().syncUninterruptibly()
    }

    private inner class HandshakeHandler : SimpleChannelInboundHandler<Socks5Message>() {
        override fun channelRead0(
            ctx: ChannelHandlerContext,
            msg: Socks5Message,
        ) {
            when (msg) {
                is Socks5InitialRequest -> {
                    ctx.pipeline().replace(Socks5InitialRequestDecoder::class.java, null, Socks5CommandRequestDecoder())
                    ctx.writeAndFlush(DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH))
                }
                is Socks5CommandRequest -> connect(ctx.channel(), msg)
                else -> ctx.close()
            }
        }

        private fun connect(
            client: Channel,
            request: Socks5CommandRequest,
        ) {
            if (request.type() != Socks5CommandType.CONNECT) {
                fail(client, Socks5CommandStatus.COMMAND_UNSUPPORTED, request)
                return
            }
            val host = request.dstAddr()
            if (request.dstAddrType() == Socks5AddressType.IPv4 && host.startsWith("127.")) {
                relayToLocalPort(client, request)
            } else {
                dialRequested(client, request)
            }
        }

        private fun relayToLocalPort(
            client: Channel,
            request: Socks5CommandRequest,
        ) {
            val requested = InetSocketAddress(InetAddress.getByName(request.dstAddr()), request.dstPort())
            bindRecording(requested).addListener(
                ChannelFutureListener { bound ->
                    if (!bound.isSuccess) {
                        fail(client, Socks5CommandStatus.FAILURE, request, bound.cause())
                        return@ChannelFutureListener
                    }
                    val local = InetSocketAddress(InetAddress.getLoopbackAddress(), request.dstPort())
                    bound.channel().connect(local).addListener { future ->
                        onConnected(client, request, future as ChannelFuture)
                    }
                },
            )
        }

        private fun bindRecording(requested: InetSocketAddress): ChannelFuture {
            return outboundBootstrap.bind(InetAddress.getLoopbackAddress(), 0).addListener(
                ChannelFutureListener { bound ->
                    if (!bound.isSuccess) return@ChannelFutureListener
                    val relayPort = (bound.channel().localAddress() as InetSocketAddress).port
                    SocksRelayDestinations[relayPort] = requested
                    bound.channel().closeFuture().addListener { SocksRelayDestinations.remove(relayPort, requested) }
                },
            )
        }

        private fun dialRequested(
            client: Channel,
            request: Socks5CommandRequest,
        ) {
            CompletableFuture
                .supplyAsync({ InetAddress.getByName(request.dstAddr()) }, resolver)
                .whenComplete { address, cause ->
                    if (cause != null) {
                        fail(client, Socks5CommandStatus.HOST_UNREACHABLE, request, cause)
                        return@whenComplete
                    }
                    outboundBootstrap.connect(address, request.dstPort()).addListener { future ->
                        onConnected(client, request, future as ChannelFuture)
                    }
                }
        }

        private fun onConnected(
            client: Channel,
            request: Socks5CommandRequest,
            future: ChannelFuture,
        ) {
            val outbound = future.channel()
            if (!future.isSuccess) {
                outbound.close()
                fail(client, Socks5CommandStatus.HOST_UNREACHABLE, request, future.cause())
                return
            }
            val response = DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4, "0.0.0.0", 0)
            client.writeAndFlush(response).addListener(
                ChannelFutureListener { written ->
                    if (!written.isSuccess) {
                        outbound.close()
                        client.close()
                        return@ChannelFutureListener
                    }
                    val pipeline = client.pipeline()
                    pipeline.remove(Socks5ServerEncoder::class.java)
                    pipeline.remove(Socks5CommandRequestDecoder::class.java)
                    pipeline.remove(HandshakeHandler::class.java)
                    client.config().isAutoRead = false
                    pipeline.addLast(RelayHandler(outbound))
                    outbound.pipeline().addLast(RelayHandler(client))
                    client.read()
                    outbound.read()
                },
            )
        }

        private fun fail(
            client: Channel,
            status: Socks5CommandStatus,
            request: Socks5CommandRequest,
            cause: Throwable? = null,
        ) {
            logger.debug(cause) { "Unable to relay ${request.dstAddr()}:${request.dstPort()}: $status" }
            client
                .writeAndFlush(DefaultSocks5CommandResponse(status, Socks5AddressType.IPv4, "0.0.0.0", 0))
                .addListener(ChannelFutureListener.CLOSE)
        }

        @Deprecated("Deprecated in Java")
        override fun exceptionCaught(
            ctx: ChannelHandlerContext,
            cause: Throwable,
        ) {
            if (cause !is IOException) {
                logger.error(cause) { "Exception in socks relay handshake $ctx" }
            }
            ctx.close()
        }
    }

    private class RelayHandler(
        private val peer: Channel,
    ) : ChannelInboundHandlerAdapter() {
        override fun channelRead(
            ctx: ChannelHandlerContext,
            msg: Any,
        ) {
            val source = ctx.channel()
            peer.writeAndFlush(msg).addListener { future ->
                if (future.isSuccess) {
                    source.read()
                } else {
                    source.close()
                }
            }
        }

        override fun channelInactive(ctx: ChannelHandlerContext) {
            if (peer.isActive) {
                peer.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun exceptionCaught(
            ctx: ChannelHandlerContext,
            cause: Throwable,
        ) {
            if (cause !is IOException) {
                logger.error(cause) { "Exception in socks relay channel $ctx" }
            }
            ctx.close()
        }
    }

    private companion object {
        private val logger = InlineLogger()
        private const val THREADS = 2
        private const val CONNECT_TIMEOUT_MILLIS = 10_000
    }
}
