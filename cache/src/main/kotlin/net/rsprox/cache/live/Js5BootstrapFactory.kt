package net.rsprox.cache.live

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelOption
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioSocketChannel

internal class Js5BootstrapFactory(
    private val allocator: ByteBufAllocator,
) {
    fun createClientBootstrap(downloader: Js5GroupDownloader): Bootstrap {
        return Bootstrap()
            .group(MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory()))
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.ALLOCATOR, allocator)
            .option(ChannelOption.AUTO_READ, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_RCVBUF, SOCKET_BUFFER_CAPACITY)
            .option(ChannelOption.SO_SNDBUF, SOCKET_BUFFER_CAPACITY)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
            .handler(Js5ConnectionInitializer(downloader))
    }

    private companion object {
        private const val SOCKET_BUFFER_CAPACITY: Int = 65536
        private const val CONNECT_TIMEOUT_MILLIS: Int = 30_000
    }
}
