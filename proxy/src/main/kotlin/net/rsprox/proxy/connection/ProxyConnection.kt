package net.rsprox.proxy.connection

import io.netty.channel.Channel
import net.rsprox.proxy.binary.BinaryBlob

public class ProxyConnection(
    public val clientChannel: Channel,
    public val serverChannel: Channel,
    public val blob: BinaryBlob,
)
