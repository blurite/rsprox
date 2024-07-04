package net.rsprox.proxy.channel

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelPipeline

/**
 * Replaces a channel handler with a new variant.
 */
public inline fun <reified T : ChannelHandler> ChannelPipeline.replace(newHandler: ChannelHandler): ChannelHandler =
    replace(T::class.java, newHandler::class.qualifiedName, newHandler)

public inline fun <reified T : ChannelHandler> ChannelPipeline.remove(): ChannelHandler = remove(T::class.java)
