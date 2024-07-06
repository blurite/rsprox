package net.rsprox.proxy.bootstrap

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import net.rsprox.proxy.client.ClientLoginInitializer
import net.rsprox.proxy.config.JavConfig
import net.rsprox.proxy.http.HttpServerHandler
import net.rsprox.proxy.server.ServerConnectionInitializer
import net.rsprox.proxy.worlds.WorldListProvider
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.math.BigInteger

public class BootstrapFactory(
    private val allocator: ByteBufAllocator,
) {
    private fun group(numThreads: Int): EventLoopGroup {
        return NioEventLoopGroup(numThreads)
    }

    public fun createServerBootStrap(
        worldListProvider: WorldListProvider,
        rsa: RSAPrivateCrtKeyParameters,
        originalModulus: BigInteger,
    ): ServerBootstrap {
        return ServerBootstrap()
            .group(group(PARENT_GROUP_THREADS), group(CHILD_GROUP_THREADS))
            .channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.ALLOCATOR, allocator)
            .childOption(ChannelOption.ALLOCATOR, allocator)
            .childOption(ChannelOption.AUTO_READ, false)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_RCVBUF, SOCKET_BUFFER_CAPACITY)
            .childOption(ChannelOption.SO_SNDBUF, SOCKET_BUFFER_CAPACITY)
            .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
            .childHandler(ClientLoginInitializer(this, worldListProvider, rsa, originalModulus))
    }

    public fun createClientBootstrap(): Bootstrap {
        return Bootstrap()
            .group(NioEventLoopGroup())
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.ALLOCATOR, allocator)
            .option(ChannelOption.AUTO_READ, false)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_RCVBUF, SOCKET_BUFFER_CAPACITY)
            .option(ChannelOption.SO_SNDBUF, SOCKET_BUFFER_CAPACITY)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
            .handler(ServerConnectionInitializer())
    }

    public fun createWorldListHttpServer(
        worldListProvider: WorldListProvider,
        javConfig: JavConfig,
    ): ServerBootstrap {
        return ServerBootstrap()
            .group(group(PARENT_GROUP_THREADS), group(CHILD_GROUP_THREADS))
            .channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.ALLOCATOR, allocator)
            .childOption(ChannelOption.ALLOCATOR, allocator)
            .childHandler(
                object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                        val pipeline = ch.pipeline()
                        pipeline.addLast(HttpRequestDecoder())
                        pipeline.addLast(HttpResponseEncoder())
                        pipeline.addLast(HttpServerHandler(worldListProvider, javConfig))
                    }
                },
            )
    }

    private companion object {
        private const val PARENT_GROUP_THREADS: Int = 1
        private const val CHILD_GROUP_THREADS: Int = 0
        private const val SOCKET_BUFFER_CAPACITY: Int = 65536
        private const val CONNECT_TIMEOUT_MILLIS: Int = 30_000
    }
}
