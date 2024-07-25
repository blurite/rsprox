package net.rsprox.cache.live

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

internal class Js5ConnectionInitializer(
    private val downloader: Js5GroupDownloader,
) : ChannelInitializer<Channel>() {
    override fun initChannel(channel: Channel) {
        logger.debug { "JS5 connection initialized to $channel" }
        channel.pipeline().addLast(Js5Decoder(downloader))
        channel.pipeline().addLast(
            IdleStateHandler(
                true,
                TIMEOUT_SEC,
                TIMEOUT_SEC,
                TIMEOUT_SEC,
                TimeUnit.SECONDS,
            ),
        )
    }

    private companion object {
        private const val TIMEOUT_SEC: Long = 30
        private val logger = InlineLogger()
    }
}
