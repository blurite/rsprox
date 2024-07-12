package net.rsprox.proxy.plugin

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.proxy.binary.StreamDirection

public data class DirectionalPacket(
    public val direction: StreamDirection,
    public val message: IncomingMessage,
)