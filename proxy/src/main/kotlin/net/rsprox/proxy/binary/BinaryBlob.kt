package net.rsprox.proxy.binary

import io.netty.buffer.ByteBuf

public data class BinaryBlob(
    public val header: BinaryHeader,
    public val stream: BinaryStream,
) {
    public fun append(
        direction: StreamDirection,
        packet: ByteBuf,
    ) {
        stream.append(
            direction,
            packet,
        )
    }
}
