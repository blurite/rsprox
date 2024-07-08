package net.rsprox.proxy.binary

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.Prot

public data class BinaryPacket(
    public val epochTimeMillis: Long,
    public val direction: StreamDirection,
    public val prot: Prot,
    public val size: Int,
    public val payload: ByteBuf,
)
