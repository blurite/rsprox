package net.rsprox.proxy.server

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer

public class ServerConnectionInitializer : ChannelInitializer<Channel>() {
    override fun initChannel(serverChannel: Channel) {
        logger.debug { "Connection initialized to $serverChannel" }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
