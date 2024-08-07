package net.rsprox.proxy.connection

import io.netty.channel.Channel
import net.rsprox.proxy.binary.BinaryBlob

public class ProxyConnection(
    public var clientChannel: Channel,
    public var serverChannel: Channel,
    public val blob: BinaryBlob,
)
