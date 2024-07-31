package net.rsprox.proxy.plugin

import net.rsprot.protocol.Prot
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.shared.StreamDirection

public data class DirectionalPacket(
    public val direction: StreamDirection,
    public val prot: Prot,
    public val message: IncomingMessage,
)
